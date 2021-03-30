/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.UpdateCreditAmountServiceCallHandler;

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

@RunWith(MockitoJUnitRunner.class)
public class CreditAmountUpdateEventHandlerTest {
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
    public void testCreditAmountUpdatedOutsideReadStatusInformationCommandOperation() throws Exception {
        when(serviceCallType.getName()).thenReturn(UpdateCreditAmountServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        CreditAmountUpdateEventHandler handler = new CreditAmountUpdateEventHandler(serviceCallService);
        CreditAmount creditAmount = mock(CreditAmount.class);
        when(creditAmount.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(creditAmount, EventType.CREDIT_AMOUNT_UPDATED.topic());

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testCreditAmountUpdatedBeforeMessageReleaseDate() throws Exception {
        commandServiceCallDomainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        commandServiceCallDomainExtension.setReleaseDate(Instant.now());
        when(serviceCallType.getName()).thenReturn(UpdateCreditAmountServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        CreditAmountUpdateEventHandler handler = new CreditAmountUpdateEventHandler(serviceCallService);
        CreditAmount creditAmount = mock(CreditAmount.class);
        when(creditAmount.getLastChecked()).thenReturn(Instant.ofEpochSecond(commandServiceCallDomainExtension.getReleaseDate()
                .getEpochSecond() - 100));  // To ensure instant is before commandServiceCallDomainExtension.getReleaseDate()
        when(creditAmount.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(creditAmount, EventType.CREDIT_AMOUNT_UPDATED.topic());

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testCreditAmountUpdated() throws Exception {
        commandServiceCallDomainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        when(serviceCallType.getName()).thenReturn(UpdateCreditAmountServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        CreditAmountUpdateEventHandler handler = new CreditAmountUpdateEventHandler(serviceCallService);
        CreditAmount creditAmount = mock(CreditAmount.class);
        when(creditAmount.getLastChecked()).thenReturn(Instant.now());
        when(creditAmount.getDevice()).thenReturn(device);
        LocalEvent localEvent = createEventFor(creditAmount, EventType.CREDIT_AMOUNT_UPDATED.topic());
        commandServiceCallDomainExtension.setReleaseDate(Instant.ofEpochSecond(creditAmount.getLastChecked().getEpochSecond() - 100));  // To ensure instant is before last checked of creditAmount

        // Business method
        handler.handle(localEvent);

        // Asserts
        verify(serviceCall, times(1)).transitionWithLockIfPossible(DefaultState.ONGOING);
    }

    private LocalEvent createEventFor(CreditAmount creditAmount, String eventTopic) {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(eventTopic);
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getSource()).thenReturn(creditAmount);
        when(localEvent.getType()).thenReturn(eventType);
        return localEvent;
    }
}
