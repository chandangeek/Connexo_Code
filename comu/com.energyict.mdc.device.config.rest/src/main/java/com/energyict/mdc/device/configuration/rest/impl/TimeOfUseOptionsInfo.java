/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class TimeOfUseOptionsInfo {
    public Long id;
    public boolean isAllowed = false;
    public List<OptionInfo> supportedOptions;
    public List<OptionInfo> allowedOptions;
    public long version;

    public TimeOfUseOptionsInfo() {
        supportedOptions = new ArrayList<>();
        allowedOptions = new ArrayList<>();
    }
}
