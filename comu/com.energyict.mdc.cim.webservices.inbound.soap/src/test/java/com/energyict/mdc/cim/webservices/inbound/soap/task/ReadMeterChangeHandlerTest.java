/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadMeterChangeHandlerTest {
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCall childServiceCall_1;
    @Mock
    private ServiceCall childServiceCall_2;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    protected ServiceCallService serviceCallService;
    @Mock
    protected JsonService jsonService;
    @Mock
    protected TransactionService transactionService;
    @Mock
    protected TransactionContext transactionContext;
    @Mock
    private Message message;

    private CompletionMessageInfo completionMessageInfo;
    private ReadMeterChangeHandler readMeterChangeHandler;

    @Before
    public void initMocks() {
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(serviceCallService.findServiceCallType(anyString(), anyString())).thenReturn(Optional.of(serviceCallType));

        long serviceCallId = 1;
        when(serviceCall.getId()).thenReturn(serviceCallId);
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCallType.getName()).thenReturn("ServiceCallType");

        when(childServiceCall_1.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(childServiceCall_2.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(childServiceCall_1.getType()).thenReturn(serviceCallType);
        when(childServiceCall_2.getType()).thenReturn(serviceCallType);
        when(childServiceCall_1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCall_2.getState()).thenReturn(DefaultState.SUCCESSFUL);

        Finder<ServiceCall> childServiceCallFinder = mock(Finder.class);
        List<ServiceCall> children = new ArrayList<>(2);
        children.add(childServiceCall_1);
        children.add(childServiceCall_2);
        when(childServiceCallFinder.stream()).thenReturn(children.stream());
        when(serviceCall.findChildren()).thenReturn(childServiceCallFinder);
        when(serviceCallService.getServiceCall(serviceCallId)).thenReturn(Optional.of(serviceCall));

        completionMessageInfo = new CompletionMessageInfo(Long.toString(serviceCallId));
        completionMessageInfo.setCompletionMessageStatus(CompletionMessageInfo.CompletionMessageStatus.FAILURE);
        when(jsonService.serialize(any())).then(i -> i.getArgumentAt(0, CompletionMessageInfo.class).toString());
        when(jsonService.deserialize(any(byte[].class), any(Class.class))).thenReturn(completionMessageInfo);

        readMeterChangeHandler = new ReadMeterChangeHandler(jsonService, serviceCallService, transactionService);
    }

    @Test
    public void testSuccessCase() {
        readMeterChangeHandler.onMessageDelete(message);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.PAUSED);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
    }

    @Test
    public void testPartialSuccessCase() {
        when(childServiceCall_2.getState()).thenReturn(DefaultState.FAILED);
        readMeterChangeHandler.onMessageDelete(message);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.PARTIAL_SUCCESS);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
    }

    @Test
    public void testFailedCase() {
        when(childServiceCall_1.getState()).thenReturn(DefaultState.FAILED);
        when(childServiceCall_2.getState()).thenReturn(DefaultState.FAILED);
        readMeterChangeHandler.onMessageDelete(message);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.FAILED);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
    }

    @Test
    public void testCancelledCase() {
        when(childServiceCall_1.getState()).thenReturn(DefaultState.CANCELLED);
        when(childServiceCall_2.getState()).thenReturn(DefaultState.CANCELLED);
        readMeterChangeHandler.onMessageDelete(message);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, atLeastOnce()).requestTransition(DefaultState.CANCELLED);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
    }
}
