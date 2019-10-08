/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class SecurityAccessorTypeMultipleFoundException extends LocalizedException {
    public SecurityAccessorTypeMultipleFoundException(Thesaurus thesaurus, String securityAccessorTypeName) {
        super(thesaurus, MessageSeeds.SECURITY_ACCESSOR_TYPE_MULTIPLE_FOUND, securityAccessorTypeName);
    }
}
