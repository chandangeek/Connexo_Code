package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ProductSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link ProductSpec} but another one with the same
 * {@link com.elster.jupiter.metering.ReadingType} already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class DuplicateReadingTypeException extends LocalizedException {

    public DuplicateReadingTypeException(Thesaurus thesaurus, ReadingType readingType) {
        super(thesaurus, MessageSeeds.READING_TYPE_ALREADY_EXISTS, readingType.getMRID());
        this.set("readingTypeId", readingType.getMRID());
    }

}