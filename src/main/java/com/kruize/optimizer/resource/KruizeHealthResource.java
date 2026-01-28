package com.kruize.optimizer.resource;

import com.kruize.optimizer.model.HealthCheckResult;
import com.kruize.optimizer.service.KruizeHealthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/health/kruize")
public class KruizeHealthResource {

    @Inject
    KruizeHealthService healthService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HealthCheckResult healthCheck() {
        return healthService.performHealthCheck(false);
    }
}
