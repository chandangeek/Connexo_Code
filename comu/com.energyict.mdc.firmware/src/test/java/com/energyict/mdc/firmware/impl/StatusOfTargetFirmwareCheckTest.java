/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareStatus;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;

import static org.mockito.Mockito.when;

public class StatusOfTargetFirmwareCheckTest extends AbstractFirmwareCheckTest {

    public StatusOfTargetFirmwareCheckTest() {
        super(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK, StatusOfTargetFirmwareCheck.class);
    }

    @Test
    public void testUnexpectedStatus() {
        when(firmwareManagementOptions.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.FINAL));
        when(uploadedFirmware.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);

        expectError("Target firmware isn't in the allowed status.");
    }

    @Test
    public void testWrongStatus() {
        when(uploadedFirmware.getFirmwareStatus()).thenReturn(FirmwareStatus.DEPRECATED);

        expectError("Target firmware isn't in the allowed status.");
    }

    @Test
    public void testCheckNotActivated() {
        when(uploadedFirmware.getFirmwareStatus()).thenReturn(FirmwareStatus.DEPRECATED);
        when(firmwareManagementOptions.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(false);

        expectSuccess();
    }

    @Test
    public void testFirmwareManagementNotActivated() {
        when(uploadedFirmware.getFirmwareStatus()).thenReturn(FirmwareStatus.DEPRECATED);
        when(firmwareService.findFirmwareManagementOptions(deviceType)).thenReturn(Optional.empty());

        expectSuccess();
    }

    @Test
    public void testAcceptableStatus1() {
        when(uploadedFirmware.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);

        expectSuccess();
    }

    @Test
    public void testAcceptableStatus2() {
        when(uploadedFirmware.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);

        expectSuccess();
    }
}
