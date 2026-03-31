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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kruize.optimizer.utils.OptimizerConstants.JobsConstants;

/**
 * Model representing bulk jobs overview statistics
 */
public class JobsOverview {

    @JsonProperty(JobsConstants.JOBS_TRIGGERED)
    private int jobsTriggered;

    @JsonProperty(JobsConstants.TOTAL_EXPERIMENTS)
    private int totalExperiments;

    @JsonProperty(JobsConstants.PROCESSED_EXPERIMENTS)
    private int processedExperiments;

    @JsonProperty(JobsConstants.UNIQUE_EXPERIMENTS)
    private int uniqueExperiments;

    public JobsOverview() {
    }

    public JobsOverview(int jobsTriggered, int totalExperiments, int processedExperiments, int uniqueExperiments) {
        this.jobsTriggered = jobsTriggered;
        this.totalExperiments = totalExperiments;
        this.processedExperiments = processedExperiments;
        this.uniqueExperiments = uniqueExperiments;
    }

    public int getJobsTriggered() {
        return jobsTriggered;
    }

    public void setJobsTriggered(int jobsTriggered) {
        this.jobsTriggered = jobsTriggered;
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

    public int getUniqueExperiments() {
        return uniqueExperiments;
    }

    public void setUniqueExperiments(int uniqueExperiments) {
        this.uniqueExperiments = uniqueExperiments;
    }
}

