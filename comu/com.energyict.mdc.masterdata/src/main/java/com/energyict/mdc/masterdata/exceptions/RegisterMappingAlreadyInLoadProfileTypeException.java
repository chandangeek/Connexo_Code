package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link RegisterMapping} to a {@link LoadProfileType}
 * but that RegisterMapping was already added to the LoadProfileType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class RegisterMappingAlreadyInLoadProfileTypeException extends LocalizedException {

    public RegisterMappingAlreadyInLoadProfileTypeException(Thesaurus thesaurus, LoadProfileType loadProfileType, RegisterMapping registerMapping) {
        super(thesaurus, MessageSeeds.DUPLICATE_REGISTER_MAPPING_IN_LOAD_PROFILE_TYPE);
        this.set("loadProfileType", loadProfileType);
        this.set("registerMapping", registerMapping);
    }

}