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
package com.kruize.optimizer.resource;

import com.kruize.optimizer.client.KruizeClient;
import com.kruize.optimizer.model.api.DatasourceListResponse;
import com.kruize.optimizer.service.BulkSchedulerService;
import com.kruize.optimizer.service.KruizeStateService;
import com.kruize.optimizer.util.MockResponseLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for DatasourceResource using mock JSON responses
 */
@QuarkusTest
class DatasourceResourceTest {
    
    @InjectMock
    BulkSchedulerService bulkSchedulerService;
    
    @InjectMock
    KruizeStateService kruizeStateService;
    
    @InjectMock
    @RestClient
    KruizeClient kruizeClient;

    private DatasourceListResponse mockDatasourceResponse;
    private DatasourceListResponse emptyDatasourceResponse;

    @BeforeEach
    void setUp() throws IOException {
        Mockito.reset(bulkSchedulerService, kruizeStateService, kruizeClient);
        
        // Mock the initialization to prevent startup from connecting to real Kruize
        doNothing().when(bulkSchedulerService).initialize();
        doNothing().when(kruizeStateService).refreshStateAndInstallProfiles();
        
        // Load mock responses from JSON files
        mockDatasourceResponse = MockResponseLoader.loadMockResponse("datasource_list.json", DatasourceListResponse.class);
        emptyDatasourceResponse = MockResponseLoader.loadMockResponse("empty_datasource_list.json", DatasourceListResponse.class);
    }

    @Test
    void testListDatasources_Success() {
        // Arrange - Mock KruizeClient to return the JSON response
        when(kruizeClient.getDatasources()).thenReturn(mockDatasourceResponse);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/datasources/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"))
            .body("message", containsString("successfully"))
            .body("data", hasSize(1))
            .body("data[0].name", equalTo("prometheus-1"))
            .body("data[0].provider", equalTo("prometheus"))
            .body("data[0].url", equalTo("http://prometheus-k8s.monitoring.svc.cluster.local:9090"));
        
        verify(kruizeClient, times(1)).getDatasources();
    }

    @Test
    void testListDatasources_EmptyList() {
        // Arrange - Mock KruizeClient to return empty list
        when(kruizeClient.getDatasources()).thenReturn(emptyDatasourceResponse);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/datasources/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"))
            .body("message", containsString("No datasources found"))
            .body("data", hasSize(0));
        
        verify(kruizeClient, times(1)).getDatasources();
    }

    @Test
    void testListDatasources_ServiceException() {
        // Arrange - Mock KruizeClient to throw exception
        when(kruizeClient.getDatasources()).thenThrow(new RuntimeException("Service error"));
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/datasources/list")
            .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("status", equalTo("error"))
            .body("message", containsString("Error fetching datasources"));
        
        verify(kruizeClient, times(1)).getDatasources();
    }

    @Test
    void testListDatasources_VerifyResponseStructure() {
        // Arrange
        when(kruizeClient.getDatasources()).thenReturn(mockDatasourceResponse);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/datasources/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasKey("status"))
            .body("$", hasKey("message"))
            .body("$", hasKey("data"))
            .body("status", notNullValue())
            .body("message", notNullValue())
            .body("data", notNullValue());
        
        verify(kruizeClient, times(1)).getDatasources();
    }
}

