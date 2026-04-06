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
 * Integration tests for LayerResource using mock JSON responses
 */
@QuarkusTest
class LayerResourceTest {
    
    @InjectMock
    BulkSchedulerService bulkSchedulerService;
    
    @InjectMock
    KruizeStateService kruizeStateService;
    
    @InjectMock
    @RestClient
    KruizeClient kruizeClient;

    private List<KruizeProfile> mockLayersList;
    private String emptyLayersResponse;

    @BeforeEach
    void setUp() throws IOException {
        Mockito.reset(bulkSchedulerService, kruizeStateService, kruizeClient);
        
        // Mock the initialization to prevent startup from connecting to real Kruize
        doNothing().when(bulkSchedulerService).initialize();
        doNothing().when(kruizeStateService).refreshStateAndInstallProfiles();
        
        // Load mock responses from JSON files
        mockLayersList = MockResponseLoader.loadMockResponse("layer_list.json", new TypeReference<List<KruizeProfile>>() {});
        emptyLayersResponse = MockResponseLoader.loadMockResponseAsString("empty_layer_list.json");
    }

    /**
     * Test successful retrieval of layers list
     *
     * Test Description: Verifies that the layers list endpoint returns all available layers
     * when Kruize service has layers configured.
     *
     * Expected Output:
     * - HTTP Status: 200 OK
     * - Response body contains:
     *   - status: "success"
     *   - message: "Profiles fetched successfully"
     *   - data: Array of 4 layers (container, semeru, hotspot, quarkus)
     */
    @Test
    void testListLayers_Success() {
        // Arrange - Mock KruizeClient to return the JSON response
        when(kruizeClient.getLayers()).thenReturn(mockLayersList);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/layers/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"))
            .body("message", equalTo("Profiles fetched successfully"))
            .body("data", hasSize(4))
            .body("data[0].name", equalTo("container"))
            .body("data[1].name", equalTo("semeru"))
            .body("data[2].name", equalTo("hotspot"))
            .body("data[3].name", equalTo("quarkus"));
        
        verify(kruizeClient, times(1)).getLayers();
    }

    /**
     * Test layers list endpoint when no layers are found
     *
     * Test Description: Verifies that when Kruize returns a 400 error indicating no layers
     * are found, the optimizer service handles it gracefully and returns an empty list with
     * a success status.
     *
     * Mock Response (from empty_layer_list.json):
     * - Kruize returns: {"message": "No layers found!", "httpcode": 400, "status": "ERROR"}
     *
     * Expected Output:
     * - HTTP Status: 200 OK
     * - Response body contains:
     *   - status: "success"
     *   - message: "No profiles found"
     *   - data: Empty array []
     */
    @Test
    void testListLayers_EmptyList() {
        // Arrange - Mock KruizeClient to throw 400 exception (Kruize returns error for empty list)
        Response mockResponse = Response.status(400)
                .entity(emptyLayersResponse)
                .build();
        ClientWebApplicationException exception = new ClientWebApplicationException(mockResponse);
        when(kruizeClient.getLayers()).thenThrow(exception);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/layers/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"))
            .body("message", equalTo("No profiles found"))
            .body("data", hasSize(0));
        
        verify(kruizeClient, times(1)).getLayers();
    }

    /**
     * Test layers list endpoint when service throws an exception
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
    void testListLayers_ServiceException() {
        // Arrange - Mock KruizeClient to throw exception
        when(kruizeClient.getLayers()).thenThrow(new RuntimeException("Service error"));
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/layers/list")
            .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("status", equalTo("error"))
            .body("message", containsString("Error fetching"));
        
        verify(kruizeClient, times(1)).getLayers();
    }

    /**
     * Test layers list response structure
     *
     * Test Description: Verifies that the response from the layers list endpoint
     * contains all required fields with proper structure.
     *
     * Expected Output:
     * - HTTP Status: 200 OK
     * - Response body must contain keys: status, message, data
     * - All fields must be non-null
     */
    @Test
    void testListLayers_VerifyResponseStructure() {
        // Arrange
        when(kruizeClient.getLayers()).thenReturn(mockLayersList);
        
        // Act & Assert
        given()
            .when()
            .get("/kruize/layers/list")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasKey("status"))
            .body("$", hasKey("message"))
            .body("$", hasKey("data"))
            .body("status", notNullValue())
            .body("message", notNullValue())
            .body("data", notNullValue());
        
        verify(kruizeClient, times(1)).getLayers();
    }
}
