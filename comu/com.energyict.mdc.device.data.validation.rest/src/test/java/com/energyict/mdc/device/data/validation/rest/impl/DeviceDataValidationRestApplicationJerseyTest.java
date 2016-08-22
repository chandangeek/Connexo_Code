package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.DeviceDataValidationService;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import javax.ws.rs.core.Application;

import org.junit.*;
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
        return application;
    }
}