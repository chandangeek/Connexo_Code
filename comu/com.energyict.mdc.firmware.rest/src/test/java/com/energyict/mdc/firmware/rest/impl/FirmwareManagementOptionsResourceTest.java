/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareManagementOptionsResourceTest extends BaseFirmwareTest {
    @Mock
    private DeviceType deviceType;

    @Mock
    private FirmwareManagementOptions options;

    private ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER;
    private Set<ProtocolSupportedFirmwareOptions> set = new HashSet<>();
    private String URI = "devicetypes/1/firmwaremanagementoptions/1";

    @Before
    public void setUpStubs() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));


        set.addAll(Arrays.asList(protocolSupportedFirmwareOptions));
        when(firmwareService.getSupportedFirmwareOptionsFor(deviceType)).thenReturn(set);
        when(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).thenReturn(set);

        when(firmwareService.findFirmwareManagementOptions(deviceType)).thenReturn(Optional.of(options));
        when(firmwareService.findAndLockFirmwareManagementOptionsByIdAndVersion(eq(deviceType), anyLong())).thenReturn(Optional.of(options));

    }

    @Test
    public void testGetFirmwareManagementOptions() {
        when(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).thenReturn(new HashSet<>());
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(0);
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(false);
    }

    @Test
    public void testGetFirmwareManagementOptionsAllowed() {
        when(options.getOptions()).thenReturn(set);
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(true);
    }

    @Test
    public void testPutFirmwareManagementOptions() {
        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        firmwareManagementOptionsInfo.isAllowed = false;

        target(URI).request().put(Entity.json(firmwareManagementOptionsInfo));

        verify(options).delete();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UPGRADE_OPTIONS_REQUIRED + "}", property = "key", strict = false)
    public void testPutFirmwareManagementOptionsException() {
        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        firmwareManagementOptionsInfo.isAllowed = true;

        target(URI).request().put(Entity.json(firmwareManagementOptionsInfo));
    }
}
