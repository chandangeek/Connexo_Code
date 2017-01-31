/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class PluggableRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    CalendarService calendarService;
    @Mock
    FirmwareService firmwareService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;

    @Override
    protected Application getApplication() {
        MdcPluggableRestApplication application = new MdcPluggableRestApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setPropertySpecService(propertySpecService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setCalendarService(calendarService);
        application.setFirmwareService(firmwareService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        return application;
    }

}