/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.EnumSet;

public class NoGhostFirmwareCheck implements FirmwareCheck {
    private final FirmwareServiceImpl firmwareService;
    private final Thesaurus thesaurus;

    @Inject
    NoGhostFirmwareCheck(FirmwareServiceImpl firmwareService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.NO_GHOST_FIRMWARE).format();
    }

    @Override
    public void execute(FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        Device device = deviceUtils.getDevice();
        DeviceType deviceType = device.getDeviceType();
        EnumSet.of(FirmwareType.METER, FirmwareType.COMMUNICATION).stream()
                .filter(firmwareType -> firmwareService.isFirmwareTypeSupported(deviceType, firmwareType))
                .forEach(firmwareType -> {
                    if (firmwareService.getActiveFirmwareVersion(device, firmwareType)
                            .map(ActivatedFirmwareVersion::getFirmwareVersion)
                            .map(FirmwareVersion::getFirmwareStatus)
                            .filter(FirmwareStatus.GHOST::equals)
                            .isPresent()) {
                        throw new FirmwareCheckException(thesaurus, MessageSeeds.CURRENT_FIRMWARE_IS_GHOST);
                    }
                });
    }
}
