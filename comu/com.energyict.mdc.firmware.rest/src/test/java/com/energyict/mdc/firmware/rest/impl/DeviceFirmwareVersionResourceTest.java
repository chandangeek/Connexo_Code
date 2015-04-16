package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceFirmwareVersionResourceTest extends BaseFirmwareTest {
    @Mock
    private Device device;

    @Mock
    private ActivatedFirmwareVersion activatedFirmwareVersion;

    @Mock
    private FirmwareVersion firmwareVersion;

    @Before
    public void setUpStubs() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));

        when(firmwareService.getCurrentMeterFirmwareVersionFor(device)).thenReturn(Optional.of(activatedFirmwareVersion));
        when(firmwareService.getCurrentCommunicationFirmwareVersionFor(device)).thenReturn(Optional.of(activatedFirmwareVersion));

        when(activatedFirmwareVersion.getFirmwareVersion()).thenReturn(firmwareVersion);

        when(firmwareVersion.getId()).thenReturn(1L);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("firmwareVersion");
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
    }

    @Test
    public void testGetFirmwareVersionsOnDevice() {
        String json = target("device/1/firmwares").request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwares[0].activeVersion.firmwareVersion")).isEqualTo("firmwareVersion");
    }

}
