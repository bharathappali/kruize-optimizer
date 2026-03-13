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
package com.kruize.optimizer.utils;

public final class OptimizerConstants {

    private OptimizerConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // contains constants related to Kruize application
    public static final class KruizeClientConstants {

        private KruizeClientConstants() {
            throw new UnsupportedOperationException("Utility class");
        }

        // query params and other general constants
        public static final String VERBOSE = "verbose";
        public static final String NAME = "name";

        // list APIs
        public static final String LIST_DATASOURCE_ENDPOINT = "/datasources";
        public static final String LIST_METADATA_PROFILE_ENDPOINT = "/listMetadataProfiles";
        public static final String LIST_METRIC_PROFILE_ENDPOINT = "/listMetricProfiles";
        public static final String LIST_LAYERS_ENDPOINT = "/listLayers";
        public static final String LIST_EXPERIMENTS_ENDPOINT = "/listExperiments";

        // create APIs
        public static final String CREATE_METADATA_PROFILE_ENDPOINT = "/createMetadataProfile";
        public static final String CREATE_METRIC_PROFILE_ENDPOINT = "/createMetricProfile";
        public static final String CREATE_LAYERS_ENDPOINT = "/createLayer";

        // update APIs
        public static final String UPDATE_METADATA_PROFILE_ENDPOINT = "/updateMetadataProfile";
        public static final String UPDATE_METRIC_PROFILE_ENDPOINT = "/updateMetricProfile";

        // bulk APIs
        public static final String BULK_ENDPOINT = "/bulk";
        public static final String JOB_ID = "job_id";
    }

    // contains constants for Optimizer Service API endpoints
    public static final class OptimizerApiConstants {

        private OptimizerApiConstants() {
            throw new UnsupportedOperationException("Utility class");
        }

        // Base path
        public static final String KRUIZE_BASE_PATH = "/kruize";

        // Common endpoint suffixes (reusable across resources)
        public static final String LIST_PATH = "/list";
        public static final String INSTALL_PATH = "/install";

        // Datasource endpoints
        public static final String DATASOURCES_PATH = "/datasources";

        // Metadata Profile endpoints
        public static final String METADATA_PROFILES_PATH = "/metadataProfiles";

        // Metric Profile endpoints
        public static final String METRIC_PROFILES_PATH = "/metricProfiles";

        // Layer endpoints
        public static final String LAYERS_PATH = "/layers";

        // Status endpoint
        public static final String STATUS_PATH = "/status";
    }

    // contains generic constants related to Optimizer application
    public static final class GenericOptimizerConstants {

        private GenericOptimizerConstants() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String STARTUP_MESSAGE = "Kruize Optimizer Service is STARTED!";
    }

    // contains message constants
    public static final class MessageConstants {

        private MessageConstants() {
            throw new UnsupportedOperationException("Utility class");
        }

        // Success messages
        public static final String PROFILES_INSTALLED_SUCCESS = "Profiles installed successfully";
        public static final String DATASOURCES_FETCHED_SUCCESS = "Datasources fetched successfully";
        public static final String PROFILES_FETCHED_SUCCESS = "Profiles fetched successfully";

        // Error messages
        public static final String ERROR_FETCHING_DATASOURCES = "Error fetching datasources from Kruize";
        public static final String ERROR_FETCHING_PROFILES = "Error fetching profiles from Kruize";
        public static final String ERROR_INSTALLING_PROFILES = "Error installing profiles";
        public static final String ERROR_READING_PROFILE_FILE = "Error reading profile file";
        public static final String ERROR_INVALID_PROFILE_FORMAT = "Invalid profile format";
        public static final String KRUIZE_SERVICE_UNAVAILABLE = "Kruize service is unavailable";

        // Info messages
        public static final String NO_DATASOURCES_FOUND = "No datasources found";
        public static final String NO_PROFILES_FOUND = "No profiles found";
        public static final String PROFILE_ALREADY_EXISTS = "Profile already exists";
        public static final String PROFILE_NOT_FOUND = "Profile not found in local repository";
    }

    // contains profile type constants
    public static final class ProfileType {

        private ProfileType() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String METADATA = "metadata";
        public static final String METRIC = "metric";
        public static final String LAYER = "layer";
    }
}
