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

import com.fasterxml.jackson.core.type.TypeReference;
import com.kruize.optimizer.client.KruizeClient;
import com.kruize.optimizer.model.kruize.KruizeProfile;
import com.kruize.optimizer.service.BulkSchedulerService;
import com.kruize.optimizer.service.KruizeStateService;
import com.kruize.optimizer.util.MockResponseLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for MetricProfileResource using mock JSON responses
 */
@QuarkusTest
class MetricProfileResourceTest {
    
    @InjectMock
    BulkSchedulerService bulkSchedulerService;
    
    @InjectMock
    KruizeStateService kruizeStateService;
    
    @InjectMock
    @RestClient
    KruizeClient kruizeClient;

    private List<KruizeProfile> mockMetricProfilesList;
    private String emptyMetricProfilesResponse;

    @BeforeEach
    void setUp() throws IOException {
        Mockito.reset(bulkSchedulerService, kruizeStateService, kruizeClient);
        
        // Mock the initialization to prevent startup from connecting to real Kruize
        doNothing().when(bulkSchedulerService).initialize();
        doNothing().when(kruizeStateService).refreshStateAndInstallProfiles();
        
        // Load mock responses from JSON files
        mockMetricProfilesList = MockResponseLoader.loadMockResponse("metric_profile_list.json", new TypeReference<List<KruizeProfile>>() {});
        emptyMetricProfilesResponse = MockResponseLoader.loadMockResponseAsString("empty_metric_profile_list.json");
    }

    /**
     * Test successful retrieval of metric profiles list
     *
     * Test Description: Verifies that the metric profiles list endpoint returns all available
     * metric profiles when Kruize service has profiles configured.
     *
     * Expected Output:
     * - HTTP Status: 200 OK
     * - Response body contains:
     *   - status: "success"
     *   - message: "Profiles fetched successfully"
     *   - data: Array with 1 metric profile (resource-optimization-local-monitoring)
     */
    @Test
    void testListMetricProfiles_Success() {
        // Arrange - Mock KruizeClient to return the JSON response
        when(kruizeClient.getMetricProfiles(true)).thenReturn(mockMetricProfilesList);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/metricProfiles/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"))
            .body("message", equalTo("Profiles fetched successfully"))
            .body("data", hasSize(1))
            .body("data[0].name", equalTo("resource-optimization-local-monitoring"));
        
        verify(kruizeClient, times(1)).getMetricProfiles(true);
    }

    /**
     * Test metric profiles list endpoint when no profiles are found
     *
     * Test Description: Verifies that when Kruize returns a 400 error indicating no metric
     * profiles are found, the optimizer service handles it gracefully and returns an empty
     * list with a success status.
     *
     * Mock Response (from empty_metric_profile_list.json):
     * - Kruize returns: {"message": "No metric profiles found!", "httpcode": 400, "status": "ERROR"}
     *
     * Expected Output:
     * - HTTP Status: 200 OK
     * - Response body contains:
     *   - status: "success"
     *   - message: "No profiles found"
     *   - data: Empty array []
     */
    @Test
    void testListMetricProfiles_EmptyList() {
        // Arrange - Mock KruizeClient to throw 400 exception (Kruize returns error for empty list)
        Response mockResponse = Response.status(400)
                .entity(emptyMetricProfilesResponse)
                .build();
        ClientWebApplicationException exception = new ClientWebApplicationException(mockResponse);
        when(kruizeClient.getMetricProfiles(true)).thenThrow(exception);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/metricProfiles/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"))
            .body("message", equalTo("No profiles found"))
            .body("data", hasSize(0));
        
        verify(kruizeClient, times(1)).getMetricProfiles(true);
    }

    /**
     * Test metric profiles list endpoint when service throws an exception
     *
     * Test Description: Verifies that when the Kruize service throws an unexpected exception,
     * the optimizer service returns a proper error response.
     *
     * Expected Output:
     * - HTTP Status: 500 Internal Server Error
     * - Response body contains:
     *   - status: "error"
     *   - message: Contains "Error fetching"
     */
    @Test
    void testListMetricProfiles_ServiceException() {
        // Arrange - Mock KruizeClient to throw exception
        when(kruizeClient.getMetricProfiles(true)).thenThrow(new RuntimeException("Service error"));
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/metricProfiles/list")
            .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("status", equalTo("error"))
            .body("message", containsString("Error fetching"));
        
        verify(kruizeClient, times(1)).getMetricProfiles(true);
    }

    /**
     * Test metric profiles list response structure
     *
     * Test Description: Verifies that the response from the metric profiles list endpoint
     * contains all required fields with proper structure.
     *
     * Expected Output:
     * - HTTP Status: 200 OK
     * - Response body must contain keys: status, message, data
     * - All fields must be non-null
     */
    @Test
    void testListMetricProfiles_VerifyResponseStructure() {
        // Arrange
        when(kruizeClient.getMetricProfiles(true)).thenReturn(mockMetricProfilesList);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/metricProfiles/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasKey("status"))
            .body("$", hasKey("message"))
            .body("$", hasKey("data"))
            .body("status", notNullValue())
            .body("message", notNullValue())
            .body("data", notNullValue());
        
        verify(kruizeClient, times(1)).getMetricProfiles(true);
    }
}
