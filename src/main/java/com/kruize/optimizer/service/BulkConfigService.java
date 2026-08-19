/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.kruize.optimizer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kruize.optimizer.client.KruizeClient;
import com.kruize.optimizer.model.kruize.BulkConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for managing bulk configs and converting them to bulk job requests
 */
@ApplicationScoped
public class BulkConfigService {

    private static final Logger LOG = Logger.getLogger(BulkConfigService.class);

    @Inject
    @RestClient
    KruizeClient kruizeClient;

    @Inject
    ObjectMapper objectMapper;

    // Regex pattern for parsing scheduling strings like "24h", "15min", "2d"
    private static final Pattern SCHEDULING_PATTERN =
            Pattern.compile("(\\d+)\\s*(h|hr|hrs|hour|hours|m|min|mins|minute|minutes|d|day|days)");

    /**
     * Fetch all enabled configs from Kruize
     *
     * @return List of enabled bulk configs
     */
    public List<BulkConfig> getEnabledConfigs() {
        try {
            String response = kruizeClient.getBulkConfigs(null);
            List<BulkConfig> allConfigs = objectMapper.readValue(
                    response,
                    new TypeReference<List<BulkConfig>>() {}
            );

            List<BulkConfig> enabledConfigs = allConfigs.stream()
                    .filter(c -> c.getEnabled() != null && c.getEnabled())
                    .collect(Collectors.toList());

            LOG.infof("Fetched %d enabled configs out of %d total configs",
                    enabledConfigs.size(), allConfigs.size());

            return enabledConfigs;

        } catch (Exception e) {
            LOG.error("Failed to fetch bulk configs from Kruize", e);
            return Collections.emptyList();
        }
    }

    /**
     * Parse scheduling string to Duration
     * Examples: "24h" -> 24 hours, "15min" -> 15 minutes, "2d" -> 2 days
     *
     * @param scheduling Scheduling string
     * @return Duration object
     */
    public Duration parseScheduling(String scheduling) {
        if (scheduling == null || scheduling.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheduling string cannot be null or empty");
        }

        Matcher matcher = SCHEDULING_PATTERN.matcher(scheduling.trim().toLowerCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid scheduling format: " + scheduling);
        }

        int value = Integer.parseInt(matcher.group(1));
        String unit = matcher.group(2);

        switch (unit) {
            case "h":
            case "hr":
            case "hrs":
            case "hour":
            case "hours":
                return Duration.ofHours(value);
            case "m":
            case "min":
            case "mins":
            case "minute":
            case "minutes":
                return Duration.ofMinutes(value);
            case "d":
            case "day":
            case "days":
                return Duration.ofDays(value);
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
    }

    /**
     * Convert bulk config to bulk job request.
     *
     * Expected bulk API format:
     * {
     *   "filter": {
     *     "include": {
     *       "namespaces": ["ns1"],
     *       "labels":     {"key": "value"}
     *     }
     *   },
     *   "cluster_name":       "cluster-name",
     *   "datasource":         "datasource-name",
     *   "metadata_profile":   "profile-name",
     *   "measurement_duration": "15min",
     *   "recommendation_settings": {
     *     "scheduling": "24h",
     *     "terms":  ["short_term"],
     *     "models": ["cost"]
     *   },
     *   "webhook": {
     *     "url": "http://..."
     *   }
     * }
     *
     * @param config Bulk config
     * @return Bulk job request as Map
     */
    public Map<String, Object> convertConfigToBulkJob(BulkConfig config) {
        Map<String, Object> bulkJob = new HashMap<>();

        // Build filter with namespaces and labels
        boolean hasNamespaces = config.getNamespaces() != null && !config.getNamespaces().isEmpty();
        boolean hasLabels = config.getLabels() != null && !config.getLabels().isEmpty();
        if (hasNamespaces || hasLabels) {
            Map<String, Object> include = new HashMap<>();
            if (hasNamespaces) {
                include.put("namespaces", config.getNamespaces());
            }
            if (hasLabels) {
                include.put("labels", config.getLabels());
            }
            Map<String, Object> filter = new HashMap<>();
            filter.put("include", include);
            bulkJob.put("filter", filter);
        }

        // Add cluster name
        if (config.getClusterName() != null && !config.getClusterName().isEmpty()) {
            bulkJob.put("cluster_name", config.getClusterName());
        }

        // Only the first datasource is used. Kruize bulk API accepts a single datasource
        // per job; multi-datasource configs require one job submission per datasource.
        if (config.getDatasources() != null && !config.getDatasources().isEmpty()) {
            if (config.getDatasources().size() > 1) {
                LOG.warnf("Config '%s' defines %d datasources; only the first ('%s') will be used for the bulk job",
                        config.getConfigName(), config.getDatasources().size(), config.getDatasources().get(0));
            }
            bulkJob.put("datasource", config.getDatasources().get(0));
        }

        // Add metadata profile
        if (config.getMetadataProfile() != null && !config.getMetadataProfile().isEmpty()) {
            bulkJob.put("metadata_profile", config.getMetadataProfile());
        }

        // Add measurement duration from trial settings
        if (config.getTrialSettings() != null) {
            String measurementDuration = config.getTrialSettings().getMeasurementDuration();
            if (measurementDuration != null && !measurementDuration.isEmpty()) {
                bulkJob.put("measurement_duration", measurementDuration);
            }
        }

        // Add recommendation settings (scheduling, terms, models)
        if (config.getRecommendationSettings() != null) {
            Map<String, Object> recSettings = new HashMap<>();
            String scheduling = config.getRecommendationSettings().getScheduling();
            if (scheduling != null && !scheduling.isEmpty()) {
                recSettings.put("scheduling", scheduling);
            }
            if (config.getRecommendationSettings().getTerms() != null
                    && !config.getRecommendationSettings().getTerms().isEmpty()) {
                recSettings.put("terms", config.getRecommendationSettings().getTerms());
            }
            if (config.getRecommendationSettings().getModels() != null
                    && !config.getRecommendationSettings().getModels().isEmpty()) {
                recSettings.put("models", config.getRecommendationSettings().getModels());
            }
            if (!recSettings.isEmpty()) {
                bulkJob.put("recommendation_settings", recSettings);
            }
        }

        // Add webhook URL if present
        if (config.getWebhookUrl() != null && !config.getWebhookUrl().isEmpty()) {
            Map<String, String> webhook = new HashMap<>();
            webhook.put("url", config.getWebhookUrl());
            bulkJob.put("webhook", webhook);
        }

        LOG.debugf("Converted config '%s' to bulk job request: %s",
                config.getConfigName(), bulkJob);

        return bulkJob;
    }
}
