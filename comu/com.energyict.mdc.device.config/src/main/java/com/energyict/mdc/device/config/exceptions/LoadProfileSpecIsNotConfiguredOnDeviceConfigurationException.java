/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.LoadProfileSpec;

public class LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException extends LocalizedException {

    public LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(LoadProfileSpec loadProfileSpec, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, loadProfileSpec);
        set("loadProfileSpec", loadProfileSpec);
    }

}