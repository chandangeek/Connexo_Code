/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareType;

import java.util.Optional;

import org.junit.Test;

import static org.mockito.Mockito.when;

public class NoDowngradeFirmwareCheckTest extends AbstractFirmwareCheckTest {

    public NoDowngradeFirmwareCheckTest() {
        super(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, NoDowngradeFirmwareCheck.class);
    }

    @Test
    public void testNoDowngrade() {
        expectSuccess();
    }

    @Test
    public void testMeterFWDowngrade() {
        when(activeMeterFirmware.getRank()).thenReturn(100);

        expectError("Target firmware version rank is lower than the current firmware rank.");
    }

    @Test
    public void testCommFWDowngrade() {
        when(uploadedFirmware.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(activeCommunicationFirmware.getRank()).thenReturn(100);

        expectError("Target firmware version rank is lower than the current firmware rank.");
    }

    @Test
    public void testFirstUploadOfMeterFW() {
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.METER)).thenReturn(Optional.empty());

        expectSuccess();
    }

    @Test
    public void testFirstUploadOfCommFW() {
        when(uploadedFirmware.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());

        expectSuccess();
    }

    @Test
    public void testFirmwareNotReadOut() {
        when(firmwareManagementDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(false);

        expectError("Firmware hasn't been read out after the last upload.");
    }

    @Test
    public void testCheckNotActivated() {
        when(firmwareManagementDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(false);
        when(activeMeterFirmware.getRank()).thenReturn(100);
        when(firmwareService.isFirmwareCheckActivated(deviceType, FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).thenReturn(false);

        expectSuccess();
    }
}
