/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DeviceConfigurationResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private static final long DEVICE_TYPE_ID = 1L;
    private static final long DEVICE_CONFIG_ID = 2L;

    private DeviceConfiguration deviceConfiguration;
    private DeviceType deviceType;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deviceType = mockDeviceType("deviceType", DEVICE_TYPE_ID);
        deviceConfiguration = mockDeviceConfiguration(DEVICE_CONFIG_ID, deviceType);

        Finder<DeviceConfiguration> finder = mockFinder(deviceType.getConfigurations());
        when(deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(any(DeviceType.class))).thenReturn(finder);
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID)).thenReturn(Optional.of(deviceType));
    }

    @Test
    public void whenGetDeviceConfigurations_thenDefaultAttributeIsPresent() throws IOException {
        Response response = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.deviceConfigurations[0].isDefault")).isFalse();
    }

    @Test
    public void whenSuccessfullyChangeDefaultStatus_thenReturnCode204() {
        DeviceConfigurationInfo info = new DeviceConfigurationInfo(deviceConfiguration);

        Response response = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIG_ID + "/default").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void whenDeviceTypeCannotBeLocked_thenReturnCode409() throws IOException {
        when(deviceConfigurationService.findAndLockDeviceType(DEVICE_TYPE_ID, OK_VERSION)).thenReturn(Optional.empty());
        DeviceConfigurationInfo info = new DeviceConfigurationInfo(deviceConfiguration);

        Response response = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIG_ID + "/default").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void whenDeviceConfigurationVersionConflict_thenReturnCode409() throws IOException {
        when(deviceConfigurationService.findAndLockDeviceType(DEVICE_TYPE_ID, OK_VERSION)).thenReturn(Optional.of(deviceType));
        DeviceConfigurationInfo info = new DeviceConfigurationInfo(deviceConfiguration);
        info.version++;

        Response response = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIG_ID + "/default").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}
