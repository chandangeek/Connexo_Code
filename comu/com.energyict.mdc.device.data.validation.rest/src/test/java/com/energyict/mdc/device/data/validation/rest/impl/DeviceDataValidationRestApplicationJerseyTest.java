package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;

import javax.ws.rs.core.Application;

import org.junit.Before;
import org.mockito.Mock;

public class DeviceDataValidationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    DeviceDataValidationService deviceDataValidationService;
    @Mock
    MeteringGroupsService meteringGroupsService;

    @Before
    public void setup() {
    }

    @Override
    protected Application getApplication() {
        DeviceDataValidationApplication application = new DeviceDataValidationApplication();
        application.setDeviceDataValidationService(deviceDataValidationService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setNlsService(nlsService);
        return application;
    }
}