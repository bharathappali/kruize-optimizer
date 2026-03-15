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

import com.kruize.optimizer.model.api.ApiResponse;
import com.kruize.optimizer.model.kruize.KruizeStatus;
import com.kruize.optimizer.service.StatusService;
import com.kruize.optimizer.utils.OptimizerConstants.OptimizerApiConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * REST resource for centralized Kruize system status
 */
@Path(OptimizerApiConstants.KRUIZE_BASE_PATH + OptimizerApiConstants.STATUS_PATH)
public class StatusResource {

    private static final Logger LOG = Logger.getLogger(StatusResource.class);

    @Inject
    StatusService statusService;

    /**
     * Get comprehensive Kruize system status
     * GET /kruize/status
     *
     * @return Response with system status including datasources, profiles, and alerts
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        try {
            LOG.info("Fetching Kruize system status");
            KruizeStatus status = statusService.getSystemStatus();
            
            return Response.ok(ApiResponse.success(
                    "Kruize system status retrieved successfully",
                    status
            )).build();
            
        } catch (Exception e) {
            LOG.error("Error fetching system status", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Error fetching system status: " + e.getMessage()))
                    .build();
        }
    }
}
