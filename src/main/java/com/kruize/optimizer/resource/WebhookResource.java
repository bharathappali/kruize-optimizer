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

import com.kruize.optimizer.model.WebhookPayload;
import com.kruize.optimizer.service.BulkSchedulerService;
import com.kruize.optimizer.utils.OptimizerConstants.MessageConstants;
import com.kruize.optimizer.utils.OptimizerConstants.OptimizerApiConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST resource for handling webhook callbacks from Kruize bulk API
 */
@Path(OptimizerApiConstants.WEBHOOK_PATH)
public class WebhookResource {

    private static final Logger LOG = Logger.getLogger(WebhookResource.class);

    @Inject
    BulkSchedulerService bulkSchedulerService;

    /**
     * Receive webhook callback from Kruize bulk API
     *
     * @param payload List of webhook payloads
     * @return HTTP response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveWebhook(List<WebhookPayload> payload) {
        LOG.debugf(MessageConstants.INFO_RECEIVED_WEBHOOK, payload != null ? payload.size() : 0);
        
        try {
            bulkSchedulerService.handleWebhook(payload);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error(MessageConstants.ERROR_PROCESSING_WEBHOOK, e);
            return Response.serverError().entity(String.format(MessageConstants.ERROR_PROCESSING_WEBHOOK_WITH_MESSAGE, e.getMessage())).build();
        }
    }
}

