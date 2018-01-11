package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

public class TransitionWebServiceEndpointResource {

    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public TransitionWebServiceEndpointResource(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @GET
    @Transactional
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableStateChangeEndpoints(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParams) {
        List<EndPointConfiguration> endPointConfigurations = endPointConfigurationService.findEndPointConfigurations().stream()
                .filter(EndPointConfiguration::isActive)
                .filter(outbound -> !outbound.isInbound())
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("stateChangeWebServiceEndpoints", endPointConfigurations, queryParams)).build();
    }
}