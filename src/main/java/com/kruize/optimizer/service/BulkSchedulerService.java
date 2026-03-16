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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kruize.optimizer.client.KruizeClient;
import com.kruize.optimizer.model.WebhookPayload;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * Service that schedules bulk API calls at configurable intervals
 * with a configurable target label.
 */
@ApplicationScoped
public class BulkSchedulerService {

    private static final Logger LOG = Logger.getLogger(BulkSchedulerService.class);

    @Inject
    @RestClient
    KruizeClient kruizeClient;

    @Inject
    KruizeStateService kruizeStateService;

    @Inject
    JobsService jobsService;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "kruize.bulk.scheduler.measurement-duration")
    String measurementDuration;

    @ConfigProperty(name = "kruize.webhook.url")
    String webhookUrl;
    
    @ConfigProperty(name = "kruize.target.labels.json")
    String targetLabelsJson;

    @ConfigProperty(name = "kruize.bulk.scheduler.startup-delay", defaultValue = "1m")
    String startupDelay;

    private final Set<String> completedJobs = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
    private volatile boolean initialized = false;

    /**
     * Initialize the bulk scheduler by refreshing state and installing missing profiles
     */
    public void initialize() {
        try {
            LOG.info("Initializing bulk scheduler...");
            
            // Use common function to refresh state and install missing profiles
            kruizeStateService.refreshStateAndInstallProfiles();
            
            initialized = true;
            LOG.info("Bulk scheduler initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize bulk scheduler", e);
        }
    }

    /**
     * Scheduled method that calls the bulk API at the configured interval.
     * The interval is configured via kruize.bulk.scheduler.interval property.
     * Waits for initialization to complete before executing.
     */
    @Scheduled(every = "${kruize.bulk.scheduler.interval:5m}", delayed = "${kruize.bulk.scheduler.startup-delay:1m}")
    public void scheduledBulkApiCall() {
        if (!initialized) {
            LOG.info("Bulk scheduler not yet initialized. Skipping this run.");
            return;
        }

        LOG.infof("Starting scheduled bulk API call with target labels: %s", targetLabelsJson);

        try {
            // Parse target labels from JSON
            Map<String, String> targetLabels = parseTargetLabels();
            if (targetLabels.isEmpty()) {
                LOG.error("No valid target labels found. Cannot proceed with bulk API call.");
                return;
            }

            // Check if state cache is empty, refresh if needed
            if (kruizeStateService.isCacheEmpty()) {
                LOG.info("Kruize state cache is empty, refreshing...");
                kruizeStateService.refreshState();
            }

            // Get datasource from global state
            Optional<String> datasourceName = kruizeStateService.getDefaultDatasourceName();
            if (!datasourceName.isPresent()) {
                LOG.error("No datasource available in Kruize. Cannot proceed with bulk API call.");
                return;
            }

            // Get metadata profile from global state
            Optional<String> metadataProfileName = kruizeStateService.getDefaultMetadataProfileName();
            if (!metadataProfileName.isPresent()) {
                LOG.error("No metadata profile available in Kruize. Cannot proceed with bulk API call.");
                return;
            }

            // Get metric profile from global state
            Optional<String> metricProfileName = kruizeStateService.getDefaultMetricProfileName();
            if (!metricProfileName.isPresent()) {
                LOG.error("No metric profile available in Kruize. Cannot proceed with bulk API call.");
                return;
            }

            // Construct the bulk API payload
            Map<String, Object> payload = buildBulkPayload(
                    targetLabels,
                    datasourceName.get(),
                    metadataProfileName.get(),
                    metricProfileName.get()
            );

            // Log the exact JSON payload before calling the bulk API
            try {
                String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
                LOG.infof("Calling bulk API with payload:\n%s", jsonPayload);
            } catch (Exception e) {
                LOG.warnf(e, "Failed to serialize payload to JSON for logging");
            }

            // Call the bulk API
            String response = kruizeClient.bulkCreateExperiments(payload);
            LOG.infof("Bulk API call successful. Response: %s", response);

            // Increment job counter in global state
            jobsService.incrementJobsTriggered();

        } catch (Exception e) {
            LOG.errorf(e, "Failed to execute scheduled bulk API call");
        }
    }

    /**
     * Parse target labels from JSON configuration
     *
     * @return Map of label key-value pairs
     */
    private Map<String, String> parseTargetLabels() {
        Map<String, String> labels = new HashMap<>();
        try {
            // Simple JSON parsing for {"key": "value"} format
            String json = targetLabelsJson.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] pairs = json.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        labels.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to parse target labels JSON: %s", targetLabelsJson);
        }
        return labels;
    }

    /**
     * Builds the payload for the bulk API call.
     *
     * @param targetLabels       The target labels to filter workloads
     * @param datasource         The datasource name
     * @param metadataProfile    The metadata profile name
     * @param metricProfile      The metric profile name
     * @return The bulk API payload as a Map
     */
    private Map<String, Object> buildBulkPayload(Map<String, String> targetLabels,
                                                   String datasource, String metadataProfile, String metricProfile) {
        Map<String, Object> payload = new HashMap<>();

        // Create filter with the target labels
        Map<String, Object> filter = new HashMap<>();
        Map<String, Object> include = new HashMap<>();
        
        // Add label filter
        include.put("labels", targetLabels);

        filter.put("include", include);
        payload.put("filter", filter);

        // Add datasource from global state
        payload.put("datasource", datasource);

        // Add metadata profile from global state
        payload.put("metadata_profile", metadataProfile);

        // Add measurement duration
        payload.put("measurement_duration", measurementDuration);

        // Add webhook URL
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            Map<String, String> webhook = new HashMap<>();
            webhook.put("url", webhookUrl);
            payload.put("webhook", webhook);
        }

        LOG.debugf("Built bulk payload: %s", payload);
        return payload;
    }

    /**
     * Handle webhook callbacks from Kruize bulk API
     *
     * @param payloads List of webhook payloads
     */
    public void handleWebhook(List<WebhookPayload> payloads) {
        for (WebhookPayload payload : payloads) {
            if (payload.getSummary() != null) {
                WebhookPayload.Summary summary = payload.getSummary();
                String jobId = summary.getJobId();
                String status = summary.getStatus();
                LOG.infof("Received webhook for Job %s with status %s", jobId, status);

                if ("COMPLETED".equalsIgnoreCase(status)) {
                    if (!completedJobs.add(jobId)) {
                        LOG.infof("Job %s already processed. Skipping.", jobId);
                        continue;
                    }

                    int total = summary.getTotalExperiments();
                    int processed = summary.getProcessedExperiments();
                    int existing = summary.getExistingExperiments();

                    // Update experiment counters in global state
                    jobsService.updateExperimentCounters(total, processed, existing);

                    LOG.infof("Job %s completed. Total: %d, Processed: %d, Existing: %d",
                            jobId, total, processed, existing);
                } else {
                    LOG.infof("Job %s status is %s", jobId, status);
                }
            }
        }
    }
}

