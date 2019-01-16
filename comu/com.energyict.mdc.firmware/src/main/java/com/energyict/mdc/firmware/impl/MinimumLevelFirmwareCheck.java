/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;

public class MinimumLevelFirmwareCheck implements FirmwareCheck {
    private final FirmwareService firmwareService;
    private final Thesaurus thesaurus;

    @Inject
    MinimumLevelFirmwareCheck(FirmwareService firmwareService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.MINIMUM_LEVEL_FIRMWARE).format();
    }

    @Override
    public void execute(FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        // TODO if check is enabled
        if (!deviceUtils.isReadOutAfterLastFirmwareUpgrade()) {
            throw new FirmwareCheckException(thesaurus, MessageSeeds.DEVICE_FIRMWARE_NOT_READOUT);
        }
        // TODO implement check
        throw new FirmwareCheckException(thesaurus, MessageSeeds.CURRENT_FIRMWARE_RANK_BELOW_MINIMUM_SUPPORTED, FirmwareType.CA_CONFIG_IMAGE.getTranslation(thesaurus)); // TODO insert appropriate fw type
    }
}
