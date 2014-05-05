package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update a {@link com.energyict.mdc.masterdata.RegisterGroup},
 * but there is no {@link com.energyict.mdc.masterdata.RegisterMapping} selected.
 *
 */
public class RegisterTypesRequiredException extends LocalizedFieldValidationException {

    public RegisterTypesRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.REGISTER_GROUP_REQUIRES_REGISTER_TYPES, "items");
    }
}
