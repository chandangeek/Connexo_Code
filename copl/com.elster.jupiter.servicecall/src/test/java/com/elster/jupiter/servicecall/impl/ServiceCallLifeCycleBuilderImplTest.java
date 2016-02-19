package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Provider;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallLifeCycleBuilderImplTest {

    private static final String NAME = "theName";

    private FiniteStateMachineBuilder finiteStateMachineBuilder;

    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private FiniteStateMachineService finiteStateMachineService;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private Provider<ServiceCallLifeCycleImpl> serviceCallLifeCycleProvider;
    @Mock
    private com.elster.jupiter.fsm.CustomStateTransitionEventType customStateTransitionEventType;

    @Before
    public void setUp() {
        finiteStateMachineBuilder = FakeBuilder.initBuilderStub(finiteStateMachine, FiniteStateMachineBuilder.class, FiniteStateMachineBuilder.StateBuilder.class, FiniteStateMachineBuilder.TransitionBuilder.class);
        when(finiteStateMachineService.newFiniteStateMachine(anyString())).thenReturn(finiteStateMachineBuilder);
        when(finiteStateMachineService.findCustomStateTransitionEventType(anyString())).thenReturn(Optional.of(customStateTransitionEventType));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testBuildingWithoutTransitionFromPendingToCanceled() {

        ServiceCallLifeCycleBuilder builder = new ServiceCallLifeCycleBuilderImpl(finiteStateMachineService, serviceCallService, serviceCallLifeCycleProvider)
                .setName(NAME);

        builder.removeTransition(DefaultState.PENDING, DefaultState.CANCELLED);

        ServiceCallLifeCycle serviceCallLifeCycle = builder.create();

    }


}