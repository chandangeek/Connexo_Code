/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.ConnectServiceCallHandler;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import org.osgi.service.event.Event;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 14/06/2016 - 15:59
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageUpdateEventHandlerTest {

    private static final long SERVICE_CALL_ID = 1;
    private static final String NON_HEADEND_SERVICECALL_TYPE = "NON_HEADEND_TYPE";

    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    CompletionOptionsCallBack completionOptionsCallBack;

    @Before
    public void setUp() throws Exception {
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        CommandServiceCallDomainExtension commandServiceCallDomainExtension = new CommandServiceCallDomainExtension();
        commandServiceCallDomainExtension.setNrOfUnconfirmedDeviceCommands(1);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(commandServiceCallDomainExtension));
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
    }

    @Test
    public void testDeviceMessageWithoutTrackingInfo() throws Exception {
        DeviceMessageUpdateEventHandler handler = new DeviceMessageUpdateEventHandler(serviceCallService, completionOptionsCallBack);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        LocalEvent localEvent = createEventFor(deviceMessage, DeviceMessageStatus.CONFIRMED);

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(deviceMessage, never()).getStatus();
    }

    @Test
    public void testDeviceMessageWithServiceCallNotRelatedToHeadEndInterface() throws Exception {
        when(serviceCallType.getName()).thenReturn(NON_HEADEND_SERVICECALL_TYPE);

        DeviceMessageUpdateEventHandler handler = new DeviceMessageUpdateEventHandler(serviceCallService, completionOptionsCallBack);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getTrackingCategory()).thenReturn(TrackingCategory.serviceCall);
        when(deviceMessage.getTrackingId()).thenReturn(Long.toString(SERVICE_CALL_ID));
        LocalEvent localEvent = createEventFor(deviceMessage, DeviceMessageStatus.CONFIRMED);

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(deviceMessage, never()).getStatus();
    }

    @Test
    public void testDeviceMessageStatusHasNotChanged() throws Exception {
        DeviceMessageUpdateEventHandler handler = new DeviceMessageUpdateEventHandler(serviceCallService, completionOptionsCallBack);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(deviceMessage.getTrackingCategory()).thenReturn(TrackingCategory.serviceCall);
        when(deviceMessage.getTrackingId()).thenReturn(Long.toString(SERVICE_CALL_ID));
        LocalEvent localEvent = createEventFor(deviceMessage, DeviceMessageStatus.PENDING);

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(deviceMessage, never()).getStatus();
    }

    @Test
    public void testDeviceMessageConfirmed() throws Exception {
        when(serviceCallType.getName()).thenReturn(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME);

        DeviceMessageUpdateEventHandler handler = new DeviceMessageUpdateEventHandler(serviceCallService, completionOptionsCallBack);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(deviceMessage.getTrackingCategory()).thenReturn(TrackingCategory.serviceCall);
        when(deviceMessage.getTrackingId()).thenReturn(Long.toString(SERVICE_CALL_ID));
        LocalEvent localEvent = createEventFor(deviceMessage, DeviceMessageStatus.PENDING);

        // Business method
        handler.handle(localEvent);
        ArgumentCaptor<CommandServiceCallDomainExtension> domainExtensionArgumentCaptor = ArgumentCaptor.forClass(CommandServiceCallDomainExtension.class);
        verify(serviceCall).update(domainExtensionArgumentCaptor.capture());

        // Asserts
        assertEquals(0, domainExtensionArgumentCaptor.getValue().getNrOfUnconfirmedDeviceCommands());
        verify(serviceCall, times(1)).requestTransition(DefaultState.ONGOING);
    }

    @Test
    public void testDeviceMessageFailed() throws Exception {
        when(serviceCallType.getName()).thenReturn(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME);

        DeviceMessageUpdateEventHandler handler = new DeviceMessageUpdateEventHandler(serviceCallService, completionOptionsCallBack);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(deviceMessage.getTrackingCategory()).thenReturn(TrackingCategory.serviceCall);
        when(deviceMessage.getTrackingId()).thenReturn(Long.toString(SERVICE_CALL_ID));
        LocalEvent localEvent = createEventFor(deviceMessage, DeviceMessageStatus.PENDING);

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).update(any(CommandServiceCallDomainExtension.class));
        verify(serviceCall, times(1)).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, times(1)).requestTransition(DefaultState.FAILED);
        verify(completionOptionsCallBack, times(1)).sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_FAILED);
    }

    @Test
    public void testDeviceMessageRevoked() throws Exception {
        when(serviceCallType.getName()).thenReturn(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME);

        DeviceMessageUpdateEventHandler handler = new DeviceMessageUpdateEventHandler(serviceCallService, completionOptionsCallBack);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.REVOKED);
        when(deviceMessage.getTrackingCategory()).thenReturn(TrackingCategory.serviceCall);
        when(deviceMessage.getTrackingId()).thenReturn(Long.toString(SERVICE_CALL_ID));
        LocalEvent localEvent = createEventFor(deviceMessage, DeviceMessageStatus.PENDING);

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).update(any(CommandServiceCallDomainExtension.class));
        verify(serviceCall, times(1)).requestTransition(DefaultState.CANCELLED);
        verify(completionOptionsCallBack, times(1)).sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.CANCELLED, CompletionMessageInfo.FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_HAVE_BEEN_REVOKED);
    }

    private LocalEvent createEventFor(DeviceMessage deviceMessage, DeviceMessageStatus oldDeviceMessageStatus) {
        Map<String, Integer> properties = Collections.singletonMap(DeviceMessageUpdateEventHandler.OLD_OBIS_CODE_PROPERTY_NAME, oldDeviceMessageStatus.ordinal());
        Event event = new Event("topic", properties);

        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getSource()).thenReturn(deviceMessage);
        when(localEvent.toOsgiEvent()).thenReturn(event);
        return localEvent;
    }
}