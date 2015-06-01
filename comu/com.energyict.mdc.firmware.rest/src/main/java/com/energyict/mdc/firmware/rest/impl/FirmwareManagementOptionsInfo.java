package com.energyict.mdc.firmware.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class FirmwareManagementOptionsInfo {
    public Long id;
    public boolean isAllowed = false;
    public List<ManagementOptionInfo> supportedOptions;
    public List<ManagementOptionInfo> allowedOptions;

    public FirmwareManagementOptionsInfo() {
        supportedOptions = new ArrayList<>();
        allowedOptions = new ArrayList<>();
    }
}
