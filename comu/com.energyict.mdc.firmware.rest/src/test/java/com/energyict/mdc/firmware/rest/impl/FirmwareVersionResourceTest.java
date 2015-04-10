package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FirmwareVersionResourceTest extends BaseFirmwareTest {
    @Mock
    private DeviceType deviceType;

    @Mock
    FirmwareVersion firmwareVersion;

    @Before
    public void setUpStubs() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(firmwareService.getFirmwareVersionById(1)).thenReturn(Optional.of(firmwareVersion));
        when(firmwareVersion.getId()).thenReturn(1L);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("firmwareVersion");
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
    }

    @Test
    public void testGetFirmwareById() {
        String json = target("devicetypes/1/firmwares/1").request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(1L);
        assertThat(jsonModel.<Number>get("$.firmwareVersion")).isEqualTo("firmwareVersion");
    }
}
