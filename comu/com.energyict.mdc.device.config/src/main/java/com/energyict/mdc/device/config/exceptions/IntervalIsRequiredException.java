package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.LoadProfileType}
 * without a {@link com.elster.jupiter.util.time.Interval}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class IntervalIsRequiredException extends LocalizedException {

    public IntervalIsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.INTERVAL_IS_REQUIRED);
    }

}