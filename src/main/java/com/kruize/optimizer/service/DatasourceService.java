package com.kruize.optimizer.service;

import com.kruize.optimizer.client.KruizeClient;
import com.kruize.optimizer.model.Datasource;
import com.kruize.optimizer.model.DatasourceListResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DatasourceService {

    private static final Logger LOG = Logger.getLogger(DatasourceService.class);

    @Inject
    @RestClient
    KruizeClient kruizeClient;

    /**
     * Checks for connected datasources.
     * 
     * @return List of datasource names if any are connected, otherwise empty list.
     */
    public List<String> getConnectedDatasources() {
        try {
            DatasourceListResponse response = kruizeClient.getDatasources();
            List<Datasource> datasources = response.getDatasources();
            if (datasources != null && !datasources.isEmpty()) {
                LOG.infof("Found %d connected datasources.", datasources.size());
                return datasources.stream()
                        .map(Datasource::getName)
                        .collect(Collectors.toList());
            } else {
                LOG.info("No connected datasources found.");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            LOG.error("Error fetching datasources from Kruize: " + e.getMessage());
            // Depending on requirements, we might want to throw or return empty.
            // Returning empty list for now to signify "none connected/reachable".
            return Collections.emptyList();
        }
    }
}
