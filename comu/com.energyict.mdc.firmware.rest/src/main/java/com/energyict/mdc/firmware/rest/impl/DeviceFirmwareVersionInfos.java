package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;

import java.util.ArrayList;
import java.util.List;

public class DeviceFirmwareVersionInfos {
    public Long total = 0L;
    public List<DeviceActiveFirmwareVersionInfo> firmwares = new ArrayList<>();

    public DeviceFirmwareVersionInfos() {

    }

    public void addVersion(ActivatedFirmwareVersion version, Thesaurus thesaurus) {
        firmwares.add(new DeviceActiveFirmwareVersionInfo(version, thesaurus));
        total++;
    }
}
