/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StateTransitionChangeEventTopicHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (08:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionChangeEventTopicHandlerTest {

    private static final long END_DEVICE_ID = 121;
    private static final String END_DEVICE_MRID = "MasterResourceIdentifier";
    private static final String MISSING_END_DEVICE_MRID = "DoesNotExist";

    @Mock
    private FiniteStateMachineService stateMachineService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private StateTransitionChangeEvent event;
    @Mock
    private State state;
    @Mock
    private ServerEndDevice endDevice;
    @Mock
    private Clock clock;
    @Mock
    private Query<EndDevice> endDeviceQuery;
    @Mock
    private FiniteStateMachine finiteStateMachine;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getSource()).thenReturn(this.event);
        when(this.event.getSourceId()).thenReturn(MISSING_END_DEVICE_MRID);
        when(this.event.getSourceType()).thenReturn(EndDevice.class.getName());
        when(this.event.getNewState()).thenReturn(this.state);
        when(this.state.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(this.finiteStateMachine.getId()).thenReturn(1L);
        when(this.meteringService.findEndDeviceByMRID(MISSING_END_DEVICE_MRID)).thenReturn(Optional.empty());
        when(this.meteringService.findEndDeviceByMRID(END_DEVICE_MRID)).thenReturn(Optional.of(this.endDevice));
        when(this.endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(this.endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(this.endDevice.getFiniteStateMachine()).thenReturn(Optional.of(finiteStateMachine));
        when(this.meteringService.getEndDeviceQuery()).thenReturn(endDeviceQuery);
        when(endDeviceQuery.select(any(Condition.class))).thenReturn(Collections.singletonList(endDevice));
    }

    @Test
    public void handlerIgnoreNonDeviceEvents() {
        when(this.event.getSourceType()).thenReturn("Please Ignore Me");

        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.endDeviceQuery, never()).select(any(Condition.class));
    }

    @Test
    public void handlerAttemptsToFindTheEndDevice() {
        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.endDeviceQuery).select(any(Condition.class));
    }

    @Test
    public void handlerDelegatesToTheEndDeviceWithEffectiveTimestampFromEvent() {
        when(this.event.getSourceId()).thenReturn(String.valueOf(END_DEVICE_ID));
        Instant effective = Instant.ofEpochSecond(1000L);
        when(this.event.getEffectiveTimestamp()).thenReturn(effective);

        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.endDevice).changeState(this.state, effective);
        verify(this.event).getEffectiveTimestamp();
        verify(this.clock, never()).instant();
    }

    @Test
    public void handlerDelegatesToTheEndDeviceWithEffectiveTimestampFromClock() {
        Instant effective = Instant.ofEpochSecond(2000L);
        when(this.clock.instant()).thenReturn(effective);
        when(this.event.getSourceId()).thenReturn(String.valueOf(END_DEVICE_ID));

        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.endDevice).changeState(this.state, effective);
        verify(this.event).getEffectiveTimestamp();
        verify(this.clock).instant();
    }

    private StateTransitionChangeEventTopicHandler getTestInstance() {
        return new StateTransitionChangeEventTopicHandler(this.clock, this.stateMachineService, this.meteringService);
    }

}
