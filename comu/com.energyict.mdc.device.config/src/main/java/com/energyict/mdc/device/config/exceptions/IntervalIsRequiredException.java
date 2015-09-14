package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a entity without a {@link com.elster.jupiter.util.time.Interval}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class IntervalIsRequiredException extends LocalizedException {

    /**
     * Creates a IntervalIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link LoadProfileType} without an Interval
     *
     * @param thesaurus The Thesaurus
     * @param messageSeed The MessageSeed
     * @return the newly create IntervalIsRequiredException
     */
    public static IntervalIsRequiredException forChannelSpecWithoutLoadProfileSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new IntervalIsRequiredException(thesaurus, messageSeed);
    }

    private IntervalIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}