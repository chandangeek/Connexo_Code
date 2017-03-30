/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;


import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.DeviceFirmwareHistory;
import com.energyict.mdc.firmware.DeviceFirmwareVersionHistoryRecord;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceFirmwareHistoryImpl implements DeviceFirmwareHistory {

    FirmwareService firmwareService;
    Device device;

    public DeviceFirmwareHistoryImpl(FirmwareService firmwareService, Device device){
        this.firmwareService = firmwareService;
        this.device = device;
    }

    @Override
    public List<DeviceFirmwareVersionHistoryRecord> history() {
        Condition forDevice = Where.where(ActivatedFirmwareVersionImpl.Fields.DEVICE.fieldName()).isEqualTo(this.device);
        return ((FirmwareServiceImpl) firmwareService).findActivatedFirmwareVersion(forDevice).sorted("STARTTIME", false).find().stream().map(DeviceFirmwareVersionHistoryRecordImpl::new).collect(Collectors.toList());
    }

    @Override
    public List<DeviceFirmwareVersionHistoryRecord> history(FirmwareType firmwareType) {
        return history().stream().filter((x)-> x.getFirmwareVersion().getFirmwareType() == firmwareType).collect(Collectors.toList());
    }

}
