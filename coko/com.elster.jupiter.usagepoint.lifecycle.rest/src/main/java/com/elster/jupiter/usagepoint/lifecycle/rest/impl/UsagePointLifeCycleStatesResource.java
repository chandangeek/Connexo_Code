/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.Privileges;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class UsagePointLifeCycleStatesResource {
    private final ResourceHelper resourceHelper;
    private final UsagePointLifeCycleStateInfoFactory stateInfoFactory;


    @Inject
    public UsagePointLifeCycleStatesResource(ResourceHelper resourceHelper,
                                             UsagePointLifeCycleStateInfoFactory stateInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.stateInfoFactory = stateInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllStates(@PathParam("lid") long lifeCycleId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId);
        List<UsagePointLifeCycleStateInfo> states = lifeCycle.getStates()
                .stream()
                .map(this.stateInfoFactory::fullInfo)
                .sorted((st1, st2) -> st1.name.compareToIgnoreCase(st2.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("states", states, queryParameters);
    }

    @GET
    @Path("/{sid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleStateInfo getStateById(@PathParam("sid") long stateId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePointState state = this.resourceHelper.getStateByIdOrThrowException(stateId);
        return this.stateInfoFactory.fullInfo(state);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleStateInfo newState(@PathParam("lid") long lifeCycleId, UsagePointLifeCycleStateInfo stateInfo) {
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validationBuilder.notEmpty(stateInfo.name, "name", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY)
                .notEmpty(stateInfo.stage, "stage", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY)
                .validate();
        UsagePointLifeCycle lifeCycle = this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId);
        UsagePointState.UsagePointStateCreator builder = lifeCycle.newState(stateInfo.name);
        stateInfo.onEntry.stream().map(this.resourceHelper::getBpmProcessOrThrowException).forEach(builder::onEntry);
        stateInfo.onExit.stream().map(this.resourceHelper::getBpmProcessOrThrowException).forEach(builder::onExit);
        builder.setStage(stateInfo.stage);
        return this.stateInfoFactory.fullInfo(builder.complete());
    }

    @PUT
    @Path("/{sid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleStateInfo editState(UsagePointLifeCycleStateInfo stateInfo) {
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validationBuilder.notEmpty(stateInfo.name, "name")
                .notEmpty(stateInfo.stage, "stage")
                .validate();
        UsagePointState state = this.resourceHelper.lockState(stateInfo);
        UsagePointState.UsagePointStateUpdater builder = state.startUpdate().setName(stateInfo.name);
        state.getOnEntryProcesses().stream().map(ProcessReference::getStateChangeBusinessProcess).forEach(builder::removeOnEntry);
        state.getOnExitProcesses().stream().map(ProcessReference::getStateChangeBusinessProcess).forEach(builder::removeOnExit);
        stateInfo.onEntry.stream().map(this.resourceHelper::getBpmProcessOrThrowException).forEach(builder::onEntry);
        stateInfo.onExit.stream().map(this.resourceHelper::getBpmProcessOrThrowException).forEach(builder::onExit);
        builder.setStage(stateInfo.stage);
        return this.stateInfoFactory.fullInfo(builder.complete());
    }

    @PUT
    @Path("/{sid}/status")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleStateInfo setInitialState(UsagePointLifeCycleStateInfo stateInfo) {
        UsagePointState state = this.resourceHelper.lockState(stateInfo);
        state = state.startUpdate().setInitial().complete();
        return this.stateInfoFactory.fullInfo(state);
    }

    @DELETE
    @Path("/{sid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public Response removeState(UsagePointLifeCycleStateInfo stateInfo) {
        this.resourceHelper.lockState(stateInfo).remove();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
