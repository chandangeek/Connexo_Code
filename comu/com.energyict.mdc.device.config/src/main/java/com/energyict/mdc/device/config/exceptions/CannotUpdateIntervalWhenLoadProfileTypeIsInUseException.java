package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.elster.jupiter.time.TimeDuration}
 * of a {@link LoadProfileType} that is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (14:14)
 */
public class CannotUpdateIntervalWhenLoadProfileTypeIsInUseException extends LocalizedException {

    public CannotUpdateIntervalWhenLoadProfileTypeIsInUseException(LoadProfileType loadProfileType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, loadProfileType.getName());
        this.set("loadProfileType", loadProfileType);
    }

}