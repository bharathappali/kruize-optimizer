package com.kruize.optimizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ProfileScanResult {
    @JsonProperty("metadata_profiles")
    private List<ParsedProfile> metadataProfiles = new ArrayList<>();

    @JsonProperty("metric_profiles")
    private List<ParsedProfile> metricProfiles;

    private List<ParsedProfile> layers;

    private List<ParsedProfile> rulesets;

    private List<String> alerts;

    public List<ParsedProfile> getMetadataProfiles() {
        return metadataProfiles;
    }

    public void setMetadataProfiles(List<ParsedProfile> metadataProfiles) {
        this.metadataProfiles = metadataProfiles;
    }

    public List<ParsedProfile> getMetricProfiles() {
        return metricProfiles;
    }

    public void setMetricProfiles(List<ParsedProfile> metricProfiles) {
        this.metricProfiles = metricProfiles;
    }

    public List<ParsedProfile> getLayers() {
        return layers;
    }

    public void setLayers(List<ParsedProfile> layers) {
        this.layers = layers;
    }

    public List<ParsedProfile> getRulesets() {
        return rulesets;
    }

    public void setRulesets(List<ParsedProfile> rulesets) {
        this.rulesets = rulesets;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }

    public void addAlert(String alert) {
        this.alerts.add(alert);
    }

    public static class ParsedProfile {
        private String name;
        @JsonProperty("profile_version")
        private String profileVersion;

        public ParsedProfile() {
        }

        public ParsedProfile(String name, String profileVersion) {
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
