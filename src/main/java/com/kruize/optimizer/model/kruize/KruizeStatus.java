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
package com.kruize.optimizer.model.kruize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing the overall Kruize system status
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KruizeStatus {

    private DatasourceStatus datasources;

    @JsonProperty("metadata_profiles")
    private ProfileStatus metadataProfiles;

    @JsonProperty("metric_profiles")
    private ProfileStatus metricProfiles;

    private ProfileStatus layers;

    private List<String> alerts;

    public KruizeStatus() {
        this.alerts = new ArrayList<>();
    }

    public DatasourceStatus getDatasources() {
        return datasources;
    }

    public void setDatasources(DatasourceStatus datasources) {
        this.datasources = datasources;
    }

    public ProfileStatus getMetadataProfiles() {
        return metadataProfiles;
    }

    public void setMetadataProfiles(ProfileStatus metadataProfiles) {
        this.metadataProfiles = metadataProfiles;
    }

    public ProfileStatus getMetricProfiles() {
        return metricProfiles;
    }

    public void setMetricProfiles(ProfileStatus metricProfiles) {
        this.metricProfiles = metricProfiles;
    }

    public ProfileStatus getLayers() {
        return layers;
    }

    public void setLayers(ProfileStatus layers) {
        this.layers = layers;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }

    public void addAlert(String alert) {
        if (this.alerts == null) {
            this.alerts = new ArrayList<>();
        }
        this.alerts.add(alert);
    }

    /**
     * Nested class for datasource status
     */
    public static class DatasourceStatus {
        private int count;
        private List<Datasource> list;

        public DatasourceStatus() {
        }

        public DatasourceStatus(int count, List<Datasource> list) {
            this.count = count;
            this.list = list;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<Datasource> getList() {
            return list;
        }

        public void setList(List<Datasource> list) {
            this.list = list;
        }
    }

    /**
     * Nested class for profile status
     */
    public static class ProfileStatus {
        private int count;
        private List<ProfileInfo> installed;

        public ProfileStatus() {
        }

        public ProfileStatus(int count, List<ProfileInfo> installed) {
            this.count = count;
            this.installed = installed;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<ProfileInfo> getInstalled() {
            return installed;
        }

        public void setInstalled(List<ProfileInfo> installed) {
            this.installed = installed;
        }
    }

    /**
     * Nested class for profile information
     */
    public static class ProfileInfo {
        private String name;

        @JsonProperty("profile_version")
        private String profileVersion;

        public ProfileInfo() {
        }

        public ProfileInfo(String name, String profileVersion) {
            this.name = name;
            this.profileVersion = profileVersion;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfileVersion() {
            return profileVersion;
        }

        public void setProfileVersion(String profileVersion) {
            this.profileVersion = profileVersion;
        }
    }
}

// Made with Bob
