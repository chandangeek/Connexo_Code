/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.stream.Stream;

public class MinimumLevelFirmwareCheck implements FirmwareCheck {
    private final FirmwareServiceImpl firmwareService;
    private final Thesaurus thesaurus;

    @Inject
    MinimumLevelFirmwareCheck(FirmwareServiceImpl firmwareService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.MINIMUM_LEVEL_FIRMWARE).format();
    }

    @Override
    public void execute(FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        Device device = deviceUtils.getDevice();
        if (firmwareService.isFirmwareCheckActivated(device.getDeviceType(), FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)) {
            if (!deviceUtils.isReadOutAfterLastFirmwareUpgrade()) {
                throw new FirmwareCheckException(thesaurus, MessageSeeds.DEVICE_FIRMWARE_NOT_READOUT);
            }
            Stream.of(firmwareVersion.getMeterFirmwareDependency(), firmwareVersion.getCommunicationFirmwareDependency())
                    .flatMap(Functions.asStream())
                    .forEach(dependency -> {
                        FirmwareType firmwareType = dependency.getFirmwareType();
                        if (!firmwareService.getActiveFirmwareVersion(device, firmwareType)
                                .map(ActivatedFirmwareVersion::getFirmwareVersion)
                                .filter(current -> current.compareTo(dependency) >= 0)
                                .isPresent()) {
                            throw new FirmwareCheckException(thesaurus, messageSeedForType(firmwareType));
                        }
                    });
        }
    }

    private MessageSeeds messageSeedForType(FirmwareType firmwareType) {
        switch (firmwareType) {
            case METER:
                return MessageSeeds.METER_FIRMWARE_RANK_BELOW_MINIMUM_SUPPORTED;
            case COMMUNICATION:
                return MessageSeeds.COMMUNICATION_FIRMWARE_RANK_BELOW_MINIMUM_SUPPORTED;
            default:
                throw new IllegalArgumentException("Firmware type " + firmwareType.name() + " isn't supported by " + MinimumLevelFirmwareCheck.class.getSimpleName());
        }
    }
}
