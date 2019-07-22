/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class SecurityAccessorTypeWrapperInUseException extends LocalizedException {
    public SecurityAccessorTypeWrapperInUseException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.SECURITY_ACCESSOR_WRAPPER_IN_USE);
    }
}
