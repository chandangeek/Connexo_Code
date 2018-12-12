/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 19/07/2016 - 13:29
 */
@RunWith(MockitoJUnitRunner.class)
public class CompletionOptionsCallBackImplTest {

    private static final String DESTINATION_SPEC_NAME = "DESTINATION_SPEC_NAME";
    private static final String DESTINATION_IDENTIFICATION = "DESTINATION_IDENTIFICATION";

    @Mock
    private JsonService jsonService;
    @Mock
    private MessageService messageService;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private CompletionOptionsServiceCallDomainExtension domainExtension;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private MessageBuilder messageBuilder;

    private CompletionOptionsCallBack completionOptionsCallBack;

    @Before
    public void setUp() throws Exception {
        when(serviceCall.getExtensionFor(any(CompletionOptionsCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));
        when(domainExtension.getDestinationSpec()).thenReturn(DESTINATION_SPEC_NAME);
        when(domainExtension.getDestinationIdentification()).thenReturn(DESTINATION_IDENTIFICATION);

        when(messageService.getDestinationSpec(DESTINATION_SPEC_NAME)).thenReturn(Optional.of(destinationSpec));
        when(jsonService.serialize(any())).then(i -> i.getArgumentAt(0, CompletionMessageInfo.class).toString());
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);

        completionOptionsCallBack = new CompletionOptionsCallBackImpl(jsonService, messageService);
    }

    @Test
    public void sendFinishedMessageToDestinationSpecTest() throws Exception {
        // Business method
        completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_FAILED);

        // Asserts
        verify(destinationSpec).message("CompletionMessageInfo{correlationId='DESTINATION_IDENTIFICATION', completionMessageStatus=FAILURE, failureReason=ONE_OR_MORE_DEVICE_COMMANDS_FAILED}");

    }

    @Test
    public void sendFinishedMessageToDestinationSpecNotFoundTest() throws Exception {
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.empty());

        // Business method
        completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_FAILED);

        // Asserts
        verify(serviceCall).log(LogLevel.SEVERE, "Failed to send message to destination spec: could not find active destination spec with name DESTINATION_SPEC_NAME");
    }
}