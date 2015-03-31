package com.energyict.mdc.firmware.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class FirmwareUpgradeOptionsInfo {
    public List<UpgradeOptionInfo> supportedOptions;
    public List<UpgradeOptionInfo> allowedOptions;

    public FirmwareUpgradeOptionsInfo () {
        supportedOptions = new ArrayList<>();
        allowedOptions = new ArrayList<>();
    }
}
