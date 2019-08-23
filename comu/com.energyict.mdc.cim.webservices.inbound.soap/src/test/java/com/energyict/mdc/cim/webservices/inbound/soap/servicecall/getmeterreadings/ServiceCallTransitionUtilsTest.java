/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ServiceCallTransitionUtilsTest {
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCall childServiceCall1, childServiceCall2;

    @Before
    public void setUp() {
        Finder<ServiceCall> serviceCallFinder = mock(Finder.class);
        when(serviceCallFinder.stream()).then((i) -> Stream.of(childServiceCall1, childServiceCall2));
        when(serviceCall.findChildren()).thenReturn(serviceCallFinder);
    }

    @Test
    public void fromWaitingToSuccessfulTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void fromWaitingToInitiateReadingTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, true);
        verify(serviceCall, times(2)).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.PAUSED);
    }

    @Test
    public void fromWaitingToPartialSuccessfulTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.FAILED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void fromWaitingToPartialSuccessful2Test() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.PARTIAL_SUCCESS);
        when(childServiceCall2.getState()).thenReturn(DefaultState.PARTIAL_SUCCESS);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void fromWaitingToPartialSuccessful3Test() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.PARTIAL_SUCCESS);
        when(childServiceCall2.getState()).thenReturn(DefaultState.FAILED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void fromWaitingToFailedTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.FAILED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.FAILED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void fromWaitingToFailedThrueRejectedTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.REJECTED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.REJECTED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void fromWaitingToFailed2Test() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.FAILED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.REJECTED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void fromWaitingToFailed3Test() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.CANCELLED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.FAILED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void fromWaitingToCancelledTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.CANCELLED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.CANCELLED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.CANCELLED);
    }

    @Test
    public void closeStatesTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.CANCELLED);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);

        when(serviceCall.getState()).thenReturn(DefaultState.FAILED);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);

        when(serviceCall.getState()).thenReturn(DefaultState.PARTIAL_SUCCESS);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);

        when(serviceCall.getState()).thenReturn(DefaultState.CANCELLED);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);

        when(serviceCall.getState()).thenReturn(DefaultState.REJECTED);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);

        when(serviceCall.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall1.getState()).thenReturn(DefaultState.FAILED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.FAILED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void childrenOpenedStatesTest() {
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.ONGOING);
        when(childServiceCall2.getState()).thenReturn(DefaultState.ONGOING);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);

        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.PENDING);
        when(childServiceCall2.getState()).thenReturn(DefaultState.PENDING);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.PENDING);

        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.PAUSED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.PAUSED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.PAUSED);

        when(serviceCall.getState()).thenReturn(DefaultState.CREATED);
        when(childServiceCall1.getState()).thenReturn(DefaultState.SCHEDULED);
        when(childServiceCall2.getState()).thenReturn(DefaultState.SCHEDULED);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.SCHEDULED);

        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall1.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall2.getState()).thenReturn(DefaultState.WAITING);
        ServiceCallTransitionUtils.resultTransition(serviceCall, false);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).requestTransition(DefaultState.WAITING);
    }
}
