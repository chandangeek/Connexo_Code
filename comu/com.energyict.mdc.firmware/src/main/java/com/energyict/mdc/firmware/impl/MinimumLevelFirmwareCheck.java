/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareCheckManagementOptions;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.stream.Collectors;
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
    public void execute(FirmwareCheckManagementOptions options, FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        Device device = deviceUtils.getDevice();
        if (options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)) {
            if (firmwareVersion.getMeterFirmwareDependency().isPresent()
                    || firmwareVersion.getCommunicationFirmwareDependency().isPresent()
                    || firmwareVersion.getAuxiliaryFirmwareDependency().isPresent()) {
                if (!deviceUtils.isReadOutAfterLastFirmwareUpgrade()) {
                    throw new FirmwareCheckException(thesaurus, MessageSeeds.DEVICE_FIRMWARE_NOT_READOUT);
                }
                String conflictingTypes = Stream.of(firmwareVersion.getMeterFirmwareDependency(), firmwareVersion.getCommunicationFirmwareDependency(), firmwareVersion.getAuxiliaryFirmwareDependency())
                        .flatMap(Functions.asStream())
                        .flatMap(dependency -> {
                            FirmwareType firmwareType = dependency.getFirmwareType();
                            if (!firmwareService.getActiveFirmwareVersion(device, firmwareType)
                                    .map(ActivatedFirmwareVersion::getFirmwareVersion)
                                    .filter(current -> current.compareTo(dependency) >= 0)
                                    .isPresent()) {
                                return Stream.of(firmwareType.getTranslation(thesaurus));
                            } else {
                                return Stream.empty();
                            }
                        })
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.joining(", "))
                        .toLowerCase();
                if (!conflictingTypes.isEmpty()) {
                    throw new FirmwareCheckException(thesaurus, MessageSeeds.FIRMWARES_BELOW_MINIMUM_LEVEL, conflictingTypes);
                }
            }
        }
    }
}
