/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigDomainExtension;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterStatusHandlerTest {

    private static final String COM_TASK_NAME = "Com task name";
    private static final String DEVICE_NAME = "Device name";
    @Mock
    private Finder<ServiceCall> serviceCallFinder;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private MeterConfigDomainExtension meterConfigDomainExtension;
    @Mock
    private ComTask comTask;
    @Mock
    private LocalEvent notification;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private Device device;
    @Mock
    private EventType eventTypeCompleted, eventTypefailed;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private MeterStatusHandler meterStatusHandler;

    @Before
    public void setUp() {
        when(serviceCallService.getServiceCallFinder(any(ServiceCallFilter.class))).thenReturn(serviceCallFinder);
        when(serviceCallService.lockServiceCall(anyLong())).thenReturn(Optional.of(serviceCall));
        when(serviceCallFinder.find()).thenReturn(Collections.singletonList(serviceCall));
        when(serviceCall.getExtension(MeterConfigDomainExtension.class)).thenReturn(Optional.of(meterConfigDomainExtension));
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(meterConfigDomainExtension.getCommunicationTask()).thenReturn(Optional.of(comTask));
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getName()).thenReturn(COM_TASK_NAME);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(notification.getSource()).thenReturn(comTaskExecution);
        when(eventTypeCompleted.getTopic()).thenReturn("com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED");
        when(eventTypefailed.getTopic()).thenReturn("com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED");
        meterStatusHandler = new MeterStatusHandler(serviceCallService, thesaurus);
    }

    @Test
    public void successTest() {
        when(notification.getType()).thenReturn(eventTypeCompleted);
        meterStatusHandler.handle(notification);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void failTest() {
        when(notification.getType()).thenReturn(eventTypefailed);
        meterStatusHandler.handle(notification);
        verify(meterConfigDomainExtension).setErrorMessage(thesaurus.getSimpleFormat(MessageSeeds.COM_TASK_FAILED).format(COM_TASK_NAME, DEVICE_NAME));
        verify(meterConfigDomainExtension).setErrorCode(MessageSeeds.COM_TASK_FAILED.getErrorCode());
        verify(serviceCall).update(meterConfigDomainExtension);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }
}
