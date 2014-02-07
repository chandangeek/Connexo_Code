package com.energyict.mdc.device.config.exceptions;

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

    private IntervalIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a IntervalIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LoadProfileType} without an Interval
     *
     * @param thesaurus The Thesaurus
     * @return the newly create IntervalIsRequiredException
     */
    public static IntervalIsRequiredException forLoadProfileType(Thesaurus thesaurus) {
        return new IntervalIsRequiredException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
    }

    /**
     * Creates a IntervalIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LoadProfileType} without an Interval
     *
     * @param thesaurus The Thesaurus
     * @return the newly create IntervalIsRequiredException
     */
    public static IntervalIsRequiredException forChannelSpecWithoutLoadProfileSpec(Thesaurus thesaurus) {
        return new IntervalIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_WITHOUT_LOAD_PROFILE_SPEC_INTERVAL_IS_REQUIRED);
    }

}