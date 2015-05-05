package com.energyict.mdc.firmware.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class FirmwareManagementOptionsInfo {
    public Long id;
    public Boolean isAllowed = false;
    public List<UpgradeOptionInfo> supportedOptions;
    public List<UpgradeOptionInfo> allowedOptions;

    public FirmwareManagementOptionsInfo() {
        supportedOptions = new ArrayList<>();
        allowedOptions = new ArrayList<>();
    }
}
