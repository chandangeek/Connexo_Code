/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.PassiveCalendar;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToUCampaignHandlerTest {

    private final static String MANUAL_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/manualcomtaskexecution/STARTED";
    private final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private final static String TOU_CAMPAIGN_EDITED = "com/energyict/mdc/tou/campaign/toucampaign/EDITED";

    private TimeOfUseCampaignServiceImpl timeOfUseCampaignService = mock(TimeOfUseCampaignServiceImpl.class);
    private Clock clock = mock(Clock.class);
    private ServiceCallService serviceCallService = mock(ServiceCallService.class);
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private TimeOfUseCampaignHandler timeOfUseCampaignHandler;
    private ComTaskExecution calendarComTaskExecution = createCalendarTaskMock();
    private ComTaskExecution verificationComTaskExecution = createVerificationTaskMock();
    private LocalEvent event = mock(LocalEvent.class);
    private EventType eventType = mock(EventType.class);
    private ServiceCall serviceCall = mock(ServiceCall.class);
    private ThreadPrincipalService threadPrincipalService = mock(ThreadPrincipalService.class);
    private TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    private TimeOfUseCampaignItem timeOfUseItem = mock(TimeOfUseItemDomainExtension.class);
    private ServiceCall parentSc = mock(ServiceCall.class);
    private TimeOfUseCampaignDomainExtension timeOfUseCampaign = createMockCampaign("withoutActivation");
    private TimeOfUseCampaignDomainExtension timeOfUseCampaign2 = createMockCampaign("immediately");
    private TimeOfUseItemDomainExtension timeOfUseItemDomainExtension = mock(TimeOfUseItemDomainExtension.class);

    @Before
    public void setUp() {
        when(timeOfUseCampaignService.getCampaignOn(calendarComTaskExecution)).thenReturn(Optional.of(timeOfUseCampaign));
        when(timeOfUseCampaignService.getCampaignOn(verificationComTaskExecution)).thenReturn(Optional.of(timeOfUseCampaign2));
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(timeOfUseItemDomainExtension.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getExtension(TimeOfUseItemDomainExtension.class)).thenReturn(Optional.of(timeOfUseItemDomainExtension));
        when(timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(any())).thenReturn(Optional.of(timeOfUseItem));
        when(serviceCallService.lockServiceCall(anyLong())).thenReturn(Optional.of(serviceCall));
        when(event.getType()).thenReturn(eventType);
        when(timeOfUseItem.cancel(anyBoolean())).thenReturn(serviceCall);
        when(timeOfUseItem.getServiceCall()).thenReturn(serviceCall);
        when(timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(any())).thenReturn(Optional.ofNullable(timeOfUseItem));
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(timeOfUseCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        timeOfUseCampaignHandler = new TimeOfUseCampaignHandler(timeOfUseCampaignService, clock, serviceCallService, thesaurus, threadPrincipalService, transactionService);
    }


    @Test
    public void testCalendarTaskStarted() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.PENDING);
        when(calendarComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_STARTED);
        when(event.getSource()).thenReturn(calendarComTaskExecution);
        when(serviceCall.getState()).thenReturn(DefaultState.PENDING);
        when(serviceCall.canTransitionTo(DefaultState.ONGOING)).thenReturn(true);
        timeOfUseCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
    }

    @Test
    public void testCalendarTaskCompleted() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(calendarComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(calendarComTaskExecution);
        when(parentSc.getExtension(any())).thenReturn(Optional.ofNullable(timeOfUseCampaign));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        timeOfUseCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testCalendarTaskFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.FAILED);
        when(calendarComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_FAILED);
        when(event.getSource()).thenReturn(calendarComTaskExecution);
        when(parentSc.getExtension(any())).thenReturn(Optional.ofNullable(timeOfUseCampaign));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        timeOfUseCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.FAILED);
    }

    @Test
    public void testVerificationTaskCompleted() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(timeOfUseItemDomainExtension.getStepOfUpdate()).thenReturn(1L);
        when(timeOfUseCampaignService.isWithVerification(timeOfUseCampaign2)).thenReturn(true);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(timeOfUseItem.getStepOfUpdate()).thenReturn(1L);
        when(parentSc.getExtension(any())).thenReturn(Optional.ofNullable(timeOfUseCampaign));
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.ofEpochSecond(5900));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        timeOfUseCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testVerificationTaskFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        when(timeOfUseCampaignService.isWithVerification(timeOfUseCampaign2)).thenReturn(true);
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_FAILED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(timeOfUseItem.getStepOfUpdate()).thenReturn(1L);
        when(parentSc.getExtension(any())).thenReturn(Optional.ofNullable(timeOfUseCampaign));
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.ofEpochSecond(5900));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        timeOfUseCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.FAILED);
    }

    @Test
    public void testCampaignEdited() {
        when(eventType.getTopic()).thenReturn(TOU_CAMPAIGN_EDITED);
        when(event.getSource()).thenReturn(timeOfUseCampaign);
        timeOfUseCampaignHandler.onEvent(event);
        verify(timeOfUseCampaignService, timeout(500)).editCampaignItems(timeOfUseCampaign);
    }

    private static ComTaskExecution createCalendarTaskMock() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mock(ComTask.class);
        MessagesTask messagesTask = mock(MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(messagesTask));
        when(messagesTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));
        when(deviceMessageCategory.getId()).thenReturn(0);
        return comTaskExecution;
    }

    private static ComTaskExecution createVerificationTaskMock() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mock(ComTask.class);
        StatusInformationTask statusInformationTask = mock(StatusInformationTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(statusInformationTask));
        when(deviceMessageCategory.getId()).thenReturn(0);
        return comTaskExecution;
    }

    private static TimeOfUseCampaignDomainExtension createMockCampaign(String activation) {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = mock(TimeOfUseCampaignDomainExtension.class);
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(timeOfUseCampaign.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(111));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(2L);
        when(calendar.getName()).thenReturn("TestCalendar");
        when(timeOfUseCampaign.getName()).thenReturn("TestCampaign");
        when(timeOfUseCampaign.getDeviceType()).thenReturn(deviceType);
        when(timeOfUseCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(timeOfUseCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(timeOfUseCampaign.getCalendar()).thenReturn(calendar);
        when(timeOfUseCampaign.getUpdateType()).thenReturn("fullCalendar");
        when(timeOfUseCampaign.getActivationOption()).thenReturn(activation);
        when(timeOfUseCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getValidationTimeout()).thenReturn(120L);
        when(timeOfUseCampaign.getId()).thenReturn(3L);
        when(timeOfUseCampaign.getVersion()).thenReturn(4L);
        return timeOfUseCampaign;
    }

    private Device createMockDevice(DeviceMessageStatus deviceMessageStatus) {
        Device device = mock(Device.class);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        Device.CalendarSupport calendarSupport = mock(Device.CalendarSupport.class);
        PassiveCalendar passiveCalendar = mock(PassiveCalendar.class);
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        ActiveEffectiveCalendar activeEffectiveCalendar = mock(ActiveEffectiveCalendar.class);
        Calendar calendar = mock(Calendar.class);
        when(deviceMessageCategory.getId()).thenReturn(0);
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessage.getStatus()).thenReturn(deviceMessageStatus);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.ofEpochSecond(3600));
        when(calendar.getId()).thenReturn(2L);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(passiveCalendar.getAllowedCalendar()).thenReturn(allowedCalendar);
        when(passiveCalendar.getDeviceMessage()).thenReturn(Optional.of(deviceMessage));
        when(calendarSupport.getPlannedPassive()).thenReturn(Optional.of(passiveCalendar));
        when(calendarSupport.getActive()).thenReturn(Optional.of(activeEffectiveCalendar));
        when(activeEffectiveCalendar.getAllowedCalendar()).thenReturn(allowedCalendar);
        when(device.getMessages()).thenReturn(Collections.singletonList(deviceMessage));
        when(device.calendars()).thenReturn(calendarSupport);
        when(timeOfUseItem.getDeviceMessage()).thenReturn(Optional.of(deviceMessage));
        when(timeOfUseItemDomainExtension.getDeviceMessage()).thenReturn(Optional.of(deviceMessage));
        return device;
    }
}
