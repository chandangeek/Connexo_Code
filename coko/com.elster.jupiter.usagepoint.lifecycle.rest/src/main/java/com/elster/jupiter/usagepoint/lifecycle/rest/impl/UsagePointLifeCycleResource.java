/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.Privileges;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.BusinessProcessInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.BusinessProcessInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.MicroActionAndCheckInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.MicroActionAndCheckInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCyclePrivilegeInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCyclePrivilegeInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStageInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/lifecycle")
public class UsagePointLifeCycleResource {
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final Provider<UsagePointLifeCycleStatesResource> statesResourceProvider;
    private final Provider<UsagePointLifeCycleTransitionsResource> transitionsResourceProvider;
    private final FiniteStateMachineService finiteStateMachineService;
    private final BusinessProcessInfoFactory bpmFactory;
    private final UsagePointLifeCycleInfoFactory lifeCycleInfoFactory;
    private final UsagePointLifeCyclePrivilegeInfoFactory privilegeInfoFactory;
    private final MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory;
    private final ResourceHelper resourceHelper;
    private final UsagePointLifeCycleStageInfoFactory stageInfoFactory;

    @Inject
    public UsagePointLifeCycleResource(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                                       Provider<UsagePointLifeCycleStatesResource> statesResourceProvider,
                                       Provider<UsagePointLifeCycleTransitionsResource> transitionsResourceProvider,
                                       FiniteStateMachineService finiteStateMachineService,
                                       BusinessProcessInfoFactory bpmFactory,
                                       UsagePointLifeCycleInfoFactory lifeCycleInfoFactory,
                                       UsagePointLifeCyclePrivilegeInfoFactory privilegeInfoFactory,
                                       MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory,
                                       ResourceHelper resourceHelper,
                                       UsagePointLifeCycleStageInfoFactory stageInfoFactory) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.statesResourceProvider = statesResourceProvider;
        this.transitionsResourceProvider = transitionsResourceProvider;
        this.finiteStateMachineService = finiteStateMachineService;
        this.bpmFactory = bpmFactory;
        this.lifeCycleInfoFactory = lifeCycleInfoFactory;
        this.privilegeInfoFactory = privilegeInfoFactory;
        this.microActionAndCheckInfoFactory = microActionAndCheckInfoFactory;
        this.resourceHelper = resourceHelper;
        this.stageInfoFactory = stageInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllLifeCycles(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointLifeCycleInfo> lifeCycles = this.usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()
                .from(queryParameters)
                .find()
                .stream()
                .map(this.lifeCycleInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("lifeCycles", lifeCycles, queryParameters);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo newLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(lifeCycleInfo.name);
        return this.lifeCycleInfoFactory.from(lifeCycle);
    }

    @GET
    @Path("/{lid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo getLifeCycleById(@PathParam("lid") long lifeCycleId) {
        return this.lifeCycleInfoFactory.from(this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId));
    }

    @DELETE
    @Path("/{lid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public Response deleteLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.lockLifeCycle(lifeCycleInfo);
        lifeCycle.remove();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/{lid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo updateLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.lockLifeCycle(lifeCycleInfo);
        lifeCycle.setName(lifeCycleInfo.name);
        lifeCycle.save();
        return this.lifeCycleInfoFactory.from(lifeCycle);
    }

    @PUT
    @Path("/{lid}/default")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo setLifeCycleDefault(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.lockLifeCycle(lifeCycleInfo);
        lifeCycle.markAsDefault();
        return this.lifeCycleInfoFactory.from(lifeCycle);
    }

    @POST
    @Path("/{lid}/clone")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo cloneLifeCycle(@PathParam("lid") long lifeCycleId, UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle source = this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId);
        return this.lifeCycleInfoFactory.from(this.usagePointLifeCycleConfigurationService.cloneUsagePointLifeCycle(lifeCycleInfo.name, source));
    }

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllProcesses(@BeanParam JsonQueryParameters queryParams) {
        List<BusinessProcessInfo> processes = this.finiteStateMachineService.findStateChangeBusinessProcesses().stream()
                .map(this.bpmFactory::from).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("processes", processes, queryParams);
    }

    @GET
    @Path("/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllPrivileges(@BeanParam JsonQueryParameters queryParams) {
        List<UsagePointLifeCyclePrivilegeInfo> privileges = Stream.of(UsagePointTransition.Level.values())
                .map(this.privilegeInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("privileges", privileges, queryParams);
    }

    @GET
    @Path("/microChecks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllMicroChecks(@BeanParam JsonQueryParameters queryParams,
                                           @QueryParam("fromState") long fromStateId,
                                           @QueryParam("toState") long toStateId) {
        UsagePointState fromState = this.resourceHelper.getStateByIdOrThrowException(fromStateId);
        UsagePointState toState = this.resourceHelper.getStateByIdOrThrowException(toStateId);
        List<MicroActionAndCheckInfo> privileges = this.usagePointLifeCycleConfigurationService.getMicroChecks()
                .stream()
                .map(check -> check.isMandatoryForTransition(fromState, toState)
                        ? this.microActionAndCheckInfoFactory.required(check)
                        : this.microActionAndCheckInfoFactory.optional(check))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("microChecks", privileges, queryParams);
    }

    @GET
    @Path("/microActions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllMicroActions(@BeanParam JsonQueryParameters queryParams,
                                            @QueryParam("fromState") long fromStateId,
                                            @QueryParam("toState") long toStateId) {
        UsagePointState fromState = this.resourceHelper.getStateByIdOrThrowException(fromStateId);
        UsagePointState toState = this.resourceHelper.getStateByIdOrThrowException(toStateId);
        List<MicroActionAndCheckInfo> privileges = this.usagePointLifeCycleConfigurationService.getMicroActions()
                .stream()
                .map(action -> action.isMandatoryForTransition(fromState, toState)
                        ? this.microActionAndCheckInfoFactory.required(action)
                        : this.microActionAndCheckInfoFactory.optional(action))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("microActions", privileges, queryParams);
    }

    @GET
    @Path("/stages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllStages(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> stages = usagePointLifeCycleConfigurationService.getStages().stream()
                .map(stageInfoFactory::from)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("stages", stages, queryParameters);
    }

    @Path("{lid}/states")
    public UsagePointLifeCycleStatesResource getStates() {
        return this.statesResourceProvider.get();
    }

    @Path("{lid}/transitions")
    public UsagePointLifeCycleTransitionsResource getTransitions() {
        return this.transitionsResourceProvider.get();
    }
}
