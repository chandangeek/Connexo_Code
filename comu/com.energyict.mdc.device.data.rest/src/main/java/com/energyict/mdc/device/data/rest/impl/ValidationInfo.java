package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;

import java.util.Collection;

/**
 * Created by tgr on 5/09/2014.
 */
public class ValidationInfo {

    public ValidationResult validationResult;
    public boolean dataValidated;

    public ValidationInfo(DataValidationStatus value) {
        dataValidated = value.completelyValidated();
        for (ReadingQualityRecord readingQualityRecord : value.getReadingQualities()) {
            Collection<ValidationRule> rules = value.getOffendedValidationRule(readingQualityRecord);

        }
        validationResult =
    }
}
