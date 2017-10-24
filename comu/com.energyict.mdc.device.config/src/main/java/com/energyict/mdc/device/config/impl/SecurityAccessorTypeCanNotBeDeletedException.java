/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class SecurityAccessorTypeCanNotBeDeletedException extends LocalizedException {
    public SecurityAccessorTypeCanNotBeDeletedException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.KEY_ACCESSOR_CAN_NOT_BE_DELETED);
    }
}
