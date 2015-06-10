package com.elster.jupiter.validation;

import com.elster.jupiter.metering.readings.ReadingQuality;

import java.time.Instant;
import java.util.Collection;

public interface DataValidationStatus {

    Instant getReadingTimestamp();

    boolean completelyValidated();

    Collection<? extends ReadingQuality> getReadingQualities();

    Collection<? extends ReadingQuality> getBulkReadingQualities();

    Collection<ValidationRule> getOffendedValidationRule(ReadingQuality readingQuality);

    Collection<ValidationRule> getOffendedRules();

    Collection<ValidationRule> getBulkOffendedRules();

	ValidationResult getValidationResult();

    ValidationResult getBulkValidationResult();
}
