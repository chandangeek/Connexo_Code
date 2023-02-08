/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.firmware.FirmwareCheckManagementOption;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareManagementOptionsInfo {
    public Long id;
    public boolean isAllowed = false;
    public List<ManagementOptionInfo> supportedOptions;
    public List<ManagementOptionInfo> allowedOptions;
    public EnumMap<FirmwareCheckManagementOption, CheckManagementOptionInfo> checkOptions;
    public long version;
    public Boolean validateFirmwareFileSignature;

    public FirmwareManagementOptionsInfo() {
        supportedOptions = new ArrayList<>();
        allowedOptions = new ArrayList<>();
        checkOptions = new EnumMap<>(FirmwareCheckManagementOption.class);
    }
}
