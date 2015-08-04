package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models all security violations related to {@link SecurityProperty SecurityProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-28 (13:57)
 */
public class SecurityPropertyException extends LocalizedException {

    public SecurityPropertyException(Thesaurus thesaurus, SecurityPropertySet securityPropertySet) {
        super(thesaurus, MessageSeeds.USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES, securityPropertySet.getName());
        this.set("securityPropertySetId", securityPropertySet.getId());
        this.set("securityPropertySetName", securityPropertySet.getName());
    }

}