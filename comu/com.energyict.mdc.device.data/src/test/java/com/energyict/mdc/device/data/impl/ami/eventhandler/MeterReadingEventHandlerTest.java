package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;

import java.math.BigDecimal;
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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 14/06/2016 - 16:44
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterReadingEventHandlerTest {


    private static final long SERVICE_CALL_ID = 1;

    @Mock
    private Device device;
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
    private OnDemandReadServiceCallDomainExtension onDemandReadServiceCallDomainExtension;


    @Before
    public void setUp() throws Exception {
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCallType.getName()).thenReturn(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        onDemandReadServiceCallDomainExtension = new OnDemandReadServiceCallDomainExtension();
        onDemandReadServiceCallDomainExtension.setExpectedTasks(BigDecimal.ONE);
        onDemandReadServiceCallDomainExtension.setCompletedTasks(BigDecimal.ZERO);
        onDemandReadServiceCallDomainExtension.setSuccessfulTasks(BigDecimal.ZERO);
        onDemandReadServiceCallDomainExtension.setTriggerDate(BigDecimal.ZERO);
        when(serviceCall.getExtensionFor(any(OnDemandReadServiceCallCustomPropertySet.class))).thenReturn(Optional.of(onDemandReadServiceCallDomainExtension));
        when(serviceCall.getExtension(any())).thenReturn(Optional.of(onDemandReadServiceCallDomainExtension));
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.findServiceCalls(eq(device), any())).thenReturn(Collections.singleton(serviceCall));
        when(serviceCallService.getServiceCallFinder(any())).thenReturn(finder);
        when(finder.find()).thenReturn(Collections.singletonList(serviceCall));
    }

//    @Test
//    public void testOnDemandReadSuccess() throws Exception {
//        MeterReadingEventHandler handler = new MeterReadingEventHandler(jsonService, deviceService, serviceCallService, meteringService);
//        Message message = mock(Message.class);
//        byte [] payload = new byte[1];
//        when(message.getPayload()).thenReturn(payload);
//        Map<String, String> messageProperties = new HashMap<>();
//        messageProperties.put("event.topic","com/elster/jupiter/metering/meterreading/CREATED");
//        messageProperties.put("meterId","1");
//        messageProperties.put("timestamp","1");
//        when(jsonService.deserialize(eq(payload), any())).thenReturn(messageProperties);
//        when(deviceService.findDeviceById(anyInt())).thenReturn(Optional.of(device));
//
//        Message message = mock(Message.class);
//        byte [] payload = new byte[1];
//        when(message.getPayload()).thenReturn(payload);
//        Map<String, String> messageProperties = new HashMap<>();
//        messageProperties.put("deviceIdentifier","1");
//        messageProperties.put("timestamp","1");
//        when(jsonService.deserialize(eq(payload), any())).thenReturn(messageProperties);
//        when(deviceService.findDeviceById(anyInt())).thenReturn(Optional.of(device));
//
//        when(serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)).thenReturn(true);
//        when(serviceCall.canTransitionTo(DefaultState.FAILED)).thenReturn(true);
//
//        // Business method
//        handler.process(message);
//
//
//
//        // Asserts
//        verify(serviceCall, times(1)).requestTransition(DefaultState.SUCCESSFUL);
//    }

    @Test
    public void testOnDemandReadFail() throws Exception {
        MeterReadingEventHandler handler = new MeterReadingEventHandler(jsonService, deviceService, serviceCallService, meteringService);
        Message message = mock(Message.class);
        byte [] payload = new byte[1];
        when(message.getPayload()).thenReturn(payload);
        Map<String, String> messageProperties = new HashMap<>();
        messageProperties.put("failedTaskIDs","1");
        messageProperties.put("timestamp","1");
        when(jsonService.deserialize(eq(payload), any())).thenReturn(messageProperties);
        when(deviceService.findDeviceById(anyInt())).thenReturn(Optional.of(device));

        when(serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.FAILED)).thenReturn(true);

        // Business method
        handler.process(message);

        // Asserts
        verify(serviceCall, times(1)).requestTransition(DefaultState.FAILED);
    }
}