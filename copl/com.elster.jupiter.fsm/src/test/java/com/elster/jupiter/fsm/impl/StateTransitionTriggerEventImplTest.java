/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Tests the {@link StateTransitionTriggerEventImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (10:26)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionTriggerEventImplTest {

    @Mock
    private FiniteStateMachine stateMachine;
    @Mock
    private EventService eventService;

    @Test
    public void publishDelegatesToEventService() {
        StateTransitionTriggerEventImpl triggerEvent = new StateTransitionTriggerEventImpl(this.eventService);

        // Business method
        triggerEvent.publish();

        // Asserts
        verify(this.eventService).postEvent(EventType.TRIGGER_EVENT.topic(), triggerEvent);
    }

}