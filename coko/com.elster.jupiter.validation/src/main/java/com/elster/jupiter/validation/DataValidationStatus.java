package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 29/08/2014
 * Time: 17:08
 */
public interface DataValidationStatus {
    Date getReadingTimestamp();

    boolean completelyValidated();

    Collection<? extends ReadingQuality> getReadingQualities();

    Collection<ValidationRule> getOffendedValidationRule(ReadingQuality readingQuality);

    Collection<ValidationRule> getOffendedRules();
}
