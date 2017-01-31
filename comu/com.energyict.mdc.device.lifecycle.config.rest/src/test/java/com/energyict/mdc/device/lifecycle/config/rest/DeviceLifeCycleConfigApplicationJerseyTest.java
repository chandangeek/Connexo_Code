/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.impl.DefaultLifeCycleTranslationKey;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCheckTranslationKey;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleConfigApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    public static final long OK_VERSION = 6L;

    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected UserService userService;
    @Mock
    protected DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    protected FiniteStateMachineService finiteStateMachineService;
    @Mock
    protected EventService eventService;
    @Mock
    protected DeviceConfigurationService deviceConfigurationService;
    @Mock
    protected DeviceLifeCycleService deviceLifeCycleService;

    @Override
    protected Application getApplication() {
        DeviceLifeCycleConfigApplication application = new DeviceLifeCycleConfigApplication();
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setUserService(userService);
        application.setNlsService(nlsService);
        application.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        application.setFiniteStateMachineService(finiteStateMachineService);
        application.setEventService(eventService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setDeviceLifeCycleService(deviceLifeCycleService);
        when(nlsService.getThesaurus(DeviceLifeCycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT, Layer.REST)).thenReturn(thesaurus);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("Device Type");
        when(deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(any(DeviceLifeCycle.class)))
                .thenReturn(Collections.singletonList(deviceType));
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not support in unit tests");
        doReturn(messageFormat).when(thesaurus).getFormat(any(MessageSeed.class));
        doReturn(messageFormat).when(thesaurus).getFormat(any(TranslationKey.class));
        when(deviceLifeCycleService.getName(any(MicroAction.class))).thenAnswer(invocationOnMock -> ((MicroAction) invocationOnMock
                .getArguments()[0]).name());
        when(deviceLifeCycleService.getDescription(any(MicroAction.class))).thenAnswer(invocationOnMock -> ((MicroAction) invocationOnMock
                .getArguments()[0]).name());
        when(deviceLifeCycleService.getCategoryName(any(MicroAction.class))).thenAnswer(invocationOnMock -> ((MicroAction) invocationOnMock
                .getArguments()[0]).getCategory().name());

        when(deviceLifeCycleService.getName(any(MicroCheck.class))).thenAnswer(invocationOnMock -> {
            MicroCheck microCheck = (MicroCheck) invocationOnMock.getArguments()[0];
            if (EnumSet.of(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID).contains(microCheck)) {
                return MicroCheckTranslationKey.MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE.getKey();
            }
            return microCheck.name();
        });
        when(deviceLifeCycleService.getDescription(any(MicroCheck.class)))
            .thenAnswer(invocationOnMock -> ((MicroCheck) invocationOnMock.getArguments()[0]).name());
        when(deviceLifeCycleService.getCategoryName(any(MicroCheck.class)))
            .thenAnswer(invocationOnMock -> ((MicroCheck) invocationOnMock.getArguments()[0]).getCategory().name());
        Stream.of(DefaultState.values()).forEach(this::mockTranslationFor);
    }

    private void mockTranslationFor(DefaultState state) {
        when(this.deviceLifeCycleConfigurationService.getDisplayName(state)).thenReturn(state.getDefaultFormat());
    }

    // Common mocks for device lifecycle configuration
    protected DeviceLifeCycle mockSimpleDeviceLifeCycle(long id, String name) {
        DeviceLifeCycle dlc = mock(DeviceLifeCycle.class);
        when(dlc.getId()).thenReturn(id);
        when(dlc.getName()).thenReturn(name);
        when(dlc.getVersion()).thenReturn(OK_VERSION);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(id)).thenReturn(Optional.of(dlc));
        when(deviceLifeCycleConfigurationService.findAndLockDeviceLifeCycleByIdAndVersion(id, OK_VERSION))
            .thenReturn(Optional.of(dlc));
        return dlc;
    }

    private State mockSimpleState(long id, DefaultState defaultState) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(defaultState.getKey());
        when(state.isCustom()).thenReturn(false);
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(OK_VERSION);
        when(finiteStateMachineService.findFiniteStateById(id)).thenReturn(Optional.of(state));
        when(finiteStateMachineService.findAndLockStateByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(state));
        return state;
    }

    protected State mockSimpleState(long id, String name) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.isCustom()).thenReturn(false);
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(OK_VERSION);
        when(finiteStateMachineService.findFiniteStateById(id)).thenReturn(Optional.of(state));
        when(finiteStateMachineService.findAndLockStateByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(state));
        return state;
    }

    private State mockSimpleStateWithEntryAndExitProcesses(long id, String name, StateChangeBusinessProcess[] onEntry, StateChangeBusinessProcess[] onExit) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.isCustom()).thenReturn(false);
        when(state.isInitial()).thenReturn(false);

        List<ProcessReference> onEntryReferences = Collections.emptyList();
        if (onEntry != null) {
            onEntryReferences = new ArrayList<>(onEntry.length);
            for (StateChangeBusinessProcess each : onEntry) {
                ProcessReference reference = mock(ProcessReference.class);
                when(reference.getStateChangeBusinessProcess()).thenReturn(each);

                onEntryReferences.add(reference);
            }
        }
        List<ProcessReference> onExitReferences = Collections.emptyList();
        if (onExit != null) {
            onExitReferences = new ArrayList<>(onExit.length);
            for (StateChangeBusinessProcess each : onExit) {
                ProcessReference reference = mock(ProcessReference.class);
                when(reference.getStateChangeBusinessProcess()).thenReturn(each);

                onExitReferences.add(reference);
            }
        }
        when(state.getOnEntryProcesses()).thenReturn(onEntryReferences);
        when(state.getOnExitProcesses()).thenReturn(onExitReferences);
        return state;
    }

    protected List<State> mockDefaultStates() {
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, DefaultState.DECOMMISSIONED));
        states.add(mockSimpleState(1, DefaultState.COMMISSIONING));
        states.add(mockSimpleState(3, DefaultState.IN_STOCK));
        return states;
    }

    protected List<State> mockDefaultStatesWithOnEntryProcessesForDecommissioned() {
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleStateWithEntryAndExitProcesses(2, DefaultState.DECOMMISSIONED.getKey(),
                new StateChangeBusinessProcess[]{
                        mockStateChangeBusinessProcess(1, "nameOnEntry1", "deploymentIdOnEntry1", "processIdOnEntry1"),
                        mockStateChangeBusinessProcess(2, "nameOnEntry2", "deploymentIdOnEntry2", "processIdOnEntry2")},
                null));
        states.add(mockSimpleState(1, DefaultState.COMMISSIONING));
        states.add(mockSimpleState(3, DefaultState.IN_STOCK));
        return states;
    }

    protected List<State> mockDefaultStatesWithOnExitProcessesForInStock() {
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, DefaultState.DECOMMISSIONED));
        states.add(mockSimpleState(1, DefaultState.COMMISSIONING));
        states.add(mockSimpleStateWithEntryAndExitProcesses(3, DefaultState.IN_STOCK.getKey(), null,
                new StateChangeBusinessProcess[]{
                        mockStateChangeBusinessProcess(1, "nameOnExit1", "deploymentIdOnExit1", "processIdOnExit1"),
                        mockStateChangeBusinessProcess(2, "nameOnExit2", "deploymentIdOnExit2", "processIdOnExit2"),
                        mockStateChangeBusinessProcess(3, "nameOnExit3", "deploymentIdOnExit3", "processIdOnExit3")}));
        return states;
    }

    protected List<State> mockDefaultStatesWithOnEntryAndOnExitProcessesForCommissioning() {
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, DefaultState.DECOMMISSIONED));
        states.add(mockSimpleStateWithEntryAndExitProcesses(1,
                DefaultState.COMMISSIONING.getKey(),
                new StateChangeBusinessProcess[]{
                        mockStateChangeBusinessProcess(1, ",nameOnEntry1", "deploymentIdOnEntry1", "processIdOnEntry1"),
                        mockStateChangeBusinessProcess(2, "nameOnEntry2", "deploymentIdOnEntry2", "processIdOnEntry2")},
                new StateChangeBusinessProcess[]{mockStateChangeBusinessProcess(3, "nameOnExit1", "deploymentIdOnExit1", "processIdOnExit1")}));
        states.add(mockSimpleState(3, DefaultState.IN_STOCK));
        return states;
    }

    private AuthorizedTransitionAction mockBasicAction(long id, String name, State from, State to) {
        String translated = thesaurus.getString(name, name);
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getId()).thenReturn(id);
        when(action.getName()).thenReturn(translated);
        when(action.getLevels()).thenReturn(Collections.singleton(AuthorizedAction.Level.ONE));
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);
        when(transition.getName()).thenReturn(Optional.of(name));
        StateTransitionEventType eventType = mock(StateTransitionEventType.class);
        when(eventType.getSymbol()).thenReturn(DefaultCustomStateTransitionEventType.COMMISSIONING.getSymbol());
        when(transition.getEventType()).thenReturn(eventType);
        when(action.getStateTransition()).thenReturn(transition);
        return action;
    }

    protected AuthorizedTransitionAction mockSimpleAction(long id, String name, State from, State to) {
        AuthorizedTransitionAction action = mockBasicAction(id, name, from, to);
        when(action.getActions()).thenReturn(EnumSet.of(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE, MicroAction.CREATE_METER_ACTIVATION, MicroAction.ENABLE_VALIDATION));
        when(action.getChecks()).thenReturn(EnumSet.of(
                MicroCheck.ALL_DATA_VALID,
                MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.DEFAULT_CONNECTION_AVAILABLE
        ));
        return action;
    }

    private AuthorizedTransitionAction mockSimpleActionWithoutCommunications(long id, String name, State from, State to) {
        AuthorizedTransitionAction action = mockBasicAction(id, name, from, to);
        when(action.getActions()).thenReturn(EnumSet.of(MicroAction.CREATE_METER_ACTIVATION, MicroAction.ENABLE_VALIDATION));
        when(action.getChecks()).thenReturn(EnumSet.of(
                MicroCheck.ALL_DATA_VALID
        ));
        return action;
    }

    private AuthorizedTransitionAction mockSimpleActionCommunicationRelatedMicroActions(long id, String name, State from, State to) {
        AuthorizedTransitionAction action = mockBasicAction(id, name, from, to);
        when(action.getActions()).thenReturn(EnumSet.of(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE, MicroAction.CREATE_METER_ACTIVATION, MicroAction.ENABLE_VALIDATION));
        when(action.getChecks()).thenReturn(EnumSet.of(
                MicroCheck.ALL_DATA_VALID
        ));
        return action;
    }

    private AuthorizedTransitionAction mockSimpleActionCommunicationRelatedMicroChecks(long id, String name, State from, State to) {
        AuthorizedTransitionAction action = mockBasicAction(id, name, from, to);
        when(action.getActions()).thenReturn(EnumSet.of(MicroAction.CREATE_METER_ACTIVATION, MicroAction.ENABLE_VALIDATION));
        when(action.getChecks()).thenReturn(EnumSet.of(
                MicroCheck.ALL_DATA_VALID,
                MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.DEFAULT_CONNECTION_AVAILABLE
        ));
        return action;
    }

    protected List<AuthorizedAction> mockDefaultActions() {
        List<State> states = mockDefaultStates();
        List<AuthorizedAction> actions = new ArrayList<>(2);
        actions.add(mockSimpleAction(1, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING.getDefaultFormat(), states
                .get(2), states.get(1)));
        actions.add(mockSimpleAction(2, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED.getDefaultFormat(), states
                .get(1), states.get(0)));
        return actions;
    }

    protected List<AuthorizedAction> mockActionsWithoutTheCommunicationCategory() {
        List<State> states = mockDefaultStates();
        List<AuthorizedAction> actions = new ArrayList<>(2);
        actions.add(mockSimpleActionWithoutCommunications(1, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING
                .getDefaultFormat(), states.get(2), states.get(1)));
        actions.add(mockSimpleActionWithoutCommunications(2, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED
                .getDefaultFormat(), states.get(1), states.get(0)));
        return actions;
    }

    protected List<AuthorizedAction> mockActionsWithCommunicationRelatedActionsInMicroActions() {
        List<State> states = mockDefaultStates();
        List<AuthorizedAction> actions = new ArrayList<>(2);
        actions.add(mockSimpleActionCommunicationRelatedMicroActions(1, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING
                .getDefaultFormat(), states.get(2), states.get(1)));
        actions.add(mockSimpleActionWithoutCommunications(2, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED
                .getDefaultFormat(), states.get(1), states.get(0)));
        return actions;
    }

    protected List<AuthorizedAction> mockActionsWithCommunicationRelatedActionsInMicroChecks() {
        List<State> states = mockDefaultStates();
        List<AuthorizedAction> actions = new ArrayList<>(2);
        actions.add(mockSimpleActionWithoutCommunications(1, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING
                .getDefaultFormat(), states.get(2), states.get(1)));
        actions.add(mockSimpleActionCommunicationRelatedMicroChecks(2, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED
                .getDefaultFormat(), states.get(1), states.get(0)));
        return actions;
    }


    protected StateChangeBusinessProcess mockStateChangeBusinessProcess(long id, String name, String deploymentId, String processId) {
        StateChangeBusinessProcess process = mock(StateChangeBusinessProcess.class);
        when(process.getId()).thenReturn(id);
        when(process.getName()).thenReturn(name);
        when(process.getDeploymentId()).thenReturn(deploymentId);
        when(process.getProcessId()).thenReturn(processId);
        return process;
    }
}
