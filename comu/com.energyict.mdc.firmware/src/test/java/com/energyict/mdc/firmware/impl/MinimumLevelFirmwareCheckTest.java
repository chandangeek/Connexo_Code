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
    private FirmwareVersion meterFWDependency, commFWDependency, auxFWDependency;

    public MinimumLevelFirmwareCheckTest() {
        super(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, MinimumLevelFirmwareCheck.class);
    }

    @Override
    public void setUp() {
        super.setUp();
        when(meterFWDependency.getRank()).thenReturn(4);
        when(meterFWDependency.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(meterFWDependency.getDeviceType()).thenReturn(deviceType);
        when(commFWDependency.getRank()).thenReturn(5);
        when(commFWDependency.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(commFWDependency.getDeviceType()).thenReturn(deviceType);
        when(auxFWDependency.getRank()).thenReturn(6);
        when(auxFWDependency.getFirmwareType()).thenReturn(FirmwareType.AUXILIARY);
        when(auxFWDependency.getDeviceType()).thenReturn(deviceType);
    }

    @Test
    public void testOldMeterFirmware() {
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));

        expectError("Firmware of the following types are below the minimum level: device firmware.");
    }

    @Test
    public void testOldCommFirmware() {
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));

        expectError("Firmware of the following types are below the minimum level: communication firmware");
    }

    @Test
    public void testOldAuxFirmware() {
        when(uploadedFirmware.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(auxFWDependency));

        expectError("Firmware of the following types are below the minimum level: auxiliary firmware");
    }

    @Test
    public void testNoMeterFirmware() {
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.METER)).thenReturn(Optional.empty());

        expectError("Firmware of the following types are below the minimum level: device firmware.");
    }

    @Test
    public void testNoCommFirmware() {
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());

        expectError("Firmware of the following types are below the minimum level: communication firmware.");
    }

    @Test
    public void testNoAuxFirmware() {
        when(uploadedFirmware.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(auxFWDependency));
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.AUXILIARY)).thenReturn(Optional.empty());

        expectError("Firmware of the following types are below the minimum level: auxiliary firmware.");
    }

    @Test
    public void testAcceptableRanksOfCurrentFirmware() {
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(uploadedFirmware.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(auxFWDependency));
        when(meterFWDependency.getRank()).thenReturn(1);
        when(commFWDependency.getRank()).thenReturn(2);
        when(auxFWDependency.getRank()).thenReturn(3);

        expectSuccess();
    }

    @Test
    public void testNoDependencies() {
        expectSuccess();
    }

    @Test
    public void testFirmwareNotReadOut() {
        when(firmwareManagementDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(false);
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(uploadedFirmware.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(auxFWDependency));
        expectError("Firmware hasn't been read out after the last upload.");
    }

    @Test
    public void testCheckNotActivated() {
        when(firmwareManagementDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(false);
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(uploadedFirmware.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(auxFWDependency));
        when(firmwareCampaignManagementOptions.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).thenReturn(false);

        expectSuccess();
    }
}
