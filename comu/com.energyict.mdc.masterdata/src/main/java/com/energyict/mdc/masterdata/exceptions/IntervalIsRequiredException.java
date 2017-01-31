/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.masterdata.LoadProfileType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a entity without a {@link com.elster.jupiter.util.time.Interval}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:17)
 */
public class IntervalIsRequiredException extends LocalizedException {

    private IntervalIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a IntervalIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link LoadProfileType} without an Interval
     *
     * @param thesaurus The Thesaurus
     * @return the newly create IntervalIsRequiredException
     */
    public static IntervalIsRequiredException forLoadProfileType(Thesaurus thesaurus) {
        return new IntervalIsRequiredException(thesaurus, MessageSeeds.FIELD_IS_REQUIRED);
    }

}