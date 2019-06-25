/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeOfUseCampaignInfoFactoryTest {

    private static TimeOfUseCampaignService timeOfUseCampaignService = mock(TimeOfUseCampaignService.class);
    private static Clock clock = mock(Clock.class);
    private static DeviceConfigurationService deviceConfigurationService = mock(DeviceConfigurationService.class);
    private static CalendarService calendarService = mock(CalendarService.class);
    private static TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory;
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static TaskService taskService  = mock(TaskService.class);
    private static ExceptionFactory exceptionFactory = mock(ExceptionFactory.class);

    @BeforeClass
    public static void setUp() {
        timeOfUseCampaignInfoFactory = new TimeOfUseCampaignInfoFactory(timeOfUseCampaignService, clock, thesaurus,
                deviceConfigurationService, calendarService, exceptionFactory,taskService);
    }

    @Test
    public void fromTest() {
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = timeOfUseCampaignInfoFactory.from(timeOfUseCampaign);
        assertEquals(timeOfUseCampaignInfo.id, 3L);
        assertEquals(timeOfUseCampaignInfo.version, 4L);
        assertEquals(timeOfUseCampaignInfo.validationTimeout, 120L);
        assertEquals(timeOfUseCampaignInfo.name, "TestCampaign");
        assertEquals(timeOfUseCampaignInfo.activationOption, "immediately");
        assertEquals(timeOfUseCampaignInfo.deviceGroup, "TestGroup");
        assertEquals(timeOfUseCampaignInfo.updateType, "fullCalendar");
        assertEquals(timeOfUseCampaignInfo.activationStart, Instant.ofEpochSecond(100));
        assertEquals(timeOfUseCampaignInfo.activationEnd, Instant.ofEpochSecond(200));
        assertEquals(timeOfUseCampaignInfo.activationDate, Instant.ofEpochSecond(100));
        assertEquals(timeOfUseCampaignInfo.calendar.name, "TestCalendar");
        assertEquals(timeOfUseCampaignInfo.calendar.id, 2L);
        assertEquals(timeOfUseCampaignInfo.deviceType.name, "TestDeviceType");
        assertEquals(timeOfUseCampaignInfo.deviceType.id, 1L);

        assertEquals(timeOfUseCampaignInfo.sendCalendarComTask.name, "ctask");
        assertEquals(timeOfUseCampaignInfo.sendCalendarComTask.id, 1L);
        assertEquals(timeOfUseCampaignInfo.sendCalendarConnectionStrategy.name, "As soon as possible");
        assertEquals(timeOfUseCampaignInfo.sendCalendarConnectionStrategy.id, 2L);
        assertEquals(timeOfUseCampaignInfo.validationComTask.name, "ctask");
        assertEquals(timeOfUseCampaignInfo.validationComTask.id, 1L);
        assertEquals(timeOfUseCampaignInfo.validationConnectionStrategy.name, "Minimize connections");
        assertEquals(timeOfUseCampaignInfo.validationConnectionStrategy.id, 1L);
    }

    @Test
    public void getOverviewTest() {
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = timeOfUseCampaignInfoFactory.getOverviewCampaignInfo(timeOfUseCampaign);
        assertEquals(timeOfUseCampaignInfo.id, 3L);
        assertEquals(timeOfUseCampaignInfo.version, 4L);
        assertEquals(timeOfUseCampaignInfo.validationTimeout, 120L);
        assertEquals(timeOfUseCampaignInfo.name, "TestCampaign");
        assertEquals(timeOfUseCampaignInfo.activationOption, "immediately");
        assertEquals(timeOfUseCampaignInfo.deviceGroup, "TestGroup");
        assertEquals(timeOfUseCampaignInfo.updateType, "fullCalendar");
        assertEquals(timeOfUseCampaignInfo.activationStart, Instant.ofEpochSecond(100));
        assertEquals(timeOfUseCampaignInfo.activationEnd, Instant.ofEpochSecond(200));
        assertEquals(timeOfUseCampaignInfo.activationDate, Instant.ofEpochSecond(100));
        assertEquals(timeOfUseCampaignInfo.calendar.name, "TestCalendar");
        assertEquals(timeOfUseCampaignInfo.calendar.id, 2L);
        assertEquals(timeOfUseCampaignInfo.deviceType.name, "TestDeviceType");
        assertEquals(timeOfUseCampaignInfo.deviceType.id, 1L);
        assertEquals(timeOfUseCampaignInfo.startedOn, Instant.ofEpochSecond(111));
        assertNull(timeOfUseCampaignInfo.finishedOn);
        assertEquals(timeOfUseCampaignInfo.status, "Ongoing");
    }


    private TimeOfUseCampaign createMockCampaign() {
        TimeOfUseCampaign timeOfUseCampaign = mock(TimeOfUseCampaign.class);
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
        when(timeOfUseCampaign.getId()).thenReturn(3L);
        when(timeOfUseCampaign.getVersion()).thenReturn(4L);
        when(timeOfUseCampaign.getName()).thenReturn("TestCampaign");
        when(timeOfUseCampaign.getDeviceType()).thenReturn(deviceType);
        when(timeOfUseCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(timeOfUseCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(timeOfUseCampaign.getCalendar()).thenReturn(calendar);
        when(timeOfUseCampaign.getUpdateType()).thenReturn("fullCalendar");
        when(timeOfUseCampaign.getActivationOption()).thenReturn("immediately");
        when(timeOfUseCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getValidationTimeout()).thenReturn(120L);
        when(timeOfUseCampaign.getSendCalendarComTaskId()).thenReturn(1L);
        when(timeOfUseCampaign.getValidationComTaskId()).thenReturn(1L);
        when(timeOfUseCampaign.getSendCalendarConnectionStrategyId()).thenReturn(2L);
        when(timeOfUseCampaign.getValidationConnectionStrategyId()).thenReturn(1L);
        ComTask comtask = mock(ComTask.class);
        when(taskService.findComTask(anyLong())).thenReturn(Optional.of(comtask));
        when(taskService.findComTask(anyLong()).get().getName()).thenReturn("ctask");
        return timeOfUseCampaign;
    }
}
