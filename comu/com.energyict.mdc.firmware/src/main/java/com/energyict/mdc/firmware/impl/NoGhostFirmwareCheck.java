/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class NoGhostFirmwareCheck implements FirmwareCheck {
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    @Inject
    NoGhostFirmwareCheck(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.NO_GHOST_FIRMWARE).format();
    }

    @Override
    public void execute(FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        if (hasGhostMeterOrCommunicationFirmware(deviceUtils.getDevice())) {
            throw new FirmwareCheckException(thesaurus, MessageSeeds.DEVICE_HAS_GHOST_FIRMWARE);
        }
    }

    boolean hasGhostMeterOrCommunicationFirmware(Device device) {
        List<FirmwareType> checkedTypes = Arrays.asList(FirmwareType.METER, FirmwareType.COMMUNICATION);
        return dataModel.stream(ActivatedFirmwareVersion.class)
                .join(FirmwareVersion.class)
                .filter(Where.where(ActivatedFirmwareVersionImpl.Fields.DEVICE.fieldName()).isEqualTo(device))
                .filter(Where.where(ActivatedFirmwareVersionImpl.Fields.INTERVAL.fieldName()).isEffective())
                .filter(Where.where(ActivatedFirmwareVersionImpl.Fields.FIRMWARE_VERSION.fieldName() + '.' + FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).in(checkedTypes))
                .anyMatch(Where.where(ActivatedFirmwareVersionImpl.Fields.FIRMWARE_VERSION.fieldName() + '.' + FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).isEqualTo(FirmwareStatus.GHOST));
    }
}
