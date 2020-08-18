/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsDomainExtension;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.device.data.DeviceService;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FutureComTaskExecutionHandlerTest {

    private final String COM_TASK_NAME = "stub_task";
    private final String DEVICE_MRID = "device_mrid";

    private Clock clock = mock(Clock.class);
    private Finder<ServiceCall> serviceCallFinder = mock(Finder.class);
    private ServiceCallService serviceCallService = mock(ServiceCallService.class);
    private ServiceCall childServiceCall = mock(ServiceCall.class);
    private ServiceCall subParentServiceCall = mock(ServiceCall.class);
    private DeviceService deviceService = mock(DeviceService.class);
    private ChildGetMeterReadingsDomainExtension childDomainExtension = mock(ChildGetMeterReadingsDomainExtension.class);
    private SubParentGetMeterReadingsDomainExtension subParentDomainExtension = mock(SubParentGetMeterReadingsDomainExtension.class);
    private Device device = mock(Device.class);
    private ComTaskExecution loadProfileComTaskExecution = createLoadProfileComTaskExecutionMock();
    private FutureComTaskExecutionHandler futureComTaskExecutionHandler;


    @Before
    public void setUp() {
        when(serviceCallService.getServiceCallFinder(any(ServiceCallFilter.class))).thenReturn(serviceCallFinder);
        when(serviceCallFinder.find()).thenReturn(Collections.singletonList(childServiceCall));
        when(serviceCallFinder.stream()).then((i) -> Stream.of(childServiceCall));
        when(childDomainExtension.getTriggerDate()).thenReturn(Instant.ofEpochSecond(111));
        when(childDomainExtension.getCommunicationTask()).thenReturn(COM_TASK_NAME);
        when(childServiceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)).thenReturn(Optional.of(childDomainExtension));
        when(childServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(childServiceCall.getParent()).thenReturn(Optional.of(subParentServiceCall));

        when(subParentDomainExtension.getEndDeviceMrid()).thenReturn(DEVICE_MRID);
        when(subParentServiceCall.getExtension(SubParentGetMeterReadingsDomainExtension.class)).thenReturn(Optional.of(subParentDomainExtension));

        when(deviceService.findDeviceByMrid(anyString())).thenReturn(Optional.of(device));

        when(clock.instant()).thenReturn(Instant.ofEpochSecond(222));
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(loadProfileComTaskExecution));

        futureComTaskExecutionHandler = new FutureComTaskExecutionHandler(clock, serviceCallService, deviceService);
    }

    @Test
    public void comTaskExecutionInThePastTest() {
        futureComTaskExecutionHandler.execute(null);
        verify(childServiceCall).requestTransition(DefaultState.PENDING);
        verify(childServiceCall).requestTransition(DefaultState.ONGOING);
        verify(childServiceCall).requestTransition(DefaultState.WAITING);
        verify(loadProfileComTaskExecution).runNow();
    }

    @Test
    public void comTaskExecutionInTheFutureTest() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(1));
        futureComTaskExecutionHandler.execute(null);
        verify(childServiceCall, never()).requestTransition(DefaultState.PENDING);
        verify(childServiceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(childServiceCall, never()).requestTransition(DefaultState.WAITING);
        verify(loadProfileComTaskExecution, never()).runNow();
    }

    @Test
    public void noDomainExtensionFailTest() {
        when(childServiceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)).thenReturn(Optional.empty());
        try {
            futureComTaskExecutionHandler.execute(null);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assert (e.getMessage().equals("Unable to get domain extension for child service call"));
        }
    }

    @Test
    public void noDeviceFailTest() {
        when(deviceService.findDeviceByMrid(anyString())).thenReturn(Optional.empty());
        futureComTaskExecutionHandler.execute(null);
        verify(childServiceCall).log(LogLevel.SEVERE, "Unable to get device for mrid device_mrid");
        verify(childServiceCall).requestTransition(DefaultState.ONGOING);
        verify(childServiceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void noComTaskExecutionFailTest() {
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        futureComTaskExecutionHandler.execute(null);
        verify(childServiceCall).log(LogLevel.SEVERE, "The communication task required for the read-out not found on the device");
        verify(childServiceCall).requestTransition(DefaultState.ONGOING);
        verify(childServiceCall).requestTransition(DefaultState.FAILED);
    }

    private ComTaskExecution createLoadProfileComTaskExecutionMock() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mock(ComTask.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(loadProfilesTask));
        when(comTask.getName()).thenReturn(COM_TASK_NAME);
        when(comTaskExecution.getDevice()).thenReturn(device);
        return comTaskExecution;
    }
}
