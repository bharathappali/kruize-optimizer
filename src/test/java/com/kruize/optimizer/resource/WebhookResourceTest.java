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
import com.kruize.optimizer.model.WebhookPayload;
import com.kruize.optimizer.service.BulkSchedulerService;
import com.kruize.optimizer.service.KruizeStateService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for WebhookResource
 */
@QuarkusTest
class WebhookResourceTest {

    @InjectMock
    BulkSchedulerService bulkSchedulerService;
    
    @InjectMock
    KruizeStateService kruizeStateService;
    
    @InjectMock
    @RestClient
    KruizeClient kruizeClient;

    @BeforeEach
    void setUp() {
        Mockito.reset(bulkSchedulerService, kruizeStateService, kruizeClient);
        
        // Mock the initialization to prevent startup from connecting to real Kruize
        doNothing().when(bulkSchedulerService).initialize();
        doNothing().when(kruizeStateService).refreshStateAndInstallProfiles();
    }

    private WebhookPayload createWebhookPayload(String jobId, String status, int total, int processed, int existing) {
        WebhookPayload payload = new WebhookPayload();
        WebhookPayload.Summary summary = new WebhookPayload.Summary();
        summary.setJobId(jobId);
        summary.setStatus(status);
        summary.setTotalExperiments(total);
        summary.setProcessedExperiments(processed);
        summary.setExistingExperiments(existing);
        payload.setSummary(summary);
        return payload;
    }

    @Test
    void testReceiveWebhook_Success() {
        // Arrange
        WebhookPayload payload = createWebhookPayload("job-123", "COMPLETED", 10, 8, 2);
        List<WebhookPayload> payloads = Collections.singletonList(payload);
        doNothing().when(bulkSchedulerService).handleWebhook(any());
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(payloads)
            .when()
            .post("/webhook")
            .then()
            .statusCode(200);
        
        verify(bulkSchedulerService, times(1)).handleWebhook(any());
    }

    @Test
    void testReceiveWebhook_MultiplePayloads() {
        // Arrange
        WebhookPayload payload1 = createWebhookPayload("job-123", "COMPLETED", 10, 8, 2);
        WebhookPayload payload2 = createWebhookPayload("job-124", "COMPLETED", 5, 5, 0);
        List<WebhookPayload> payloads = Arrays.asList(payload1, payload2);
        doNothing().when(bulkSchedulerService).handleWebhook(any());
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(payloads)
            .when()
            .post("/webhook")
            .then()
            .statusCode(200);
        
        verify(bulkSchedulerService, times(1)).handleWebhook(any());
    }


    @Test
    void testReceiveWebhook_InvalidJson() {
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json}")
            .when()
            .post("/webhook")
            .then()
            .statusCode(400);
        
        verify(bulkSchedulerService, never()).handleWebhook(any());
    }

    @Test
    void testReceiveWebhook_FailedStatus() {
        // Arrange
        WebhookPayload payload = createWebhookPayload("job-123", "FAILED", 0, 0, 0);
        List<WebhookPayload> payloads = Collections.singletonList(payload);
        doNothing().when(bulkSchedulerService).handleWebhook(any());
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(payloads)
            .when()
            .post("/webhook")
            .then()
            .statusCode(200);
        
        verify(bulkSchedulerService, times(1)).handleWebhook(any());
    }
}

