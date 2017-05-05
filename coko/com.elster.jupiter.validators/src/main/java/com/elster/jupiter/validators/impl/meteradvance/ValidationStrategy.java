/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.time.Instant;

class ValidationStrategy {

    private SkipValidationOption skipValidationOption = SkipValidationOption.DO_NOT_SKIP;
    private Range<Instant> validInterval;
    private Range<Instant> suspectInterval;

    private ValidationStrategy() {
    }

    static ValidationStrategy skipValidation(SkipValidationOption skipValidationOption) {
        ValidationStrategy validationStrategy = new ValidationStrategy();
        validationStrategy.skipValidationOption = skipValidationOption;
        return validationStrategy;
    }

    static ValidationStrategy markSuspect(Range<Instant> suspectInterval) {
        return markValidAndSuspect(null, suspectInterval);
    }

    static ValidationStrategy markValid(Range<Instant> validInterval) {
        return markValidAndSuspect(validInterval, null);
    }

    static ValidationStrategy markValidAndSuspect(Range<Instant> validInterval, Range<Instant> suspectInterval) {
        ValidationStrategy validationStrategy = new ValidationStrategy();
        validationStrategy.suspectInterval = suspectInterval;
        validationStrategy.validInterval = validInterval;
        return validationStrategy;
    }

    ValidationResult validate(BaseReadingRecord intervalReadingRecord) {
        switch (this.skipValidationOption) {
            case MARK_ALL_VALID:
                return ValidationResult.VALID;
            case MARK_ALL_NOT_VALIDATED:
                return ValidationResult.NOT_VALIDATED;
            case DO_NOT_SKIP:
                Instant timeStamp = intervalReadingRecord.getTimeStamp();
                if (this.validInterval != null && this.validInterval.contains(timeStamp)) {
                    return ValidationResult.VALID;
                } else if (this.suspectInterval != null && this.suspectInterval.contains(timeStamp)) {
                    return ValidationResult.SUSPECT;
                } else {
                    return ValidationResult.NOT_VALIDATED;
                }
            default:
                throw new UnsupportedOperationException("Skip validation option is not supported: " + this.skipValidationOption.name());
        }
    }

    SkipValidationOption getSkipValidationOption() {
        return skipValidationOption;
    }
}
