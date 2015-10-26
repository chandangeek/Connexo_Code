package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link SecurityPropertySet}
 * is being deleted while it is still being used by one or more
 * {@link com.energyict.mdc.dynamic.relation.Relation}s
 * on one or more {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-10 (15:20)
 */
public class VetoDeleteSecurityPropertySetException extends LocalizedException {

    protected VetoDeleteSecurityPropertySetException(Thesaurus thesaurus, SecurityPropertySet securityPropertySet) {
        super(thesaurus, MessageSeeds.VETO_SECURITY_PROPERTY_SET_DELETION, securityPropertySet.getName(), securityPropertySet.getDeviceConfiguration().getName());
        this.set("securityPropertySetId", securityPropertySet.getId());
        this.set("deviceConfigurationId", securityPropertySet.getDeviceConfiguration().getId());
    }

}