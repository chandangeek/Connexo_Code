/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.Constants;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionBusinessProcessInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.TransitionEndPointConfigurationInfo;

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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeviceLifeCycleStateResource {
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleStateFactory deviceLifeCycleStateFactory;
    private final AuthorizedActionInfoFactory authorizedActionInfoFactory;
    private final ResourceHelper resourceHelper;
    private final BpmService bpmService;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public DeviceLifeCycleStateResource(
            ExceptionFactory exceptionFactory,
            DeviceLifeCycleStateFactory deviceLifeCycleStateFactory,
            AuthorizedActionInfoFactory authorizedActionInfoFactory,
            ResourceHelper resourceHelper,
            BpmService bpmService,
            EndPointConfigurationService endPointConfigurationService) {
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleStateFactory = deviceLifeCycleStateFactory;
        this.authorizedActionInfoFactory = authorizedActionInfoFactory;
        this.resourceHelper = resourceHelper;
        this.bpmService = bpmService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Constants.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getStatesForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        List<DeviceLifeCycleStateInfo> states = deviceLifeCycle.getFiniteStateMachine().getStates()
                .stream()
                .map(state -> deviceLifeCycleStateFactory.from(deviceLifeCycle, state))
                .sorted((st1, st2) -> st1.name.compareToIgnoreCase(st2.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleStates", ListPager.of(states).from(queryParams).find(), queryParams);
    }

    @GET
    @Transactional
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getStateById(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        State state = resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, stateId);
        return Response.ok(deviceLifeCycleStateFactory.from(deviceLifeCycle, state)).build();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response addDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, DeviceLifeCycleStateInfo stateInfo) {
        validateInfo(stateInfo);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        StageSet stageSet = deviceLifeCycle.getFiniteStateMachine().getStageSet().orElseThrow(getDefaultStageSetException());
        Stage stage = stageSet.getStageByName((String) stateInfo.stage.id).orElseThrow(getDefaultStageSetException());
        FiniteStateMachineUpdater.StateBuilder stateUpdater = fsmUpdater.newCustomState(stateInfo.name, stage);
        stateInfo.onEntry.stream().map(this::findBpmBusinessProcess).forEach(stateUpdater::onEntry);
        stateInfo.onExit.stream().map(this::findBpmBusinessProcess).forEach(stateUpdater::onExit);
        stateInfo.onEntryEndPointConfigurations.stream().map(this::findEndPointConfiguration).forEach(stateUpdater::onEntry);
        stateInfo.onExitEndPointConfigurations.stream().map(this::findEndPointConfiguration).forEach(stateUpdater::onExit);

        State newState = stateUpdater.complete();
        boolean firstState = deviceLifeCycle.getFiniteStateMachine().getStates().isEmpty();
        if (firstState) {
            fsmUpdater.complete(newState);
        } else {
            fsmUpdater.complete();
        }
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleStateFactory.from(deviceLifeCycle, newState)).build();
    }

    private Supplier<IllegalStateException> getDefaultStageSetException() {
        return () -> new IllegalStateException("Default stage set not installed correctly");
    }

    @PUT
    @Transactional
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response editDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, DeviceLifeCycleStateInfo info) {
        validateInfo(info);
        info.id = stateId;
        State stateForEdit = resourceHelper.lockStateOrThrowException(info);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);

        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        FiniteStateMachineUpdater.StateUpdater stateUpdater = fsmUpdater.state(stateId);
        if (stateForEdit.isCustom()) {
            stateUpdater.setName(info.name);
        }
        StageSet stageSet = deviceLifeCycle.getFiniteStateMachine().getStageSet().orElseThrow(getDefaultStageSetException());
        Stage stage = stageSet.getStageByName((String) info.stage.id).orElseThrow(getDefaultStageSetException());
        stateUpdater.stage(stage);

        info.onEntry.stream().map(this::findBpmBusinessProcess).forEach(stateUpdater::onEntry);
        info.onExit.stream().map(this::findBpmBusinessProcess).forEach(stateUpdater::onExit);
        // remove 'obsolete' onEntry processes:
        stateForEdit.getOnEntryProcesses().stream()
                .map(ProcessReference::getStateChangeBusinessProcess)
                .filter(x -> isObsoleteBpmBusinessProcess(info.onEntry, x))
                .forEach(stateUpdater::removeOnEntry);
        //remove 'obsolete' onExit processes
        stateForEdit.getOnExitProcesses().stream()
                .map(ProcessReference::getStateChangeBusinessProcess)
                .filter(x -> isObsoleteBpmBusinessProcess(info.onExit, x))
                .forEach(stateUpdater::removeOnExit);

        info.onEntryEndPointConfigurations.stream().map(this::findEndPointConfiguration).forEach(stateUpdater::onEntry);
        info.onExitEndPointConfigurations.stream().map(this::findEndPointConfiguration).forEach(stateUpdater::onExit);
        // remove 'obsolete' onEntry end point configurations:
        stateForEdit.getOnEntryEndPointConfigurations().stream()
                .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                .filter(x -> isObsoleteEndPointConfigurations(info.onEntryEndPointConfigurations, x))
                .forEach(stateUpdater::removeOnEntry);
        //remove 'obsolete' onExit end point configurations
        stateForEdit.getOnExitEndPointConfigurations().stream()
                .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                .filter(x -> isObsoleteEndPointConfigurations(info.onExitEndPointConfigurations, x))
                .forEach(stateUpdater::removeOnExit);

        State stateAfterEdit = stateUpdater.complete();
        fsmUpdater.complete();
        deviceLifeCycle.save(); // increase parent version
        return Response.ok(deviceLifeCycleStateFactory.from(deviceLifeCycle, stateAfterEdit)).build();
    }

    private BpmProcessDefinition findBpmBusinessProcess(TransitionBusinessProcessInfo businessProcessInfo) {
        Optional<BpmProcessDefinition> process = bpmService.findBpmProcessDefinition(businessProcessInfo.id);
        if (!process.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.STATE_CHANGE_BUSINESS_PROCESS_NOT_FOUND, businessProcessInfo.id);
        }
        return process.get();
    }

    private boolean isObsoleteBpmBusinessProcess(List<TransitionBusinessProcessInfo> transitionBussinessProcessInfos, BpmProcessDefinition bpmProcessDefinition) {
        return transitionBussinessProcessInfos.stream().noneMatch(x -> x.id == bpmProcessDefinition.getId());
    }

    private EndPointConfiguration findEndPointConfiguration(TransitionEndPointConfigurationInfo endPointConfigurationInfo) {
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService
                .findAndLockEndPointConfigurationByIdAndVersion(endPointConfigurationInfo.id, endPointConfigurationInfo.version);
        return endPointConfiguration.orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.STATE_CHANGE_ENDPOINT_CONFIGURATION_NOT_FOUND, endPointConfigurationInfo.id));
    }

    private boolean isObsoleteEndPointConfigurations(List<TransitionEndPointConfigurationInfo> endPointConfigurationInfos, EndPointConfiguration endPointConfiguration) {
        return endPointConfigurationInfos.stream().noneMatch(x -> x.id == endPointConfiguration.getId());
    }

    @PUT
    @Transactional
    @Path("/{stateId}/status")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response setInitialDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, DeviceLifeCycleStateInfo info) {
        info.id = stateId;
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        State stateForEdit = resourceHelper.lockStateOrThrowException(info);
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        FiniteStateMachineUpdater fsmUpdater = finiteStateMachine.startUpdate();
        fsmUpdater.complete(stateForEdit);
        deviceLifeCycle.save(); // increase parent version
        return Response.ok(deviceLifeCycleStateFactory.from(deviceLifeCycle, stateForEdit)).build();
    }

    @DELETE
    @Transactional
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response deleteDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, DeviceLifeCycleStateInfo info) {
        State stateForDeletion = resourceHelper.lockStateOrThrowException(info);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        resourceHelper.checkDeviceLifeCycleUsages(deviceLifeCycle);
        checkStateHasTransitions(deviceLifeCycle, stateForDeletion);
        checkStateIsTheLatest(deviceLifeCycle);
        checkStateIsInitial(stateForDeletion);
        deviceLifeCycle.getFiniteStateMachine().startUpdate().removeState(stateForDeletion).complete();
        deviceLifeCycle.save(); // increase parent version
        return Response.ok(deviceLifeCycleStateFactory.from(deviceLifeCycle, stateForDeletion)).build();
    }

    List<DeviceLifeCycleStateInfo> getAllStatesForDeviceLifecycle(long deviceLifeCycleId) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        return deviceLifeCycle.getFiniteStateMachine().getStates()
                .stream()
                .map(state -> deviceLifeCycleStateFactory.from(deviceLifeCycle, state))
                .sorted((st1, st2) -> st1.name.compareToIgnoreCase(st2.name)) // alphabetical sort
                .collect(Collectors.toList());
    }

    private void checkStateHasTransitions(DeviceLifeCycle deviceLifeCycle, State stateForDeletion) {
        List<Long> transitionIds = deviceLifeCycle.getFiniteStateMachine().getTransitions().stream()
                .filter(transition -> transition.getFrom().getId() == stateForDeletion.getId()
                        || transition.getTo().getId() == stateForDeletion.getId())
                .map(StateTransition::getId)
                .collect(Collectors.toList());
        if (!transitionIds.isEmpty()) {
            String transitionNames = deviceLifeCycle.getAuthorizedActions().stream()
                    .filter(aa -> aa instanceof AuthorizedTransitionAction)
                    .filter(aa -> transitionIds.contains(((AuthorizedTransitionAction) aa).getStateTransition().getId()))
                    .map(authorizedActionInfoFactory::from)
                    .map(aai -> aai.name)
                    .filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                    .collect(Collectors.joining(", "));
            throw exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_IS_STILL_USED_BY_TRANSITIONS, transitionNames);
        }
    }

    private void checkStateIsTheLatest(DeviceLifeCycle deviceLifeCycle) {
        if (deviceLifeCycle.getFiniteStateMachine().getStates().size() == 1) {
            throw exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_IS_THE_LATEST_STATE);
        }
    }

    private void checkStateIsInitial(State stateForDeletion) {
        if (stateForDeletion.isInitial()) {
            throw exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_IS_THE_INITIAL_STATE);
        }
    }

    private void validateInfo(DeviceLifeCycleStateInfo stateInfo) {
        new RestValidationBuilder()
                .notEmpty(stateInfo.name, "name", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY)
                .notEmpty(stateInfo.stage != null ? stateInfo.stage.id : stateInfo.stage, "stage", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY)
                .validate();
    }
}