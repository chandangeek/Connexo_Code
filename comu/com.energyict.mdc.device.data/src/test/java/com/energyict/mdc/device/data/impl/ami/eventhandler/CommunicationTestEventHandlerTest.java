/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.CommunicationTestServiceCallHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommunicationTestEventHandlerTest {


    private static final long SERVICE_CALL_ID = 1;

    @Mock
    private Device device;
    @Mock
    private Meter meter;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private JsonService jsonService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Finder finder;
    private CommunicationTestServiceCallDomainExtension communicationTestServiceCallDomainExtension;

    @Before
    public void setUp() throws Exception {
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCallType.getName()).thenReturn(CommunicationTestServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        communicationTestServiceCallDomainExtension = new CommunicationTestServiceCallDomainExtension();
        communicationTestServiceCallDomainExtension.setExpectedTasks(BigDecimal.ONE);
        communicationTestServiceCallDomainExtension.setCompletedTasks(BigDecimal.ZERO);
        communicationTestServiceCallDomainExtension.setSuccessfulTasks(BigDecimal.ZERO);
        communicationTestServiceCallDomainExtension.setTriggerDate(Instant.MIN);
        when(serviceCall.getExtensionFor(any(CommunicationTestServiceCallCustomPropertySet.class))).thenReturn(Optional.of(communicationTestServiceCallDomainExtension));
        when(serviceCall.getExtension(any())).thenReturn(Optional.of(communicationTestServiceCallDomainExtension));
        when(serviceCallService.lockServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.findServiceCalls(eq(device), any())).thenReturn(Collections.singleton(serviceCall));
        when(serviceCallService.getServiceCallFinder(any())).thenReturn(finder);
        when(finder.find()).thenReturn(Collections.singletonList(serviceCall));
        when(deviceService.findDeviceById(333)).thenReturn(Optional.of(device));
        when(meteringService.findMeterById(1)).thenReturn(Optional.of(meter));
        when(meter.getAmrId()).thenReturn("333");
    }

    @Test
    public void testCommunicationTestSuccess() throws Exception {
        CommunicationTestEventHandler handler = new CommunicationTestEventHandler(jsonService, deviceService, serviceCallService, meteringService);
        Message messageSuccess = mock(Message.class);
        byte[] payload = new byte[1];
        when(messageSuccess.getPayload()).thenReturn(payload);
        Map<String, String> messagePropertiesSuccess = new HashMap<>();
        messagePropertiesSuccess.put("event.topics", "com/energyict/mdc/connectiontask/COMPLETION");
        messagePropertiesSuccess.put("meterId", "1");
        messagePropertiesSuccess.put("timestamp", "1");
        messagePropertiesSuccess.put("failedTaskIDs", "");
        messagePropertiesSuccess.put("skippedTaskIDs", "");
        when(jsonService.deserialize(eq(payload), any())).thenReturn(messagePropertiesSuccess);

        // Business method
        handler.process(messageSuccess);

        when(serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.FAILED)).thenReturn(true);

        // Asserts
        verify(serviceCall, times(1)).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testCommunicationTestFail() throws Exception {
        CommunicationTestEventHandler handler = new CommunicationTestEventHandler(jsonService, deviceService, serviceCallService, meteringService);
        Message message = mock(Message.class);
        byte[] payload = new byte[1];
        when(message.getPayload()).thenReturn(payload);
        Map<String, String> messageProperties = new HashMap<>();
        messageProperties.put("event.topics", "com/energyict/mdc/connectiontask/COMPLETION");
        messageProperties.put("deviceIdentifier", "333");
        messageProperties.put("timestamp", "1");
        messageProperties.put("failedTaskIDs", "123");
        messageProperties.put("skippedTaskIDs", "");
        when(jsonService.deserialize(eq(payload), any())).thenReturn(messageProperties);

        when(serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.FAILED)).thenReturn(true);

        // Business method
        handler.process(message);

        // Asserts
        verify(serviceCall, times(1)).requestTransition(DefaultState.FAILED);
    }
}
