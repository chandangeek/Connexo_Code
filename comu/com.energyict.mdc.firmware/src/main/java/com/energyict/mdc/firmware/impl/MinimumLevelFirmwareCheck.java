/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;

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
        if (firmwareService.isFirmwareCheckActivatedForStatus(device.getDeviceType(), FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, firmwareVersion.getFirmwareStatus())) {
            if (!deviceUtils.isReadOutAfterLastFirmwareUpgrade()) {
                throw new FirmwareCheckException(thesaurus, MessageSeeds.DEVICE_FIRMWARE_NOT_READOUT);
            }
            // TODO implement check
            // TODO: only supported firmware types
            throw new FirmwareCheckException(thesaurus, MessageSeeds.CURRENT_FIRMWARE_RANK_BELOW_MINIMUM_SUPPORTED, FirmwareType.CA_CONFIG_IMAGE.getTranslation(thesaurus)); // TODO insert appropriate fw type
        }
    }
}
