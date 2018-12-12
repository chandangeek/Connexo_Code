/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransitionEventTypeStillInUseException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the deletion aspects of the {@link StateTransitionEventTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-10 (16:01)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteStateTransitionEventTypeImplTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerFiniteStateMachineService stateMachineService;
    @Mock
    private com.elster.jupiter.events.EventType eventType;

    @Test
    public void testCustomDeleteWhenNotInUse() {
        StateTransitionEventTypeImpl testInstance = new CustomStateTransitionEventTypeImpl(this.dataModel, this.thesaurus, this.stateMachineService);
        when(this.stateMachineService.findFiniteStateMachinesUsing(testInstance)).thenReturn(Collections.emptyList());

        // Business method
        testInstance.delete();

        // Asserts
        verify(this.stateMachineService).findFiniteStateMachinesUsing(testInstance);
        verify(this.dataModel).remove(testInstance);
    }

    @Test(expected = StateTransitionEventTypeStillInUseException.class)
    public void testCustomDeleteWhenInUse() {
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        StateTransitionEventTypeImpl testInstance = new CustomStateTransitionEventTypeImpl(this.dataModel, this.thesaurus, this.stateMachineService);
        when(this.stateMachineService.findFiniteStateMachinesUsing(testInstance)).thenReturn(Arrays.asList(stateMachine));

        // Business method
        testInstance.delete();

        // Asserts: see expected exception rule
    }

    @Test
    public void testStandardDeleteWhenNotInUse() {
        StandardStateTransitionEventTypeImpl testInstance = new StandardStateTransitionEventTypeImpl(this.dataModel, this.thesaurus, this.stateMachineService);
        testInstance.initialize(this.eventType);
        when(this.stateMachineService.findFiniteStateMachinesUsing(testInstance)).thenReturn(Collections.emptyList());

        // Business method
        testInstance.delete();

        // Asserts
        verify(this.stateMachineService).findFiniteStateMachinesUsing(testInstance);
        verify(this.eventType).disableForUseInStateMachines();
        verify(this.dataModel).remove(testInstance);
    }

    @Test(expected = StateTransitionEventTypeStillInUseException.class)
    public void testStandardDeleteWhenInUse() {
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        StandardStateTransitionEventTypeImpl testInstance = new StandardStateTransitionEventTypeImpl(this.dataModel, this.thesaurus, this.stateMachineService);
        testInstance.initialize(this.eventType);
        when(this.stateMachineService.findFiniteStateMachinesUsing(testInstance)).thenReturn(Arrays.asList(stateMachine));

        // Business method
        testInstance.delete();

        // Asserts: see expected exception rule
    }

}