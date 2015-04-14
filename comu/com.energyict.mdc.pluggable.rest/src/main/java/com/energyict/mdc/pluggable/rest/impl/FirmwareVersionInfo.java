package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.firmware.FirmwareVersion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareVersionInfo {
    public long id;
    public String name;

    public FirmwareVersionInfo() {
    }

    public FirmwareVersionInfo(FirmwareVersion firmwareVersion){
        this.id = firmwareVersion.getId();
        this.name = firmwareVersion.getFirmwareVersion();
    }
}
