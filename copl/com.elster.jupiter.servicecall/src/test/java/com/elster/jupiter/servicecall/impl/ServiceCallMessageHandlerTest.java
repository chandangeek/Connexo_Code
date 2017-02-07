/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.HandlerDisappearedException;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 8/4/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceCallMessageHandlerTest {

    private static final Long SERVICE_CALL_ID = 7L;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private JsonService jsonService;
    @Mock
    private IServiceCallService serviceCallService;
    @Mock
    private ServiceCallImpl serviceCall;
    @Mock
    private IServiceCallType serviceCallType;
    @Mock
    private ServiceCallHandler serviceCallHandler;
    @Mock
    private Message message;


    private byte[] bytes;
    private ServiceCallMessageHandler serviceCallMessageHandler;

    @Before
    public void setUp() throws Exception {
        bytes = new byte[1];
        when(message.getPayload()).thenReturn(bytes);
        TransitionNotification transitionNotification = mock(TransitionNotification.class);
        when(transitionNotification.getServiceCallId()).thenReturn(SERVICE_CALL_ID);
        when(transitionNotification.getOldState()).thenReturn(DefaultState.CREATED);
        when(transitionNotification.getNewState()).thenReturn(DefaultState.ONGOING);
        when(jsonService.deserialize(bytes, TransitionNotification.class)).thenReturn(transitionNotification);
        when(serviceCallService.getServiceCall(anyLong())).thenReturn(Optional.empty());
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCallType.getServiceCallHandler()).thenReturn(serviceCallHandler);
        serviceCallMessageHandler = new ServiceCallMessageHandler(jsonService, serviceCallService, thesaurus);
        when(serviceCall.getParent()).thenReturn(Optional.empty());
    }

    @Test
    public void testExceptionLoggedIfNoHandler() throws Exception {
        when(serviceCallType.getServiceCallHandler()).thenThrow(new HandlerDisappearedException(thesaurus, MessageSeeds.HANDLER_DISAPPEARED, "AAA"));
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(nlsMessageFormat.format(any())).thenAnswer(iom -> "The service call type was created with a handler '" + iom
                .getArguments()[0] + "' that can no longer be found in the system");
        when(thesaurus.getFormat(MessageSeeds.HANDLER_DISAPPEARED)).thenReturn(nlsMessageFormat);

        serviceCallMessageHandler.process(message);

        verify(serviceCall).setState(DefaultState.FAILED);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(serviceCall).log(stringArgumentCaptor.capture(), exceptionArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).startsWith("Service call handler failed to process the service call: The service call type was created with a handler 'AAA' that can no longer be found in the system");
        assertThat(exceptionArgumentCaptor.getValue() instanceof HandlerDisappearedException).isTrue();
    }
}
