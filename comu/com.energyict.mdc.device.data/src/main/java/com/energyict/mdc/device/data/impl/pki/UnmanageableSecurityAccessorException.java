/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.data.impl.MessageSeeds;

public class UnmanageableSecurityAccessorException extends LocalizedException {
    public UnmanageableSecurityAccessorException(Thesaurus thesaurus, SecurityAccessorType securityAccessorType) {
        super(thesaurus, MessageSeeds.NOT_ALLOWED_TO_EDIT_CENTRALLY_MANAGED_SECURITY_ACCESSOR, securityAccessorType.getName());
    }
}
