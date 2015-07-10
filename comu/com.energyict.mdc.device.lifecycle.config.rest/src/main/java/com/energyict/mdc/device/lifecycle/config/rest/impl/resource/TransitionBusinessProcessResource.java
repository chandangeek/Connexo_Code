package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionBusinessProcessInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 10/07/2015
 * Time: 10:03
 */
public class TransitionBusinessProcessResource {

    private final FiniteStateMachineService finiteStateMachineService;
    private final TransitionBusinessProcessInfoFactory transitionBusinessProcessInfoFactory;

    @Inject
    public TransitionBusinessProcessResource(FiniteStateMachineService finiteStateMachineService,
                                             TransitionBusinessProcessInfoFactory transitionBusinessProcessInfoFactory) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.transitionBusinessProcessInfoFactory = transitionBusinessProcessInfoFactory;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getAvailableStateChangeProcesses(@BeanParam JsonQueryParameters queryParams){
        return Response.ok(PagedInfoList.fromCompleteList("stateChangeBusinessProcesses",
                finiteStateMachineService.findStateChangeBusinessProcesses().stream().map(transitionBusinessProcessInfoFactory::from).collect(Collectors.toList()),
                queryParams)).build();
    }
}
