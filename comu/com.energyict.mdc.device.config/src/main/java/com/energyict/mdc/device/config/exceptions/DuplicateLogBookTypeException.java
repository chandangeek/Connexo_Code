/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;

public class DuplicateLogBookTypeException extends LocalizedException{

    public DuplicateLogBookTypeException(DeviceConfiguration deviceConfiguration, LogBookType logBookType, LogBookSpec logBookSpec, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, deviceConfiguration, logBookType);
        set("deviceConfiguration", deviceConfiguration);
        set("logBookType", logBookType);
        set("logBookSpec", logBookSpec);
    }

}