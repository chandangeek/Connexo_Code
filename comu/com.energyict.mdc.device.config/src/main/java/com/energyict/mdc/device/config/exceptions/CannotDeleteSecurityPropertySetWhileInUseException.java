package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.SecurityPropertySet;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete a {@link SecurityPropertySet}
 * that is still in use by {@link com.energyict.mdc.device.config.ComTaskEnablement}s
 * of the same configuration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-23 (16:18)
 */
public class CannotDeleteSecurityPropertySetWhileInUseException extends LocalizedException {

    public CannotDeleteSecurityPropertySetWhileInUseException(Thesaurus thesaurus, SecurityPropertySet securityPropertySet) {
        super(thesaurus, MessageSeeds.SECURITY_PROPERTY_SET_IN_USE, securityPropertySet.getName(), securityPropertySet.getDeviceConfiguration().getName());
        this.set("securityPropertySetName", securityPropertySet.getName());
        this.set("deviceConfiguration", securityPropertySet.getDeviceConfiguration());
    }

}