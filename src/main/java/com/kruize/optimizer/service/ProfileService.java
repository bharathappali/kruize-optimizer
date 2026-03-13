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
import com.kruize.optimizer.model.kruize.KruizeProfile;
import com.kruize.optimizer.utils.OptimizerConstants.MessageConstants;
import com.kruize.optimizer.utils.OptimizerConstants.ProfileType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing Kruize profiles (metadata, metric, layers)
 */
@ApplicationScoped
public class ProfileService {

    private static final Logger LOG = Logger.getLogger(ProfileService.class);

    @Inject
    @RestClient
    KruizeClient kruizeClient;

    @ConfigProperty(name = "kruize.profile.git.base-url", defaultValue = "local")
    String profileBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get metadata profiles from Kruize
     *
     * @param verbose include detailed information
     * @return List of metadata profiles
     */
    public List<KruizeProfile> getMetadataProfiles(boolean verbose) {
        try {
            LOG.info("Fetching metadata profiles from Kruize");
            return Optional.ofNullable(kruizeClient.getMetadataProfiles(verbose))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            LOG.error(MessageConstants.ERROR_FETCHING_PROFILES, e);
            throw new RuntimeException(MessageConstants.ERROR_FETCHING_PROFILES, e);
        }
    }

    /**
     * Get metric profiles from Kruize
     *
     * @param verbose include detailed information
     * @return List of metric profiles
     */
    public List<KruizeProfile> getMetricProfiles(boolean verbose) {
        try {
            LOG.info("Fetching metric profiles from Kruize");
            return Optional.ofNullable(kruizeClient.getMetricProfiles(verbose))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            LOG.error(MessageConstants.ERROR_FETCHING_PROFILES, e);
            throw new RuntimeException(MessageConstants.ERROR_FETCHING_PROFILES, e);
        }
    }

    /**
     * Get layers from Kruize
     *
     * @return List of layers
     */
    public List<KruizeProfile> getLayers() {
        try {
            LOG.info("Fetching layers from Kruize");
            return Optional.ofNullable(kruizeClient.getLayers())
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            LOG.error(MessageConstants.ERROR_FETCHING_PROFILES, e);
            throw new RuntimeException(MessageConstants.ERROR_FETCHING_PROFILES, e);
        }
    }

    /**
     * Install missing profiles from local repository
     *
     * @param profileType type of profile (metadata, metric, layer)
     * @return List of installation results
     */
    public List<String> installMissingProfiles(String profileType) {
        List<String> results = new ArrayList<>();
        
        try {
            // Get installed profiles
            List<KruizeProfile> installedProfiles = getInstalledProfiles(profileType);
            Set<String> installedNames = installedProfiles.stream()
                    .map(p -> p.getMetadata() != null ? p.getMetadata().getName() : p.getName())
                    .collect(Collectors.toSet());

            // Get available profiles from local repository
            List<String> availableProfiles = getAvailableProfilesFromLocal(profileType);

            // Install missing profiles
            for (String profileName : availableProfiles) {
                if (!installedNames.contains(profileName)) {
                    try {
                        installProfile(profileType, profileName);
                        results.add("Installed: " + profileName);
                        LOG.info("Successfully installed profile: " + profileName);
                    } catch (Exception e) {
                        String error = "Failed to install " + profileName + ": " + e.getMessage();
                        results.add(error);
                        LOG.error(error, e);
                    }
                } else {
                    results.add("Already installed: " + profileName);
                }
            }

        } catch (Exception e) {
            LOG.error(MessageConstants.ERROR_INSTALLING_PROFILES, e);
            results.add("Error: " + e.getMessage());
        }

        return results;
    }

    /**
     * Install a specific profile
     *
     * @param profileType type of profile
     * @param profileName name of the profile
     */
    private void installProfile(String profileType, String profileName) {
        try {
            Object profileDefinition = loadProfileFromLocal(profileType, profileName);
            
            switch (profileType) {
                case ProfileType.METADATA:
                    kruizeClient.createMetadataProfile(profileDefinition);
                    break;
                case ProfileType.METRIC:
                    kruizeClient.createMetricProfile(profileDefinition);
                    break;
                case ProfileType.LAYER:
                    kruizeClient.createLayer(profileDefinition);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown profile type: " + profileType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to install profile: " + profileName, e);
        }
    }

    /**
     * Load profile definition from local resources
     *
     * @param profileType type of profile
     * @param profileName name of the profile
     * @return profile definition as Object
     */
    private Object loadProfileFromLocal(String profileType, String profileName) {
        try {
            String resourcePath = getResourcePath(profileType, profileName);
            LOG.info("Loading profile from: " + resourcePath);
            
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new RuntimeException(MessageConstants.PROFILE_NOT_FOUND + ": " + resourcePath);
                }
                return objectMapper.readValue(inputStream, Object.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(MessageConstants.ERROR_READING_PROFILE_FILE + ": " + profileName, e);
        }
    }

    /**
     * Get resource path for profile
     *
     * @param profileType type of profile
     * @param profileName name of the profile
     * @return resource path
     */
    private String getResourcePath(String profileType, String profileName) {
        String basePath = "configs/v1.0/";
        switch (profileType) {
            case ProfileType.METADATA:
                return basePath + "metadata-profiles/" + profileName + ".json";
            case ProfileType.METRIC:
                return basePath + "metric-profiles/" + profileName + ".json";
            case ProfileType.LAYER:
                return basePath + "layers/" + profileName + ".json";
            default:
                throw new IllegalArgumentException("Unknown profile type: " + profileType);
        }
    }

    /**
     * Get list of available profiles from local repository
     *
     * @param profileType type of profile
     * @return list of profile names
     */
    private List<String> getAvailableProfilesFromLocal(String profileType) {
        // For now, return hardcoded list based on standard profiles
        // In production, this could scan the resources directory
        List<String> profiles = new ArrayList<>();
        
        switch (profileType) {
            case ProfileType.METADATA:
                profiles.add("cluster-metadata-local-monitoring");
                break;
            case ProfileType.METRIC:
                profiles.add("resource-optimization-local-monitoring");
                break;
            case ProfileType.LAYER:
                profiles.addAll(Arrays.asList("container", "hotspot", "openj9", "quarkus"));
                break;
        }
        
        return profiles;
    }

    /**
     * Get installed profiles based on type
     *
     * @param profileType type of profile
     * @return list of installed profiles
     */
    private List<KruizeProfile> getInstalledProfiles(String profileType) {
        switch (profileType) {
            case ProfileType.METADATA:
                return getMetadataProfiles(false);
            case ProfileType.METRIC:
                return getMetricProfiles(false);
            case ProfileType.LAYER:
                return getLayers();
            default:
                return Collections.emptyList();
        }
    }
}

// Made with Bob
