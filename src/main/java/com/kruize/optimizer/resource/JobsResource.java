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
import com.kruize.optimizer.model.kruize.JobsOverview;
import com.kruize.optimizer.service.JobsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * REST resource for bulk jobs operations
 */
@Path("/jobs")
public class JobsResource {

    private static final Logger LOG = Logger.getLogger(JobsResource.class);

    @Inject
    JobsService jobsService;

    /**
     * Get overview of bulk jobs statistics
     * GET /jobs/overview
     *
     * @return Response with jobs overview
     */
    @GET
    @Path("/overview")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobsOverview() {
        try {
            LOG.info("Fetching jobs overview");
            
            JobsOverview overview = jobsService.getJobsOverview();
            
            return Response.ok(ApiResponse.success(
                    "Jobs overview fetched successfully",
                    overview
            )).build();
            
        } catch (Exception e) {
            LOG.error("Error fetching jobs overview", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Error fetching jobs overview"))
                    .build();
        }
    }
}

