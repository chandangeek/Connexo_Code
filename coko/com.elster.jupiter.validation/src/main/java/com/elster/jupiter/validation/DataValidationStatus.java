package com.elster.jupiter.validation;

import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;
import java.util.Date;

public interface DataValidationStatus {

    Date getReadingTimestamp();

    boolean completelyValidated();

    Collection<? extends ReadingQuality> getReadingQualities();

    Collection<ValidationRule> getOffendedValidationRule(ReadingQuality readingQuality);

    Collection<ValidationRule> getOffendedRules();
}
