package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ProductSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link ProductSpec} without a {@link com.elster.jupiter.metering.ReadingType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class ReadingTypeIsRequiredException extends LocalizedException {

    public ReadingTypeIsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.READING_TYPE_IS_REQUIRED);
    }

}