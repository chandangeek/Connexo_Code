/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.device.data.validation.DeviceDataQualityService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class DeviceDataValidationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    DeviceDataQualityService deviceDataQualityService;
    @Mock
    MeteringGroupsService meteringGroupsService;

    @Override
    protected Application getApplication() {
        DeviceDataValidationApplication application = new DeviceDataValidationApplication();
        application.setDeviceDataQualityService(deviceDataQualityService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setNlsService(nlsService);
        return application;
    }

}