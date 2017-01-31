/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointStateImplTest {
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EventService eventService;
    @Mock
    private UsagePointLifeCycleImpl lifeCycle;
    @Mock
    private State fsmState;
    @Mock
    private ProcessReference process;
    @Mock
    private DataModel dataModel;

    private UsagePointState getTestInstance() {
        return new UsagePointStateImpl(this.thesaurus, this.eventService, this.dataModel).init(this.lifeCycle, this.fsmState, UsagePointStage.Key.OPERATIONAL);
    }

    @Test
    public void testIsInitialWhenDelegateIsInitial() {
        when(this.fsmState.isInitial()).thenReturn(true);
        assertThat(getTestInstance().isInitial()).isTrue();
    }

    @Test
    public void testIsInitialWhenDelegateIsNotInitial() {
        when(this.fsmState.isInitial()).thenReturn(false);
        assertThat(getTestInstance().isInitial()).isFalse();
    }

    @Test
    public void testCustomStateIsNotDefault() {
        when(this.fsmState.isCustom()).thenReturn(true);
        assertThat(getTestInstance().getDefaultState()).isEmpty();
    }

    @Test
    public void testRenamedStateIsNotDefault() {
        when(this.fsmState.isCustom()).thenReturn(false);
        when(this.fsmState.getName()).thenReturn("Renamed");
        assertThat(getTestInstance().getDefaultState()).isEmpty();
    }

    @Test
    public void testStateIsDefault() {
        when(this.fsmState.isCustom()).thenReturn(false);
        when(this.fsmState.getName()).thenReturn(DefaultState.DEMOLISHED.getKey());
        UsagePointState state = getTestInstance();
        assertThat(state.getDefaultState()).isPresent();
        assertThat(state.getDefaultState().get().getKey()).isEqualTo(DefaultState.DEMOLISHED.getKey());
        assertThat(state.isDefault(DefaultState.DEMOLISHED)).isTrue();
    }

    @Test
    public void testGetProcessesOnEntry() {
        when(this.fsmState.getOnEntryProcesses()).thenReturn(Collections.singletonList(this.process));
        assertThat(getTestInstance().getOnEntryProcesses()).containsExactly(this.process);
    }

    @Test
    public void testGetProcessesOnExit() {
        when(this.fsmState.getOnEntryProcesses()).thenReturn(Collections.singletonList(this.process));
        assertThat(getTestInstance().getOnEntryProcesses()).containsExactly(this.process);
    }

    @Test
    public void testCanRemoveState() {
        FiniteStateMachineUpdater stateMachineUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachineUpdater.removeState(this.fsmState)).thenReturn(stateMachineUpdater);
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.startUpdate()).thenReturn(stateMachineUpdater);
        when(this.fsmState.getFiniteStateMachine()).thenReturn(stateMachine);
        UsagePointState testInstance = getTestInstance();
        UsagePointState anotherState = mock(UsagePointState.class);
        when(this.lifeCycle.getStates()).thenReturn(Arrays.asList(anotherState, testInstance));

        testInstance.remove();

        verify(stateMachineUpdater).removeState(this.fsmState);
        verify(stateMachineUpdater).complete();
    }

    @Test(expected = UsagePointStateRemoveException.class)
    public void testCanNotRemoveStateWithTransitions() {
        UsagePointTransition transition = mock(UsagePointTransition.class);
        UsagePointState testInstance = getTestInstance();
        when(transition.getFrom()).thenReturn(testInstance);
        UsagePointState toState = mock(UsagePointState.class);
        when(transition.getTo()).thenReturn(toState);
        when(this.lifeCycle.getTransitions()).thenReturn(Collections.singletonList(transition));

        testInstance.remove();
    }

    @Test(expected = UsagePointStateRemoveException.class)
    public void testCanNotRemoveTheLatestState() {
        UsagePointState testInstance = getTestInstance();
        when(this.lifeCycle.getStates()).thenReturn(Collections.singletonList(testInstance));

        testInstance.remove();
    }

    @Test(expected = UsagePointStateRemoveException.class)
    public void testCanNotRemoveTheInitialState() {
        UsagePointState anotherState = mock(UsagePointState.class);
        UsagePointState testInstance = getTestInstance();
        when(this.lifeCycle.getStates()).thenReturn(Arrays.asList(anotherState, testInstance));
        when(testInstance.isInitial()).thenReturn(true);

        testInstance.remove();
    }

    @Test
    public void testStage() {
        assertThat(getTestInstance().getStage().getKey()).isEqualTo(UsagePointStage.Key.OPERATIONAL);
    }
}
