package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionEndPointConfigurationInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionEndPointConfigurationInfoFactory;

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
import java.util.Set;
import java.util.stream.Collectors;

public class TransitionEndPointConfigurationResource {

    private final EndPointConfigurationService endPointConfigurationService;
    private final TransitionEndPointConfigurationInfoFactory transitionEndPointConfigurationInfoFactory;
    private final FiniteStateMachineService finiteStateMachineService;

    @Inject
    public TransitionEndPointConfigurationResource(EndPointConfigurationService endPointConfigurationService,
                                                   TransitionEndPointConfigurationInfoFactory transitionEndPointConfigurationInfoFactory,
                                                   FiniteStateMachineService finiteStateMachineService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.transitionEndPointConfigurationInfoFactory = transitionEndPointConfigurationInfoFactory;
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @GET
    @Transactional
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableStateChangeEndPointConfigurations(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParams) {
        Set<String> serviceNames = finiteStateMachineService.getStateTransitionWebServiceClients()
                .stream()
                .map(StateTransitionWebServiceClient::getWebServiceName)
                .collect(Collectors.toSet());

        List<TransitionEndPointConfigurationInfo> transitionEndPointConfigurationInfoList = endPointConfigurationService.findEndPointConfigurations(serviceNames).stream()
                .filter(EndPointConfiguration::isActive)
                .filter(outbound -> !outbound.isInbound())
                .map(transitionEndPointConfigurationInfoFactory::from)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("stateChangeEndPointConfigurations", transitionEndPointConfigurationInfoList, queryParams)).build();
    }
}
