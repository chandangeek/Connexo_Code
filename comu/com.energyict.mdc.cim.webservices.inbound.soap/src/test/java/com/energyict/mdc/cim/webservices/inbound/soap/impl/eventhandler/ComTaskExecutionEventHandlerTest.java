/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler.ComTaskExecutionEventHandler.EventType.MANUAL_COMTASKEXECUTION_COMPLETED;
import static com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler.ComTaskExecutionEventHandler.EventType.MANUAL_COMTASKEXECUTION_FAILED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComTaskExecutionEventHandlerTest {
    private final String COM_TASK_NAME = "stub_task";

    private Clock clock = mock(Clock.class);
    private LocalEvent event = mock(LocalEvent.class);
    private EventType eventType = mock(EventType.class);
    private Finder<ServiceCall> serviceCallFinder = mock(Finder.class);
    private ServiceCallService serviceCallService = mock(ServiceCallService.class);
    private ServiceCall serviceCall = mock(ServiceCall.class);
    private ComTaskExecutionEventHandler comTaskExecutionEventHandler;
    private DeviceMessageUpdateForLoadProfileEventHandler deviceMessageUpdateForLoadProfileEventHandler;
    private ChildGetMeterReadingsDomainExtension domainExtension = mock(ChildGetMeterReadingsDomainExtension.class);
    private Device device = mock(Device.class);
    private DeviceMessage deviceMessage = mock(DeviceMessage.class);
    private ComTaskExecution devMessageComTaskExecution = createDevMessageComTaskExecutionMock();
    private ComTaskExecution loadProfileComTaskExecution = createLoadProfileComTaskExecutionMock();

    @Before
    public void setUp() {
        comTaskExecutionEventHandler = new ComTaskExecutionEventHandler(clock, serviceCallService);
        deviceMessageUpdateForLoadProfileEventHandler = new DeviceMessageUpdateForLoadProfileEventHandler(serviceCallService, clock);
        when(event.getType()).thenReturn(eventType);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED.topic());
        when(serviceCallService.getServiceCallFinder(any(ServiceCallFilter.class))).thenReturn(serviceCallFinder);
        when(serviceCallFinder.find()).thenReturn(Collections.singletonList(serviceCall));
        when(serviceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)).thenReturn(Optional.of(domainExtension));
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(222));

        when(domainExtension.getTriggerDate()).thenReturn(Instant.ofEpochSecond(111));
        when(domainExtension.getCommunicationTask()).thenReturn(COM_TASK_NAME);
    }

    @Test
    public void comTaskExecutionCompleteDeviceMessageConfirmedTest() {
        createMockDeviceMessage(DeviceMessageStatus.CONFIRMED);
        when(event.getSource()).thenReturn(deviceMessage);
        deviceMessageUpdateForLoadProfileEventHandler.handle(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).log(LogLevel.FINE, String.format("Device message 'stub_task'(id: 0, release date: %s) is confirmed",
                domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void comTaskExecutionCompleteForLoadProfileTest() {
        when(event.getSource()).thenReturn(loadProfileComTaskExecution);
        comTaskExecutionEventHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).log(LogLevel.FINE, String.format("Communication task execution 'stub_task'(trigger date: %s) is completed",
                domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
    }

    @Test
    public void comTaskExecutionCompleteTrigerTimeInFutureTest() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(1));
        when(event.getSource()).thenReturn(loadProfileComTaskExecution);
        comTaskExecutionEventHandler.onEvent(event);
        verify(serviceCall, never()).requestTransition(DefaultState.ONGOING);
        verify(serviceCall, never()).log(LogLevel.FINE, String.format("Communication task execution 'stub_task'(trigger date: %s) is completed",
                domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void comTaskExecutionCompleteDeviceMessageFailedTest() {
        createMockDeviceMessage(DeviceMessageStatus.FAILED);
        when(event.getSource()).thenReturn(deviceMessage);
        deviceMessageUpdateForLoadProfileEventHandler.handle(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).log(LogLevel.SEVERE,
                String.format("Device message 'stub_task'(id: 0, release date: %s) wasn't confirmed",
                        domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void comTaskExecutionCompleteDeviceMessageCancelledTest() {
        createMockDeviceMessage(DeviceMessageStatus.CANCELED);
        when(event.getSource()).thenReturn(deviceMessage);
        deviceMessageUpdateForLoadProfileEventHandler.handle(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).log(LogLevel.FINE, String.format("Device message 'stub_task'(id: 0, release date: %s) is canceled",
                domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
        verify(serviceCall).requestTransition(DefaultState.CANCELLED);
    }

    @Test
    public void comTaskExecutionFailedDeviceMessageFailedTest() {
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_FAILED.topic());
        createMockDeviceMessage(DeviceMessageStatus.FAILED);
        when(event.getSource()).thenReturn(devMessageComTaskExecution);
        comTaskExecutionEventHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).log(LogLevel.SEVERE, String.format("Communication task execution 'stub_task'(trigger date: %s) is failed",
                domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void comTaskExecutionFailedForLoadProdileTest() {
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_FAILED.topic());
        when(event.getSource()).thenReturn(loadProfileComTaskExecution);
        comTaskExecutionEventHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
        verify(serviceCall).log(LogLevel.SEVERE, String.format("Communication task execution 'stub_task'(trigger date: %s) is failed",
                domainExtension.getTriggerDate().atZone(ZoneId.systemDefault())));
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    private void createMockDeviceMessage(DeviceMessageStatus deviceMessageStatus) {
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getId()).thenReturn(16);
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessageSpec.getName()).thenReturn(COM_TASK_NAME);
        when(deviceMessage.getStatus()).thenReturn(deviceMessageStatus);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.ofEpochSecond(111));
        when(device.getMessages()).thenReturn(Collections.singletonList(deviceMessage));
    }

    private ComTaskExecution createDevMessageComTaskExecutionMock() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mock(ComTask.class);
        MessagesTask messagesTask = mock(MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(messagesTask));
        when(comTask.getName()).thenReturn(COM_TASK_NAME);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(messagesTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));
        when(deviceMessageCategory.getId()).thenReturn(16);
        return comTaskExecution;
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
