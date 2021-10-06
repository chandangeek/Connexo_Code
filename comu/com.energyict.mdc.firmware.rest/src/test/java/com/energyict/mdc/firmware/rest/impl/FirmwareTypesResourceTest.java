/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.firmware.FirmwareType;

import com.jayway.jsonpath.JsonModel;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FirmwareTypesResourceTest extends BaseFirmwareTest {

    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    @Before
    public void setup() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
    }

    @Test
    public void getOnlyMeterFirmwareTest() {
        when(firmwareService.getSupportedFirmwareTypes(deviceType)).thenReturn(EnumSet.of(FirmwareType.METER));
        String json = target("devicetypes/1/supportedfirmwaretypes").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwareTypes")).hasSize(1);
        assertThat(jsonModel.<String>get("$.firmwareTypes[0].id")).isEqualTo("meter");
    }

    @Test
    public void getMeterAndCommunicationFirmwareTest() {
        when(firmwareService.getSupportedFirmwareTypes(deviceType)).thenReturn(EnumSet.of(FirmwareType.METER, FirmwareType.COMMUNICATION));
        String json = target("devicetypes/1/supportedfirmwaretypes").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwareTypes")).hasSize(2);
        assertThat(jsonModel.<String>get("$.firmwareTypes[0].id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwareTypes[1].id")).isEqualTo("communication");
    }

}
