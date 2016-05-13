package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationTestResource {

    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public MetrologyConfigurationTestResource(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @PUT
    @Path("/{id}/deactivate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response deactivate(@PathParam("id") Long id) {
        if(metrologyConfigurationService.findMetrologyConfiguration(id).isPresent()){
            metrologyConfigurationService.findMetrologyConfiguration(id).get().deactivate();
        }
        return Response.status(Response.Status.OK).build();
    }
}
