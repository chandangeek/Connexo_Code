/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;

import org.osgi.service.event.Event;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * Test the {@link StandardEventHandler} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class StandardEventHandlerTest {

    private static final String TOPIC = StandardEventHandlerTest.class.getSimpleName();
    private static final String PROP1_NAME = "PROP1";
    private static final BigDecimal PROP1_VALUE = BigDecimal.TEN;
    private static final String PROP2_NAME = "PROP2";
    private static final String PROP2_VALUE = "value for property 2";

    @Mock
    private EventService eventService;
    @Mock
    private ServerFiniteStateMachineService stateMachineService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private com.elster.jupiter.events.EventType jupiterEventType;
    @Mock
    private StandardStateTransitionEventType standardStateTransitionEventType;

    @Before
    public void initializeMocks() {
        HashMap<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(PROP1_NAME, PROP1_VALUE);
        eventProperties.put(PROP2_NAME, PROP2_VALUE);
        Event osgiEvent = new Event(TOPIC, eventProperties);
        when(this.jupiterEventType.getTopic()).thenReturn(TOPIC);
        when(this.jupiterEventType.isEnabledForUseInStateMachines()).thenReturn(true);
        when(this.localEvent.getType()).thenReturn(this.jupiterEventType);
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);
        when(this.stateMachineService.findStandardStateTransitionEventType(this.jupiterEventType)).thenReturn(Optional.of(this.standardStateTransitionEventType));
        when(this.standardStateTransitionEventType.getEventType()).thenReturn(this.jupiterEventType);
        when(this.standardStateTransitionEventType.getSymbol()).thenReturn(TOPIC);
    }

    @Test
    public void handlerChecksThatEventTypeIsEnabledForStatemachines() {
        StandardEventHandler handler = this.testEventHandlerWithoutExtractors();
        when(this.jupiterEventType.isEnabledForUseInStateMachines()).thenReturn(false);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.localEvent).getType();
        verify(this.jupiterEventType).isEnabledForUseInStateMachines();
    }

    @Test
    public void handlerFindsTheStandardStateTransitionEventTypeWhenTheEventTypeIsEnabledForStatemachines() {
        StandardEventHandler handler = this.testEventHandlerWithoutExtractors();
        when(this.stateMachineService.findStandardStateTransitionEventType(this.jupiterEventType)).thenReturn(Optional.empty());

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.stateMachineService).findStandardStateTransitionEventType(this.jupiterEventType);
    }

    @Test
    public void handlerChecksThatStateMachineIsUsingTheEventType() {
        StandardEventHandler handler = this.testEventHandlerWithoutExtractors();

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.localEvent).getType();
        verify(this.stateMachineService).findFiniteStateMachinesUsing(this.standardStateTransitionEventType);
    }

    @Test
    public void handlerWithoutExtractorsDoesNotPublish() {
        StandardEventHandler handler = this.testEventHandlerWithoutExtractors();

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.eventService, never()).postEvent(anyString(), any());
    }

    @Test
    public void handlerExtractsStateInformation() {
        CurrentStateExtractor currentStateExtractor = mock(CurrentStateExtractor.class);
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(currentStateExtractor.extractFrom(this.localEvent, stateMachine)).thenReturn(Optional.empty());
        StandardEventHandler handler = this.testEventHandlerWithExtractors(currentStateExtractor);
        when(this.stateMachineService.findFiniteStateMachinesUsing(this.standardStateTransitionEventType))
            .thenReturn(Collections.singletonList(stateMachine));

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(currentStateExtractor).extractFrom(this.localEvent, stateMachine);
    }

    @Test
    public void handlerUsesAllExtractors() {
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        CurrentStateExtractor currentStateExtractor1 = mock(CurrentStateExtractor.class);
        CurrentStateExtractor.CurrentState currentState = new CurrentStateExtractor.CurrentState();
        currentState.sourceId = "mRID";
        currentState.sourceType = "handlerUsesAllExtractors";
        currentState.name = "handlerUsesAllExtractors";
        when(currentStateExtractor1.extractFrom(this.localEvent, stateMachine)).thenReturn(Optional.of(currentState));
        CurrentStateExtractor currentStateExtractor2 = mock(CurrentStateExtractor.class);
        when(currentStateExtractor2.extractFrom(this.localEvent, stateMachine)).thenReturn(Optional.empty());
        StandardEventHandler handler = this.testEventHandlerWithExtractors(currentStateExtractor1, currentStateExtractor2);
        when(this.stateMachineService.findFiniteStateMachinesUsing(this.standardStateTransitionEventType))
            .thenReturn(Collections.singletonList(stateMachine));

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(currentStateExtractor1).extractFrom(this.localEvent, stateMachine);
        verify(currentStateExtractor2).extractFrom(this.localEvent, stateMachine);
    }

    @Test
    public void handlerDoesNotPublishWhenNoInformationIsExtracted() {
        CurrentStateExtractor currentStateExtractor = mock(CurrentStateExtractor.class);
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(currentStateExtractor.extractFrom(this.localEvent, stateMachine)).thenReturn(Optional.empty());
        StandardEventHandler handler = this.testEventHandlerWithExtractors(currentStateExtractor);
        when(this.stateMachineService.findFiniteStateMachinesUsing(this.standardStateTransitionEventType))
            .thenReturn(Collections.singletonList(stateMachine));

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.eventService, never()).postEvent(anyString(), any());
    }

    @Test
    public void handlerPublishesExtractedInformation() {
        String expectedSourceType = "handlerPublishesExtractedInformation";
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        CurrentStateExtractor currentStateExtractor = mock(CurrentStateExtractor.class);
        CurrentStateExtractor.CurrentState currentState = new CurrentStateExtractor.CurrentState();
        currentState.sourceId = "mRID";
        currentState.sourceType = expectedSourceType;
        currentState.name = "handlerUsesAllExtractors";
        when(currentStateExtractor.extractFrom(this.localEvent, stateMachine)).thenReturn(Optional.of(currentState));
        StandardEventHandler handler = this.testEventHandlerWithExtractors(currentStateExtractor);
        when(this.stateMachineService.findFiniteStateMachinesUsing(this.standardStateTransitionEventType))
            .thenReturn(Collections.singletonList(stateMachine));

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        ArgumentCaptor<StateTransitionTriggerEvent> triggerEventArgumentCaptor = ArgumentCaptor.forClass(StateTransitionTriggerEvent.class);
        verify(this.eventService).postEvent(eq(EventType.TRIGGER_EVENT.topic()), triggerEventArgumentCaptor.capture());
        StateTransitionTriggerEvent triggerEvent = triggerEventArgumentCaptor.getValue();
        assertThat(triggerEvent).isNotNull();
        assertThat(triggerEvent.getSourceCurrentStateName()).isEqualTo(currentState.name);
        assertThat(triggerEvent.getSourceId()).isEqualTo(currentState.sourceId);
        assertThat(triggerEvent.getSourceType()).isEqualTo(currentState.sourceType);
        assertThat(triggerEvent.getFiniteStateMachine()).isEqualTo(stateMachine);
        assertThat(triggerEvent.getType()).isEqualTo(this.standardStateTransitionEventType);
        Map<String, Object> triggerEventProperties = triggerEvent.getProperties();
        assertThat(triggerEventProperties).isNotEmpty();
        assertThat(triggerEventProperties.get(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        assertThat(triggerEventProperties.get(PROP2_NAME)).isEqualTo(PROP2_VALUE);
    }


    private StandardEventHandler testEventHandlerWithoutExtractors() {
        return new StandardEventHandler(this.eventService, this.stateMachineService, Clock.systemDefaultZone());
    }

    private StandardEventHandler testEventHandlerWithExtractors(CurrentStateExtractor first, CurrentStateExtractor... others) {
        StandardEventHandler handler = this.testEventHandlerWithoutExtractors();
        handler.addCurrentStateExtractor(first);
        Stream.of(others).forEach(handler::addCurrentStateExtractor);
        return handler;
    }

}