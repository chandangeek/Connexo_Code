/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.ConnectServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.EnableLoadLimitServiceCallHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 14/06/2016 - 16:44
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivatedBreakerStatusUpdateEventHandlerTest {


    private static final long SERVICE_CALL_ID = 1;

    @Mock
    private Device device;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private Finder<ServiceCall> serviceCallFinder;
    @Mock
    private ServiceCallService serviceCallService;
    private CommandServiceCallDomainExtension commandServiceCallDomainExtension;

    @Before
    public void setUp() throws Exception {
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        commandServiceCallDomainExtension = new CommandServiceCallDomainExtension();
        commandServiceCallDomainExtension.setNrOfUnconfirmedDeviceCommands(1);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(commandServiceCallDomainExtension));
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.getServiceCallFinder(any(ServiceCallFilter.class))).thenReturn(serviceCallFinder);
        when(serviceCallFinder.find()).thenReturn(Collections.singletonList(serviceCall));
    }

    @Test
    public void testDeleteActionsAreDiscarded() throws Exception {
        ActivatedBreakerStatusUpdateEventHandler handler = new ActivatedBreakerStatusUpdateEventHandler(serviceCallService);
        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        LocalEvent localEvent = createEventFor(breakerStatus, EventType.ACTIVATED_BREAKER_STATUS_DELETED.topic());

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(localEvent, never()).getSource();
    }


    @Test
    public void testBreakerStatusUpdatedNotPartOfContactorOperation() throws Exception {
        when(serviceCallType.getName()).thenReturn(EnableLoadLimitServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        ActivatedBreakerStatusUpdateEventHandler handler = new ActivatedBreakerStatusUpdateEventHandler(serviceCallService);
        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(breakerStatus, EventType.ACTIVATED_BREAKER_STATUS_UPDATED.topic());

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testBreakerStatusUpdatedOutsideReadStatusInformationCommandOperation() throws Exception {
        when(serviceCallType.getName()).thenReturn(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        ActivatedBreakerStatusUpdateEventHandler handler = new ActivatedBreakerStatusUpdateEventHandler(serviceCallService);
        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(breakerStatus, EventType.ACTIVATED_BREAKER_STATUS_UPDATED.topic());

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testBreakerStatusUpdatedBeforeMessageReleaseDate() throws Exception {
        commandServiceCallDomainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        commandServiceCallDomainExtension.setReleaseDate(Instant.now());
        when(serviceCallType.getName()).thenReturn(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        ActivatedBreakerStatusUpdateEventHandler handler = new ActivatedBreakerStatusUpdateEventHandler(serviceCallService);
        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getLastChecked()).thenReturn(Instant.ofEpochSecond(commandServiceCallDomainExtension.getReleaseDate()
                .getEpochSecond() - 100));  // To ensure instant is before commandServiceCallDomainExtension.getReleaseDate()
        when(breakerStatus.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(breakerStatus, EventType.ACTIVATED_BREAKER_STATUS_UPDATED.topic());

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testBreakerStatusUpdated() throws Exception {
        commandServiceCallDomainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        when(serviceCallType.getName()).thenReturn(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        ActivatedBreakerStatusUpdateEventHandler handler = new ActivatedBreakerStatusUpdateEventHandler(serviceCallService);
        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getLastChecked()).thenReturn(Instant.now());
        when(breakerStatus.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(breakerStatus, EventType.ACTIVATED_BREAKER_STATUS_UPDATED.topic());
        commandServiceCallDomainExtension.setReleaseDate(Instant.ofEpochSecond(breakerStatus.getLastChecked().getEpochSecond() - 100));  // To ensure instant is before last checked of breakerStatus

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, times(1)).requestTransition(DefaultState.ONGOING);
    }

    private LocalEvent createEventFor(ActivatedBreakerStatus breakerStatus, String eventTopic) {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(eventTopic);
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getSource()).thenReturn(breakerStatus);
        when(localEvent.getType()).thenReturn(eventType);
        return localEvent;
    }
}