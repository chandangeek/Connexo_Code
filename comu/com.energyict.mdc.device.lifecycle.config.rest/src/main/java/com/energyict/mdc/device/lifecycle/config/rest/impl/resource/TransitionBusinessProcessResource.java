/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionBusinessProcessInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

public class TransitionBusinessProcessResource {

    private final FiniteStateMachineService finiteStateMachineService;
    private final TransitionBusinessProcessInfoFactory transitionBusinessProcessInfoFactory;

    @Inject
    public TransitionBusinessProcessResource(FiniteStateMachineService finiteStateMachineService,
                                             TransitionBusinessProcessInfoFactory transitionBusinessProcessInfoFactory) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.transitionBusinessProcessInfoFactory = transitionBusinessProcessInfoFactory;
    }

    @GET @Transactional
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getAvailableStateChangeProcesses(@BeanParam JsonQueryParameters queryParams){
        return Response.ok(PagedInfoList.fromCompleteList("stateChangeBusinessProcesses",
                finiteStateMachineService.findStateChangeBusinessProcesses().stream().map(transitionBusinessProcessInfoFactory::from).collect(Collectors.toList()),
                queryParams)).build();
    }
}
