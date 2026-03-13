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
package com.kruize.optimizer.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kruize.optimizer.model.kruize.Datasource;
import java.util.List;

/**
 * Response model for datasource list API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceListResponse {

    private List<Datasource> datasources;

    public DatasourceListResponse() {
    }

    public DatasourceListResponse(List<Datasource> datasources) {
        this.datasources = datasources;
    }

    public List<Datasource> getDatasources() {
        return datasources;
    }

    public void setDatasources(List<Datasource> datasources) {
        this.datasources = datasources;
    }

    @Override
    public String toString() {
        return "DatasourceListResponse{" +
                "datasources=" + datasources +
                '}';
    }
}

