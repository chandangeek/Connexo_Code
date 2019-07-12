/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.time.ZoneId;

import org.mockito.Mock;

import static org.mockito.Mockito.when;

public abstract class BaseTouTest extends FelixRestApplicationJerseyTest {
    @Mock
    TimeOfUseCampaignService timeOfUseCampaignService;
    @Mock
    DeviceService deviceService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    Clock clock;
    @Mock
    CalendarService calendarService;

    @Override
    protected Application getApplication() {
        TimeOfUseApplication application = new TimeOfUseApplication();
        application.setClock(clock);
        application.setTimeOfUseCampaignService(timeOfUseCampaignService);
        application.setDeviceService(deviceService);
        application.setServiceCallService(serviceCallService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setNlsService(nlsService);
        application.setCalendarService(calendarService);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        return application;
    }
}
