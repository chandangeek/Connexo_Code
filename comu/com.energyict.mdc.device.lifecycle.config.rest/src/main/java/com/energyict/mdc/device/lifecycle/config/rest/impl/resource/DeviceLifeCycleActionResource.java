/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.TransitionType;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedActionChangeRequest;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedActionRequestFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfoFactory;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceLifeCycleActionResource {

    private final ResourceHelper resourceHelper;
    private final AuthorizedActionInfoFactory authorizedActionInfoFactory;
    private final MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory;

    @Inject
    public DeviceLifeCycleActionResource(
            ResourceHelper resourceHelper,
            AuthorizedActionInfoFactory authorizedActionInfoFactory,
            MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.authorizedActionInfoFactory = authorizedActionInfoFactory;
        this.microActionAndCheckInfoFactory = microActionAndCheckInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getActionsForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        List<AuthorizedActionInfo> transitions = deviceLifeCycle.getAuthorizedActions()
                .stream()
                .map(authorizedActionInfoFactory::from)
                .sorted((t1, t2) -> t1.name.compareToIgnoreCase(t2.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleActions", ListPager.of(transitions).from(queryParams).find(), queryParams);
    }

    @GET
    @Transactional
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getAuthorizedActionById(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("actionId") Long actionId, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedAction action = resourceHelper.findAuthorizedActionByIdOrThrowException(deviceLifeCycle, actionId);
        return Response.ok(authorizedActionInfoFactory.from(action)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response addActionsForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, AuthorizedActionInfo newAction) {
        validateInfo(newAction);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedActionRequestFactory factory = new AuthorizedActionRequestFactory(this.resourceHelper);
        AuthorizedActionChangeRequest creationRequest = factory.from(deviceLifeCycle, newAction, AuthorizedActionRequestFactory.Operation.CREATE);
        AuthorizedAction authorizedAction = creationRequest.perform();
        return Response.ok(authorizedActionInfoFactory.from(authorizedAction)).build();
    }

    @PUT
    @Transactional
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response editAuthorizedAction(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, AuthorizedActionInfo info) {
        validateInfo(info);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedActionRequestFactory factory = new AuthorizedActionRequestFactory(this.resourceHelper);
        AuthorizedActionChangeRequest editRequest = factory.from(deviceLifeCycle, info, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction authorizedAction = editRequest.perform();
        return Response.ok(authorizedActionInfoFactory.from(authorizedAction)).build();
    }

    private void validateInfo(AuthorizedActionInfo actionForEdit) {
        Predicate<Long> check = id -> id != null && id > 0;
        new RestValidationBuilder()
                .notEmpty(actionForEdit.name, "name", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY)
                .notEmpty(actionForEdit.triggeredBy != null ? actionForEdit.triggeredBy.symbol : null, "triggeredBy", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY)
                .on(actionForEdit.fromState != null ? actionForEdit.fromState.id : null).field("fromState").check(check).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test()
                .on(actionForEdit.toState != null ? actionForEdit.toState.id : null).field("toState").check(check).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test()
                .validate();
    }

    @DELETE
    @Transactional
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response deleteAuthorizedAction(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId,
                                           @PathParam("actionId") Long actionId,
                                           @BeanParam JsonQueryParameters queryParams,
                                           AuthorizedActionInfo info) {
        info.id = actionId;
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedActionRequestFactory factory = new AuthorizedActionRequestFactory(this.resourceHelper);
        AuthorizedActionChangeRequest deleteRequest = factory.from(deviceLifeCycle, info, AuthorizedActionRequestFactory.Operation.DELETE);
        AuthorizedAction authorizedAction = deleteRequest.perform();
        return Response.ok(authorizedActionInfoFactory.from(authorizedAction)).build();
    }

    @GET
    @Transactional
    @Path("/microactions")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getAvailableMicroActionsForTransition(
            @PathParam("deviceLifeCycleId") Long deviceLifeCycleId,
            @QueryParam("fromState") long fromStateId,
            @QueryParam("toState") long toStateId,
            @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        Optional<TransitionType> defaultTransition = getDefaultTransition(deviceLifeCycle, fromStateId, toStateId);
        List<MicroActionAndCheckInfo> microActions = new ArrayList<>();
        if (defaultTransition.isPresent()) {
            defaultTransition.get().optionalActions()
                    .stream()
                    .map(microActionAndCheckInfoFactory::optional)
                    .forEach(microActions::add);
            defaultTransition.get().requiredActions()
                    .stream()
                    .map(microActionAndCheckInfoFactory::required)
                    .forEach(microActions::add);
        } else {
            Arrays.stream(MicroAction.values())
                    .map(microActionAndCheckInfoFactory::optional)
                    .forEach(microActions::add);
        }
        microActions.sort(Comparator.<MicroActionAndCheckInfo, String>comparing(obj -> obj.category.name)
                .thenComparing(obj -> obj.name));
        return Response.ok(PagedInfoList.fromCompleteList("microActions", microActions, queryParams)).build();
    }

    @GET
    @Transactional
    @Path("/microchecks")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getAvailableMicroChecksForTransition(
            @PathParam("deviceLifeCycleId") Long deviceLifeCycleId,
            @QueryParam("fromState") long fromStateId,
            @QueryParam("toState") long toStateId,
            @BeanParam JsonQueryParameters queryParams) {
        Set<MicroActionAndCheckInfo> microChecks = new TreeSet<>(Comparator.<MicroActionAndCheckInfo, String>comparing(obj -> obj.category.name)
                .thenComparing(obj -> obj.name));
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        Set<MicroCheck> allMicroChecks = resourceHelper.findAllAvailableMicroChecks();
        State fromState = getFromState(deviceLifeCycle, fromStateId);
        State toState = getToState(deviceLifeCycle, toStateId);
        if (getDefaultTransition(fromState, toState).isPresent()) {
            allMicroChecks
                    .stream()
                    .filter(microCheck -> microCheck.isOptionalForTransition(fromState, toState))
                    .map(microActionAndCheckInfoFactory::optional)
                    .forEach(microChecks::add);
            allMicroChecks
                    .stream()
                    .filter(microCheck -> microCheck.isRequiredForTransition(fromState, toState))
                    .map(microActionAndCheckInfoFactory::required)
                    .forEach(microChecks::add);
        } else {
            allMicroChecks
                    .stream()
                    .filter(microCheck -> !microCheck.getKey().equals("MetrologyConfigurationInCorrectStateIfAny"))
                    .map(microActionAndCheckInfoFactory::optional)
                    .forEach(microChecks::add);
        }
        return Response.ok(PagedInfoList.fromCompleteList("microChecks", new ArrayList<>(microChecks), queryParams)).build();
    }

    private State getFromState(DeviceLifeCycle deviceLifeCycle, long fromStateId) {
        return resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, fromStateId);
    }

    private State getToState(DeviceLifeCycle deviceLifeCycle, long toStateId) {
        return resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, toStateId);
    }

    private Optional<TransitionType> getDefaultTransition(DeviceLifeCycle deviceLifeCycle, long fromStateId, long toStateId) {
        State fromState = getFromState(deviceLifeCycle, fromStateId);
        State toState = getToState(deviceLifeCycle, toStateId);
        return getDefaultTransition(fromState, toState);
    }

    private Optional<TransitionType> getDefaultTransition(State fromState, State toState) {
        return TransitionType.from(fromState, toState);
    }
}
