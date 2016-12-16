package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.Privileges;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleTransitionInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleTransitionInfoFactory;

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
import java.util.Set;
import java.util.stream.Collectors;

public class UsagePointLifeCycleTransitionsResource {
    private final UsagePointLifeCycleTransitionInfoFactory transitionInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointLifeCycleTransitionsResource(UsagePointLifeCycleTransitionInfoFactory transitionInfoFactory,
                                                  ResourceHelper resourceHelper) {
        this.transitionInfoFactory = transitionInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllTransitions(@PathParam("lid") long lifeCycleId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId);
        List<UsagePointLifeCycleTransitionInfo> transitions = lifeCycle.getTransitions()
                .stream()
                .map(this.transitionInfoFactory::fullInfo)
                .sorted((t1, t2) -> t1.name.compareToIgnoreCase(t2.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("transitions", transitions, queryParameters);
    }

    @GET
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleTransitionInfo getTransitionById(@PathParam("tid") long transitionId) {
        UsagePointTransition transition = this.resourceHelper.getTransitionByIdOrThrowException(transitionId);
        return this.transitionInfoFactory.fullInfo(transition);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleTransitionInfo newTransition(@PathParam("lid") long lifeCycleId, UsagePointLifeCycleTransitionInfo transitionInfo) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId);
        new RestValidationBuilder()
                .on(transitionInfo.name).check(name -> !name.isEmpty()).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).field("name").test()
                .on(transitionInfo.fromState.id).check(id -> id > 0).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).field("fromState").test()
                .on(transitionInfo.toState.id).check(id -> id > 0).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).field("toState").test()
                .validate();
        UsagePointState fromState = this.resourceHelper.getStateByIdOrThrowException(transitionInfo.fromState.id);
        UsagePointState toState = this.resourceHelper.getStateByIdOrThrowException(transitionInfo.toState.id);
        Set<UsagePointTransition.Level> levels = transitionInfo.privileges.stream().map(privilege -> privilege.privilege)
                .map(UsagePointTransition.Level::valueOf).collect(Collectors.toSet());
        Set<String> microChecks = transitionInfo.microChecks.stream()
                .filter(check -> check.checked)
                .map(check -> check.key)
                .collect(Collectors.toSet());
        Set<String> microActions = transitionInfo.microActions.stream()
                .filter(action -> action.checked)
                .map(action -> action.key)
                .collect(Collectors.toSet());
        UsagePointTransition transition = lifeCycle.newTransition(transitionInfo.name, fromState, toState)
                .withLevels(levels).withChecks(microChecks).withActions(microActions).complete();
        return this.transitionInfoFactory.fullInfo(transition);
    }

    @PUT
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleTransitionInfo editTransition(UsagePointLifeCycleTransitionInfo transitionInfo) {
        UsagePointTransition transition = this.resourceHelper.lockTransition(transitionInfo);
        new RestValidationBuilder()
                .on(transitionInfo.fromState.id).check(id -> id > 0).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).field("fromState").test()
                .on(transitionInfo.toState.id).check(id -> id > 0).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).field("toState").test()
                .validate();
        UsagePointState fromState = this.resourceHelper.getStateByIdOrThrowException(transitionInfo.fromState.id);
        UsagePointState toState = this.resourceHelper.getStateByIdOrThrowException(transitionInfo.toState.id);
        Set<UsagePointTransition.Level> levels = transitionInfo.privileges.stream().map(privilege -> privilege.privilege)
                .map(UsagePointTransition.Level::valueOf).collect(Collectors.toSet());
        Set<String> microChecks = transitionInfo.microChecks.stream()
                .filter(check -> check.checked)
                .map(check -> check.key)
                .collect(Collectors.toSet());
        Set<String> microActions = transitionInfo.microActions.stream()
                .filter(action -> action.checked)
                .map(action -> action.key)
                .collect(Collectors.toSet());
        transition.startUpdate().withName(transitionInfo.name).from(fromState).to(toState)
                .withLevels(levels).withChecks(microChecks).withActions(microActions).complete();
        return this.transitionInfoFactory.fullInfo(transition);
    }

    @DELETE
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public Response deleteTransition(UsagePointLifeCycleTransitionInfo transitionInfo) {
        UsagePointTransition transition = this.resourceHelper.lockTransition(transitionInfo);
        transition.remove();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
