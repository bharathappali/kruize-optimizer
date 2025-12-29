package com.kruize.optimizer.resource;

import com.kruize.optimizer.service.DatasourceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/datasources")
public class DatasourceResource {

    @Inject
    DatasourceService datasourceService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listConnectedDatasources() {
        return datasourceService.getConnectedDatasources();
    }
}
