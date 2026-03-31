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
package com.kruize.optimizer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kruize.optimizer.utils.OptimizerConstants.WebhookConstants;
import com.kruize.optimizer.utils.OptimizerConstants.JobsConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {

    @JsonProperty(WebhookConstants.SUMMARY)
    private Summary summary;

    @JsonProperty(WebhookConstants.WEBHOOK)
    private WebhookStatus webhook;

    // Getters and Setters
    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public WebhookStatus getWebhook() {
        return webhook;
    }

    public void setWebhook(WebhookStatus webhook) {
        this.webhook = webhook;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        @JsonProperty(WebhookConstants.JOB_ID)
        private String jobId;

        @JsonProperty(WebhookConstants.STATUS)
        private String status;

        @JsonProperty(JobsConstants.TOTAL_EXPERIMENTS)
        private int totalExperiments;

        @JsonProperty(JobsConstants.PROCESSED_EXPERIMENTS)
        private int processedExperiments;

        @JsonProperty(JobsConstants.EXISTING_EXPERIMENTS)
        private int existingExperiments;

        // Getters and Setters
        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getTotalExperiments() {
            return totalExperiments;
        }

        public void setTotalExperiments(int totalExperiments) {
            this.totalExperiments = totalExperiments;
        }

        public int getProcessedExperiments() {
            return processedExperiments;
        }

        public void setProcessedExperiments(int processedExperiments) {
            this.processedExperiments = processedExperiments;
        }

        public int getExistingExperiments() {
            return existingExperiments;
        }

        public void setExistingExperiments(int existingExperiments) {
            this.existingExperiments = existingExperiments;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookStatus {
        @JsonProperty(WebhookConstants.STATUS)
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
