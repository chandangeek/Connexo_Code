/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class MinimumLevelFirmwareCheckTest extends AbstractFirmwareCheckTest {

    @Mock
    private FirmwareVersion meterFWDependency, commFWDependency;

    public MinimumLevelFirmwareCheckTest() {
        super(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, MinimumLevelFirmwareCheck.class);
    }

    @Override
    public void setUp() {
        super.setUp();
        when(meterFWDependency.getRank()).thenReturn(3);
        when(meterFWDependency.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(meterFWDependency.getDeviceType()).thenReturn(deviceType);
        when(commFWDependency.getRank()).thenReturn(4);
        when(commFWDependency.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(commFWDependency.getDeviceType()).thenReturn(deviceType);
    }

    @Test
    public void testOldMeterFirmware() {
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));

        expectError("Meter firmware is below its minimal level.");
    }

    @Test
    public void testOldCommFirmware() {
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));

        expectError("Communication firmware is below its minimal level.");
    }

    @Test
    public void testNoMeterFirmware() {
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.METER)).thenReturn(Optional.empty());

        expectError("Meter firmware is below its minimal level.");
    }

    @Test
    public void testNoCommFirmware() {
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());

        expectError("Communication firmware is below its minimal level.");
    }

    @Test
    public void testAcceptableRanksOfCurrentFirmware() {
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(meterFWDependency.getRank()).thenReturn(1);
        when(commFWDependency.getRank()).thenReturn(2);

        expectSuccess();
    }

    @Test
    public void testNoDependencies() {
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
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(firmwareService.isFirmwareCheckActivated(deviceType, FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).thenReturn(false);

        expectSuccess();
    }
}
