package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 21/06/2016 - 14:48
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationHandlerTest {

    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCall childServiceCall_1;
    @Mock
    private ServiceCall childServiceCall_2;
    @Mock
    private ServiceCallType serviceCallType;

    private OperationHandler operationHandler;

    @Before
    public void setUp() throws Exception {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(childServiceCall_1.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(childServiceCall_2.canTransitionTo(any(DefaultState.class))).thenReturn(true);

        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(childServiceCall_1.getType()).thenReturn(serviceCallType);
        when(childServiceCall_2.getType()).thenReturn(serviceCallType);
        when(serviceCallType.getName()).thenReturn("ServiceCallType");

        Finder<ServiceCall> childServiceCallFinder = mock(Finder.class);
        List<ServiceCall> children = new ArrayList<>(2);
        children.add(childServiceCall_1);
        children.add(childServiceCall_2);
        when(childServiceCallFinder.stream()).thenReturn(children.stream());
        when(serviceCall.findChildren()).thenReturn(childServiceCallFinder);
        operationHandler = new OperationHandler();
    }

    @Test
    public void testChildServiceCallFailed() throws Exception {
        // Business method
        operationHandler.onChildStateChange(serviceCall, childServiceCall_1, DefaultState.ONGOING, DefaultState.FAILED);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testChildServiceCallCancelled() throws Exception {
        // Business method
        operationHandler.onChildStateChange(serviceCall, childServiceCall_1, DefaultState.ONGOING, DefaultState.CANCELLED);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.CANCELLED);
    }

    @Test
    public void testPartOfChildServiceCallSuccessful() throws Exception {
        when(childServiceCall_1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall_2.getState()).thenReturn(DefaultState.ONGOING);

        // Business method
        operationHandler.onChildStateChange(serviceCall, childServiceCall_1, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        // Asserts
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testAllChildServiceCallSuccessful() throws Exception {
        when(childServiceCall_1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall_2.getState()).thenReturn(DefaultState.SUCCESSFUL);

        // Business method
        operationHandler.onChildStateChange(serviceCall, childServiceCall_1, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testServiceCallCancelled() throws Exception {
        // Business method
        operationHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.CANCELLED);

        // Asserts
        verify(childServiceCall_1).requestTransition(DefaultState.CANCELLED);
        verify(childServiceCall_2).requestTransition(DefaultState.CANCELLED);
    }
}