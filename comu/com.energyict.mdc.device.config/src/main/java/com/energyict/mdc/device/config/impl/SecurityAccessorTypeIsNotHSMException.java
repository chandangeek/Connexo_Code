/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;

public class SecurityAccessorTypeIsNotHSMException extends LocalizedException {
    public SecurityAccessorTypeIsNotHSMException(SecurityAccessorType securityAccessorType, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.SECURITY_ACCESSOR_TYPE_IS_NOT_HSM, securityAccessorType.getName());
    }
}
