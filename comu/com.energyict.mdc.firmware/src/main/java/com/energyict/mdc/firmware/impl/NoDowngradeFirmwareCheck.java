/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareCheckManagementOptions;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;

public class NoDowngradeFirmwareCheck implements FirmwareCheck {
    private final FirmwareServiceImpl firmwareService;
    private final Thesaurus thesaurus;

    @Inject
    NoDowngradeFirmwareCheck(FirmwareServiceImpl firmwareService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.NO_DOWNGRADE).format();
    }

    @Override
    public void execute(FirmwareCheckManagementOptions options, FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        Device device = deviceUtils.getDevice();
        if (options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)) {
            if (!deviceUtils.isReadOutAfterLastFirmwareUpgrade()) {
                throw new FirmwareCheckException(thesaurus, MessageSeeds.DEVICE_FIRMWARE_NOT_READOUT);
            }
            if (firmwareService.getActiveFirmwareVersion(device, firmwareVersion.getFirmwareType())
                    .map(ActivatedFirmwareVersion::getFirmwareVersion)
                    .filter(current -> current.compareTo(firmwareVersion) > 0)
                    .isPresent()) {
                throw new FirmwareCheckException(thesaurus, MessageSeeds.UPLOADED_FIRMWARE_RANK_BELOW_CURRENT);
            }
        }
    }
}
