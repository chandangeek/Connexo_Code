/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

class PropertyCannotBeOverriddenException extends LocalizedException {

    PropertyCannotBeOverriddenException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }
}
