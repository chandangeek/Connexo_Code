/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.Optional;

public class StatusOfTargetFirmwareCheck implements FirmwareCheck {
    private final Thesaurus thesaurus;
    private final FirmwareServiceImpl firmwareService;

    @Inject
    StatusOfTargetFirmwareCheck(Thesaurus thesaurus, FirmwareServiceImpl firmwareService) {
        this.thesaurus = thesaurus;
        this.firmwareService = firmwareService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.MATCHING_TARGET_FIRMWARE_STATUS).format();
    }

    @Override
    public void execute(Optional<FirmwareCampaignManagementOptions> options, FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        (options.isPresent()?options:firmwareService.findFirmwareManagementOptions(deviceUtils.getDevice().getDeviceType())).ifPresent(firmwareManagementOptions -> {
            if (firmwareManagementOptions.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)
                    && !firmwareManagementOptions.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK).contains(firmwareVersion.getFirmwareStatus())) {
                throw new FirmwareCheckException(thesaurus, MessageSeeds.TARGET_FIRMWARE_STATUS_NOT_ACCEPTED);
            }
        });
    }
}
