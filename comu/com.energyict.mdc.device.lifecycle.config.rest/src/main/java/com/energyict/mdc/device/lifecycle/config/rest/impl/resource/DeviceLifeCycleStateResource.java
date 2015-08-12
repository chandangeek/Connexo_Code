package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.*;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.info.*;

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
import java.util.*;
import java.util.stream.Collectors;

public class DeviceLifeCycleStateResource {
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final DeviceLifeCycleStateFactory deviceLifeCycleStateFactory;
    private final AuthorizedActionInfoFactory authorizedActionInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceLifeCycleStateResource(
            ExceptionFactory exceptionFactory,
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            FiniteStateMachineService finiteStateMachineService,
            DeviceLifeCycleStateFactory deviceLifeCycleStateFactory,
            AuthorizedActionInfoFactory authorizedActionInfoFactory,
            ResourceHelper resourceHelper) {
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.deviceLifeCycleStateFactory = deviceLifeCycleStateFactory;
        this.authorizedActionInfoFactory = authorizedActionInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getStatesForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @BeanParam JsonQueryParameters queryParams) {
        List<DeviceLifeCycleStateInfo> states = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId).getFiniteStateMachine().getStates()
                .stream()
                .map(deviceLifeCycleStateFactory::from)
                .sorted((st1, st2) -> st1.name.compareToIgnoreCase(st2.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleStates", ListPager.of(states).from(queryParams).find(), queryParams);
    }

    @GET
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public Response getStateById(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, @BeanParam JsonQueryParameters queryParams) {
        State state = resourceHelper.findStateByIdOrThrowException(resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId), stateId);
        return Response.ok(deviceLifeCycleStateFactory.from(state)).build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response addDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, DeviceLifeCycleStateInfo stateInfo) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        State newState = fsmUpdater.newCustomState(stateInfo.name).complete();
        boolean firstState = deviceLifeCycle.getFiniteStateMachine().getStates().isEmpty();
        FiniteStateMachine fsm = firstState ? fsmUpdater.complete(newState) : fsmUpdater.complete();
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleStateFactory.from(newState)).build();
    }

    @PUT
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response editDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, DeviceLifeCycleStateInfo stateInfo) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        State stateForEdit = resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, stateId);



        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        FiniteStateMachineUpdater.StateUpdater stateUpdater = fsmUpdater.state(stateId);
        if (stateForEdit.isCustom()) {
            stateUpdater.setName(stateInfo.name);
        }

        stateInfo.onEntry.stream().map(this::findStateChangeBusinessProcess).forEach(stateUpdater::onEntry);
        stateInfo.onExit.stream().map(this::findStateChangeBusinessProcess).forEach(stateUpdater::onExit);
        // remove 'obsolete' onEntry processes:
        stateForEdit.getOnEntryProcesses().stream()
                .map(ProcessReference::getStateChangeBusinessProcess)
                .filter(x -> isObsoleteStateChageBusinessProcess(stateInfo.onEntry, x))
                .forEach(stateUpdater::removeOnEntry);
        //remove 'obsolete' onExit processes
        stateForEdit.getOnExitProcesses().stream()
                .map(ProcessReference::getStateChangeBusinessProcess)
                .filter(x -> isObsoleteStateChageBusinessProcess(stateInfo.onExit, x))
                .forEach(stateUpdater::removeOnExit);

        State stateAfterEdit = stateUpdater.complete();
        fsmUpdater.complete();
        return Response.ok(deviceLifeCycleStateFactory.from(stateAfterEdit)).build();
    }

    private StateChangeBusinessProcess findStateChangeBusinessProcess(TransitionBusinessProcessInfo businessProcessInfo){
        Optional<StateChangeBusinessProcess> process = finiteStateMachineService.findStateChangeBusinessProcessById(businessProcessInfo.id);
        if (!process.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.STATE_CHANGE_BUSINESS_PROCESS_NOT_FOUND, businessProcessInfo.id);
        }
        return process.get();
    }

    private boolean isObsoleteStateChageBusinessProcess(List<TransitionBusinessProcessInfo> transitionBussinessProcessInfos, StateChangeBusinessProcess stateChangeBusinessProcess){
        return transitionBussinessProcessInfos.stream().noneMatch(x -> x.id == stateChangeBusinessProcess.getId());
    }

    @PUT
    @Path("/{stateId}/status")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response setInitialDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        State stateForEdit = resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, stateId);
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        FiniteStateMachineUpdater fsmUpdater = finiteStateMachine.startUpdate();
        fsmUpdater.complete(stateForEdit);

        return Response.ok(deviceLifeCycleStateFactory.from(stateForEdit)).build();
    }

    @DELETE
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response deleteDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        State stateForDeletion = resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, stateId);
        resourceHelper.checkDeviceLifeCycleUsages(deviceLifeCycle);
        checkStateHasTransitions(deviceLifeCycle, stateForDeletion);
        checkStateIsTheLatest(deviceLifeCycle);
        checkStateIsInitial(stateForDeletion);
        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        fsmUpdater.removeState(stateForDeletion).complete();
        return Response.ok(deviceLifeCycleStateFactory.from(stateForDeletion)).build();
    }

    private void checkStateHasTransitions(DeviceLifeCycle deviceLifeCycle, State stateForDeletion) {
        List<Long> transitionIds = deviceLifeCycle.getFiniteStateMachine().getTransitions().stream()
                .filter(transition -> transition.getFrom().getId() == stateForDeletion.getId()
                                      || transition.getTo().getId() == stateForDeletion.getId())
                .map(transition -> transition.getId())
                .collect(Collectors.toList());
        if (!transitionIds.isEmpty()){
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
        if (deviceLifeCycle.getFiniteStateMachine().getStates().size() == 1){
            throw exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_IS_THE_LATEST_STATE);
        }
    }

    private void checkStateIsInitial(State stateForDeletion) {
        if (stateForDeletion.isInitial()){
            throw exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_IS_THE_INITIAL_STATE);
        }
    }

}
