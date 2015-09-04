package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.*;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
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
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;

import java.util.*;
import javax.ws.rs.core.Application;

import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCheckTranslationKey;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleConfigApplicationJerseyTest extends FelixRestApplicationJerseyTest {

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
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(thesaurus.getStringBeyondComponent(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(deviceLifeCycleService.getName(any(MicroAction.class))).thenAnswer(invocationOnMock -> ((MicroAction) invocationOnMock.getArguments()[0]).name());
        when(deviceLifeCycleService.getDescription(any(MicroAction.class))).thenAnswer(invocationOnMock -> ((MicroAction) invocationOnMock.getArguments()[0]).name());
        when(deviceLifeCycleService.getCategoryName(any(MicroAction.class))).thenAnswer(invocationOnMock -> ((MicroAction) invocationOnMock.getArguments()[0]).getCategory().name());

        when(deviceLifeCycleService.getName(any(MicroCheck.class))).thenAnswer(invocationOnMock -> {
            MicroCheck microCheck = (MicroCheck) invocationOnMock.getArguments()[0];
            if (EnumSet.of(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID).contains(microCheck)){
                return MicroCheckTranslationKey.MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE.getKey();
            }
            return microCheck.name();
        });
        when(deviceLifeCycleService.getDescription(any(MicroCheck.class))).thenAnswer(invocationOnMock -> ((MicroCheck) invocationOnMock.getArguments()[0]).name());
        when(deviceLifeCycleService.getCategoryName(any(MicroCheck.class))).thenAnswer(invocationOnMock -> ((MicroCheck) invocationOnMock.getArguments()[0]).getCategory().name());
    }

    // Common mocks for device lifecycle configuration
    public DeviceLifeCycle mockSimpleDeviceLifeCycle(long id, String name){
        DeviceLifeCycle dlc = mock(DeviceLifeCycle.class);
        when(dlc.getId()).thenReturn(id);
        when(dlc.getName()).thenReturn(name);
        return  dlc;
    }

    public State mockSimpleState(long id, String name){
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.isCustom()).thenReturn(false);
        when(state.isInitial()).thenReturn(false);
        return state;
    }

    public State mockSimpleStateWithEntryAndExitProcesses(long id, String name, StateChangeBusinessProcess[] onEntry, StateChangeBusinessProcess[] onExit  ){
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

    public List<State> mockDefaultStates(){
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, DefaultState.DECOMMISSIONED.getKey()));
        states.add(mockSimpleState(1, DefaultState.COMMISSIONING.getKey()));
        states.add(mockSimpleState(3, DefaultState.IN_STOCK.getKey()));
        return states;
    }

    public List<State> mockDefaultStatesWithOnEntryProcessesForDecommissioned(){
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleStateWithEntryAndExitProcesses(2, DefaultState.DECOMMISSIONED.getKey(),
                new StateChangeBusinessProcess[]{mockStateChangeBusinessProcess(1, "nameOnEntry1", "deploymentIdOnEntry1", "processIdOnEntry1"),
                        mockStateChangeBusinessProcess(2, "nameOnEntry2", "deploymentIdOnEntry2", "processIdOnEntry2")},
                null));
        states.add(mockSimpleState(1, DefaultState.COMMISSIONING.getKey()));
        states.add(mockSimpleState(3, DefaultState.IN_STOCK.getKey()));
        return states;
    }

    public List<State> mockDefaultStatesWithOnExitProcessesForInStock(){
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, DefaultState.DECOMMISSIONED.getKey()));
        states.add(mockSimpleState(1, DefaultState.COMMISSIONING.getKey()));
        states.add(mockSimpleStateWithEntryAndExitProcesses(3, DefaultState.IN_STOCK.getKey(), null,
                new StateChangeBusinessProcess[]{mockStateChangeBusinessProcess(1, "nameOnExit1", "deploymentIdOnExit1", "processIdOnExit1"),
                        mockStateChangeBusinessProcess(2, "nameOnExit2", "deploymentIdOnExit2", "processIdOnExit2"),
                        mockStateChangeBusinessProcess(3, "nameOnExit3", "deploymentIdOnExit3", "processIdOnExit3")}));
        return states;
    }

    public List<State> mockDefaultStatesWithOnEntryAndOnExitProcessesForCommissioning(){
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, DefaultState.DECOMMISSIONED.getKey()));
        states.add(mockSimpleStateWithEntryAndExitProcesses(1,
                DefaultState.COMMISSIONING.getKey(),
                new StateChangeBusinessProcess[]{mockStateChangeBusinessProcess(1, ",nameOnEntry1", "deploymentIdOnEntry1", "processIdOnEntry1"),
                                        mockStateChangeBusinessProcess(2, "nameOnEntry2", "deploymentIdOnEntry2", "processIdOnEntry2")},
                new StateChangeBusinessProcess[]{mockStateChangeBusinessProcess(3, "nameOnExit1", "deploymentIdOnExit1", "processIdOnExit1")}));
        states.add(mockSimpleState(3, DefaultState.IN_STOCK.getKey()));
        return states;
    }

    public AuthorizedTransitionAction mockSimpleAction(long id, String name, State from, State to){
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

    public List<AuthorizedAction> mockDefaultActions(){
        List<State> states = mockDefaultStates();
        List<AuthorizedAction> actions = new ArrayList<>(2);
        actions.add(mockSimpleAction(1, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING.getDefaultFormat(), states.get(2), states.get(1)));
        actions.add(mockSimpleAction(2, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED.getDefaultFormat(), states.get(1), states.get(0)));
        return actions;
    }

    public StateChangeBusinessProcess mockStateChangeBusinessProcess(long id, String name, String deploymentId, String processId){
        StateChangeBusinessProcess process = mock(StateChangeBusinessProcess.class);
        when(process.getId()).thenReturn(id);
        when(process.getName()).thenReturn(name);
        when(process.getDeploymentId()).thenReturn(deploymentId);
        when(process.getProcessId()).thenReturn(processId);
        return process;
    }
}
