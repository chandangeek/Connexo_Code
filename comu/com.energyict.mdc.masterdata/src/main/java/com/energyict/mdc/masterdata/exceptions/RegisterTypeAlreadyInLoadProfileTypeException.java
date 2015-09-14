package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link com.energyict.mdc.masterdata.MeasurementType} to a {@link LoadProfileType}
 * but that RegisterType was already added to the LoadProfileType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class RegisterTypeAlreadyInLoadProfileTypeException extends LocalizedException {

    public RegisterTypeAlreadyInLoadProfileTypeException(Thesaurus thesaurus, LoadProfileType loadProfileType, MeasurementType measurementType) {
        super(thesaurus, MessageSeeds.DUPLICATE_REGISTER_TYPE_IN_LOAD_PROFILE_TYPE, measurementType.getReadingType().getAliasName(), loadProfileType.getName());
    }

}