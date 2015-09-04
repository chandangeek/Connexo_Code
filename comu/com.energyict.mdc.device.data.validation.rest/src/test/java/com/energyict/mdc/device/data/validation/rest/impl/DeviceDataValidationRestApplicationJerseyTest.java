package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.DeviceDataValidationService;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;

import javax.ws.rs.core.Application;

import org.junit.*;
import org.mockito.Mock;

public class DeviceDataValidationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    DeviceDataValidationService deviceDataValidationService;

    @Before
    public void setup() {
    }

    @Override
    protected Application getApplication() {
        DeviceDataValidationApplication application = new DeviceDataValidationApplication();
        application.setDeviceDataValidationService(deviceDataValidationService);
        return application;
    }
}