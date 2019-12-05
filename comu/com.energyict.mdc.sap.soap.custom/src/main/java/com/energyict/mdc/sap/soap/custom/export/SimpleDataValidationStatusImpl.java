/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

public class SimpleDataValidationStatusImpl implements DataValidationStatus {
    private final Instant timeStamp;
    private final ValidationResult validationResult;

    public SimpleDataValidationStatusImpl(Instant timeStamp, ValidationResult validationResult) {
        this.timeStamp = timeStamp;
        this.validationResult = validationResult;
    }

    @Override
    public Instant getReadingTimestamp() {
        return timeStamp;
    }

    @Override
    public Collection<? extends ReadingQuality> getReadingQualities() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<? extends ReadingQuality> getBulkReadingQualities() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<ValidationRule> getOffendedValidationRule(ReadingQuality readingQuality) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<ValidationRule> getOffendedRules() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<ValidationRule> getBulkOffendedRules() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean completelyValidated() {
        return true;
    }

    @Override
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    @Override
    public ValidationResult getBulkValidationResult() {
        return validationResult;
    }
}
