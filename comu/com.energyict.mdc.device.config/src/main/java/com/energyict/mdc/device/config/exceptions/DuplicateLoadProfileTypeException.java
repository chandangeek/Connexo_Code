/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.LoadProfileType;

public class DuplicateLoadProfileTypeException extends LocalizedException{

    public DuplicateLoadProfileTypeException(DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType, LoadProfileSpec loadProfileSpec, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, deviceConfiguration.getName(), loadProfileType.getName());
        set("deviceConfiguration", deviceConfiguration);
        set("loadProfileType", loadProfileType);
        set("loadProfileSpec", loadProfileSpec);
    }
}
