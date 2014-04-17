package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.energyict.mdc.common.TimeDuration}
 * of a {@link LoadProfileType} that is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (14:14)
 */
public class CannotUpdateIntervalWhenLoadProfileTypeIsInUseException extends LocalizedException {

    public CannotUpdateIntervalWhenLoadProfileTypeIsInUseException(Thesaurus thesaurus, LoadProfileType loadProfileType) {
        super(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED, loadProfileType.getName());
        this.set("loadProfileType", loadProfileType);
    }

}