/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventhandlers;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.FileSystem;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomSAPDeviceEventHandlerTest {

    private final static String EVENT_CODE = "1.2.3.4";
    private final static String DEVICE_EVENT_TYPE = "1";

    @Mock
    private MeterEventCreateRequestProvider meterEventCreateRequestProvider;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock
    private Clock clock;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private UserService userService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private EndDeviceEventRecord eventRecord;
    @Mock
    private EndDevice endDevice;

    private CustomSAPDeviceEventHandler handler;
    private LocalDate date = LocalDate.of(2022, 7, 29);
    private Instant currentTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

    @Before
    public void initMocks() {
        mockEventRecord(eventRecord);
        when(meterEventCreateRequestProvider.createBulkMessage(any(EndDeviceEventRecord.class))).thenReturn(Optional.empty());
        handler = new CustomSAPDeviceEventHandler(meterEventCreateRequestProvider, sapCustomPropertySets, clock, fileSystem,
                serviceCallService, thesaurus, propertySpecService, userService, threadPrincipalService, transactionService);
    }

    @Test
    public void sendEventTest() {
        when(sapCustomPropertySets.isPushEventsToSapFlagSet(endDevice)).thenReturn(true);
        handler.handle(eventRecord);
        verify(meterEventCreateRequestProvider, times(1)).createBulkMessage(any(EndDeviceEventRecord.class));
    }

    @Test
    public void notSendEventTest() {
        when(sapCustomPropertySets.isPushEventsToSapFlagSet(endDevice)).thenReturn(false);
        handler.handle(eventRecord);
        verify(meterEventCreateRequestProvider, times(0)).createBulkMessage(any(EndDeviceEventRecord.class));
    }

    private void mockEventRecord(EndDeviceEventRecord eventRecord) {
        when(eventRecord.getEventTypeCode()).thenReturn(EVENT_CODE);
        when(eventRecord.getDeviceEventType()).thenReturn(DEVICE_EVENT_TYPE);
        when(eventRecord.getEndDevice()).thenReturn(endDevice);
        when(eventRecord.getCreatedDateTime()).thenReturn(currentTime);
    }
}