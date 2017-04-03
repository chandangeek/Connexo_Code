/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionBusinessProcessInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionBusinessProcessInfoFactory;

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

public class TransitionBusinessProcessResource {

    private final FiniteStateMachineService finiteStateMachineService;
    private final TransitionBusinessProcessInfoFactory transitionBusinessProcessInfoFactory;
    private final BpmService bpmService;
    private static final String DEVICE_ASSOCIATION = "device";
    private static final String APP_KEY = "mdc";
    private static final String PROCESS_KEY_DEVICE_STATES = "deviceStates";

    @Inject
    public TransitionBusinessProcessResource(FiniteStateMachineService finiteStateMachineService,
                                             TransitionBusinessProcessInfoFactory transitionBusinessProcessInfoFactory,
                                             BpmService bpmService) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.transitionBusinessProcessInfoFactory = transitionBusinessProcessInfoFactory;
        this.bpmService = bpmService;
    }

    @GET @Transactional
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getAvailableStateChangeProcesses(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParams){
        //noinspection unchecked
        List<TransitionBusinessProcessInfo> activeProcesses = bpmService
                .getActiveBpmProcessDefinitions(APP_KEY)
                .stream()
                .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals(DEVICE_ASSOCIATION))
                .filter(f -> List.class.isInstance(f.getProperties().get(PROCESS_KEY_DEVICE_STATES)))
                .filter(s -> ((List<Object>) s.getProperties().get(PROCESS_KEY_DEVICE_STATES))
                        .stream()
                        .filter(HasIdAndName.class::isInstance)
                        .anyMatch(v -> uriInfo.getQueryParameters().getFirst("stateId").equals("") || ((HasIdAndName) v).getId()
                                .toString()
                                .equals(uriInfo.getQueryParameters().getFirst("stateId"))))
                .map(transitionBusinessProcessInfoFactory::from)
                .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
                .collect(Collectors.toList());

        return Response.ok(PagedInfoList.fromCompleteList("stateChangeBusinessProcesses", activeProcesses, queryParams)).build();
    }
}
