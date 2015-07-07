package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StateTransitionTriggerEventTopicHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (13:51)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionTriggerEventTopicHandlerTest {

    public static final String SOURCE_ID = "TestDevice";
    @Mock
    private EventService eventService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private StateTransitionTriggerEvent triggerEvent;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    private Map<String, Object> eventProperties;
    private long eventTypeId;
    private long stateId;
    private StateTransitionEventType installed;
    private StateTransitionEventType deactivated;
    private StateTransitionEventType activated;
    private StateTransitionEventType decommissionedEventType;
    private State inStock;
    private State active;
    private State inactive;
    private State decommissioned;
    private StateTransitionEventType dataCollected;

    @Before
    public void initializeMocks() {
        this.mockFiniteStateMachine();
        this.eventProperties = new HashMap<>();
        when(this.localEvent.getSource()).thenReturn(this.triggerEvent);
        when(this.triggerEvent.getFiniteStateMachine()).thenReturn(this.finiteStateMachine);
        when(this.triggerEvent.getProperties()).thenReturn(this.eventProperties);
        when(this.triggerEvent.getSourceId()).thenReturn(SOURCE_ID);
        when(this.triggerEvent.getProperties()).thenReturn(this.eventProperties);
    }

    /**
     * Mocks the following finite state machine:
     * Instock --(#installed)--> Active
     * Active --(#deactivated)--> Inactive
     * Active --(#measured)--> Active
     * Inactive --(#activated)--> Active
     * Active --(#decommissioned)--> Decommissioned
     * Inactive --(#decommissioned)--> Decommissioned
     */
    private void mockFiniteStateMachine() {
        when(this.finiteStateMachine.getName()).thenReturn("StateTransitionTriggerEventTopicHandlerTest");
        this.mockEventTypes();
        this.mockStates();
        List<State> states = Arrays.asList(this.inStock, this.active, this.inactive, this.decommissioned);
        when(this.finiteStateMachine.getStates()).thenReturn(states);
        when(this.finiteStateMachine.getState(anyString())).thenReturn(Optional.empty());
        when(this.finiteStateMachine.getState(this.inStock.getName())).thenReturn(Optional.of(this.inStock));
        when(this.finiteStateMachine.getState(this.active.getName())).thenReturn(Optional.of(this.active));
        when(this.finiteStateMachine.getState(this.inactive.getName())).thenReturn(Optional.of(this.inactive));
        when(this.finiteStateMachine.getState(this.decommissioned.getName())).thenReturn(Optional.of(this.decommissioned));
        StateTransition install = this.mockStateTransition(this.inStock, this.active, this.installed);
        StateTransition deactivate = this.mockStateTransition(this.active, this.inactive, this.deactivated);
        StateTransition activate = this.mockStateTransition(this.inactive, this.active, this.activated);
        StateTransition decommissionActive = this.mockStateTransition(this.active, this.decommissioned, this.decommissionedEventType);
        StateTransition decommissionInactive = this.mockStateTransition(this.inactive, this.decommissioned, this.decommissionedEventType);
        StateTransition collectData = this.mockStateTransition(this.active, this.active, this.dataCollected);
        when(this.finiteStateMachine.getTransitions()).thenReturn(Arrays.asList(install, deactivate, activate, decommissionActive, decommissionInactive));
        when(this.inStock.getOutgoingStateTransitions()).thenReturn(Arrays.asList(install));
        when(this.active.getOutgoingStateTransitions()).thenReturn(Arrays.asList(deactivate, collectData, decommissionActive));
        when(this.inactive.getOutgoingStateTransitions()).thenReturn(Arrays.asList(activate, decommissionInactive));
    }

    private void mockEventTypes() {
        this.eventTypeId = 0;
        this.installed = this.mockEventType("#installed");
        this.deactivated = this.mockEventType("#deactivated");
        this.activated = this.mockEventType("#activated");
        this.decommissionedEventType = this.mockEventType("#decommissioned");
        this.dataCollected = this.mockEventType("#dataCollected");
    }

    private void mockStates() {
        this.stateId = 0;
        this.inStock = this.mockState(this.finiteStateMachine, "Instock");
        this.active = this.mockState(this.finiteStateMachine, "Active");
        this.inactive = this.mockState(this.finiteStateMachine, "Inactive");
        this.decommissioned = this.mockState(this.finiteStateMachine, "Decommissioned");
    }

    private StateTransitionEventType mockEventType(String symbol) {
        StateTransitionEventType eventType = mock(StateTransitionEventType.class);
        when(eventType.getSymbol()).thenReturn(symbol);
        when(eventType.getId()).thenReturn(this.eventTypeId++);
        return eventType;
    }

    private State mockState(FiniteStateMachine stateMachine, String name) {
        State state = mock(State.class);
        when(state.getName()).thenReturn(name);
        when(state.getId()).thenReturn(this.stateId++);
        when(state.getFiniteStateMachine()).thenReturn(stateMachine);
        return state;
    }

    private StateTransition mockStateTransition(State from, State to, StateTransitionEventType eventType) {
        StateTransition transition = mock(StateTransition.class);
        when(transition.getEventType()).thenReturn(eventType);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);
        return transition;
    }

    @Test
    public void handleStateChange() {
        StateTransitionTriggerEventTopicHandler handler = new StateTransitionTriggerEventTopicHandler(this.eventService);
        this.eventProperties.put("Prop1", BigDecimal.TEN);
        this.eventProperties.put("Prop2", "some value");
        String currentStateName = this.active.getName();
        when(this.triggerEvent.getSourceCurrentStateName()).thenReturn(currentStateName);
        when(this.triggerEvent.getType()).thenReturn(this.deactivated);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        ArgumentCaptor<StateTransitionChangeEvent> changeEvent = ArgumentCaptor.forClass(StateTransitionChangeEvent.class);
        verify(this.eventService).postEvent(eq(EventType.CHANGE_EVENT.topic()), changeEvent.capture());
        StateTransitionChangeEvent capturedChangeEvent = changeEvent.getValue();
        assertThat(capturedChangeEvent.getSourceId()).isEqualTo(SOURCE_ID);
        assertThat(capturedChangeEvent.getOldState().getName()).isEqualTo(currentStateName);
        assertThat(capturedChangeEvent.getNewState().getName()).isEqualTo(this.inactive.getName());
        Map<String, Object> capturedProperties = capturedChangeEvent.getProperties();
        assertThat(capturedProperties).hasSize(2);
        assertThat(capturedProperties.get("Prop1")).isEqualTo(BigDecimal.TEN);
        assertThat(capturedProperties.get("Prop2")).isEqualTo("some value");
    }

    @Test
    public void handleIllegalStateChange() {
        StateTransitionTriggerEventTopicHandler handler = new StateTransitionTriggerEventTopicHandler(this.eventService);
        String currentStateName = this.inStock.getName();
        when(this.triggerEvent.getSourceCurrentStateName()).thenReturn(currentStateName);
        when(this.triggerEvent.getType()).thenReturn(this.deactivated);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.eventService, never()).postEvent(anyString(), any(StateTransitionChangeEvent.class));
    }

    @Test
    public void handleNonExistingCurrentState() {
        StateTransitionTriggerEventTopicHandler handler = new StateTransitionTriggerEventTopicHandler(this.eventService);
        when(this.triggerEvent.getSourceCurrentStateName()).thenReturn("currentStateName");
        when(this.triggerEvent.getType()).thenReturn(this.deactivated);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.eventService, never()).postEvent(anyString(), any(StateTransitionChangeEvent.class));
    }

    @Test
    public void handleNoStateChange() {
        StateTransitionTriggerEventTopicHandler handler = new StateTransitionTriggerEventTopicHandler(this.eventService);
        String currentStateName = this.active.getName();
        when(this.triggerEvent.getSourceCurrentStateName()).thenReturn(currentStateName);
        when(this.triggerEvent.getType()).thenReturn(this.dataCollected);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.eventService, never()).postEvent(anyString(), any(StateTransitionChangeEvent.class));
    }

    @Test
    public void onEntryIsTriggered() {
        ProcessReference processReference = mock(ProcessReference.class);
        StateChangeBusinessProcess process = mock(StateChangeBusinessProcess.class);
        when(processReference.getStateChangeBusinessProcess()).thenReturn(process);
        when(this.inactive.getOnEntryProcesses()).thenReturn(Arrays.asList(processReference));
        StateTransitionTriggerEventTopicHandler handler = new StateTransitionTriggerEventTopicHandler(this.eventService);
        this.eventProperties.put("Prop1", BigDecimal.TEN);
        this.eventProperties.put("Prop2", "some value");
        String currentStateName = this.active.getName();
        when(this.triggerEvent.getSourceCurrentStateName()).thenReturn(currentStateName);
        when(this.triggerEvent.getType()).thenReturn(this.deactivated);
        String sourceId = "onEntryIsTriggered";
        when(this.triggerEvent.getSourceId()).thenReturn(sourceId);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(process).executeOnEntry(sourceId, this.inactive);
    }

    @Test
    public void onExitIsTriggered() {
        ProcessReference processReference = mock(ProcessReference.class);
        StateChangeBusinessProcess process = mock(StateChangeBusinessProcess.class);
        when(processReference.getStateChangeBusinessProcess()).thenReturn(process);
        when(this.active.getOnExitProcesses()).thenReturn(Arrays.asList(processReference));
        StateTransitionTriggerEventTopicHandler handler = new StateTransitionTriggerEventTopicHandler(this.eventService);
        this.eventProperties.put("Prop1", BigDecimal.TEN);
        this.eventProperties.put("Prop2", "some value");
        String currentStateName = this.active.getName();
        when(this.triggerEvent.getSourceCurrentStateName()).thenReturn(currentStateName);
        when(this.triggerEvent.getType()).thenReturn(this.deactivated);
        String sourceId = "onExitIsTriggered";
        when(this.triggerEvent.getSourceId()).thenReturn(sourceId);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(process).executeOnExit(sourceId, this.active);
    }

}