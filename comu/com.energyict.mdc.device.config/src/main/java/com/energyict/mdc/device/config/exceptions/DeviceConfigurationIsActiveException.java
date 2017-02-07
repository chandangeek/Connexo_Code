/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;

public class DeviceConfigurationIsActiveException extends LocalizedException {

    public DeviceConfigurationIsActiveException(DeviceConfiguration deviceConfiguration, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
        set("deviceConfiguration", deviceConfiguration);
    }
}
