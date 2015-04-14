package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        when(nlsService.getThesaurus(DeviceLifeCycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT, Layer.REST)).thenReturn(thesaurus);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("Device Type");
        when(deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(Matchers.any(DeviceLifeCycle.class)))
                .thenReturn(Collections.singletonList(deviceType));
        return application;
    }

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
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
        when(state.isCustom()).thenReturn(true);
        when(state.isInitial()).thenReturn(false);
        return state;
    }

    public List<State> mockDefaultStates(){
        List<State> states = new ArrayList<>(3);
        states.add(mockSimpleState(2, "Decommissioned"));
        states.add(mockSimpleState(1, "Commissioned"));
        states.add(mockSimpleState(3, "In stock"));
        return states;
    }

    public AuthorizedTransitionAction mockSimpleAction(long id, String name, State from, State to){
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getId()).thenReturn(id);
        when(action.getLevels()).thenReturn(Collections.singleton(AuthorizedAction.Level.ONE));
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);
        when(transition.getName()).thenReturn(Optional.of(name));
        StateTransitionEventType eventType = mock(StateTransitionEventType.class);
        when(eventType.getSymbol()).thenReturn("#eventType");
        when(transition.getEventType()).thenReturn(eventType);
        String translated = thesaurus.getString(name, name);
        when(transition.getName(Matchers.any(Thesaurus.class))).thenReturn(translated);
        when(action.getStateTransition()).thenReturn(transition);
        return action;
    }

    public List<AuthorizedAction> mockDefaultActions(){
        List<State> states = mockDefaultStates();
        List<AuthorizedAction> actions = new ArrayList<>(2);
        actions.add(mockSimpleAction(1, "#decommissioned", states.get(1), states.get(0)));
        actions.add(mockSimpleAction(2, "#commissioned", states.get(2), states.get(1)));
        return actions;
    }
}
