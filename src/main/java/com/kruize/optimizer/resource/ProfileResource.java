package com.kruize.optimizer.resource;

import com.kruize.optimizer.model.ProfileScanResult;
import com.kruize.optimizer.service.ProfileInstallerService;
import com.kruize.optimizer.service.ProfileScannerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/profiles")
public class ProfileResource {

    @Inject
    ProfileScannerService scannerService;

    @Inject
    ProfileInstallerService installerService;

    @GET
    @Path("/scan")
    @Produces(MediaType.APPLICATION_JSON)
    public ProfileScanResult scanProfiles() {
        return scannerService.scanKruizeProfiles();
    }

    @GET
    @Path("/diff")
    @Produces(MediaType.APPLICATION_JSON)
    public ProfileScanResult diffProfiles() {
        return scannerService.scanKruizeProfiles();
    }

    @POST
    @Path("/install")
    @Produces(MediaType.APPLICATION_JSON)
    public ProfileScanResult installProfiles() {
        return installerService.checkAndInstallProfiles();
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfiles(Map<String, List<String>> payload) {
        if (payload == null || !payload.containsKey("targets")) {
            return Response.status(400, "Missing 'targets' list in payload").build();
        }
        List<String> targets = payload.get("targets");
        installerService.updateProfiles(targets);
        return Response.ok("{\"message\": \"Update initiated associated profiles.\"}")
                .type(MediaType.APPLICATION_JSON).build();
    }
}
