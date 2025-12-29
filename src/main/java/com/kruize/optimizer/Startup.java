package com.kruize.optimizer;

import com.kruize.optimizer.service.KruizeHealthService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Executes initialization steps upon service startup.
 */
@ApplicationScoped
public class Startup {

    private static final Logger LOG = Logger.getLogger(Startup.class);

    @Inject
    KruizeHealthService healthService;

    /**
     * Executes at the end of application startup.
     * 
     * @param ev The Quarkus StartupEvent.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("##################################################");
        LOG.info("✅ Kruize Optimizer Service is STARTED!");

        LOG.info("Performing Startup Health Check & Repair...");
        healthService.performHealthCheck(true);

        LOG.info("   - Scans and Profiles functionality initialized.");
        LOG.info("##################################################");
    }
}