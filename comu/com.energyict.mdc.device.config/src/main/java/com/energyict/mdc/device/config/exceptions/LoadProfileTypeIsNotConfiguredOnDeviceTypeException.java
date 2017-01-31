/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.masterdata.LoadProfileType;

public class LoadProfileTypeIsNotConfiguredOnDeviceTypeException extends LocalizedException{

    public LoadProfileTypeIsNotConfiguredOnDeviceTypeException(LoadProfileType loadProfileType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, loadProfileType.getName());
        set("loadProfileTypeName", loadProfileType.getName());
    }

}