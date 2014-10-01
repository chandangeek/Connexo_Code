package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;

public enum ValidationResult {

    VALID, SUSPECT, NOT_VALIDATED;

    public static ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        if (qualities.isEmpty()) {
            return ValidationResult.NOT_VALIDATED;
        }
        if(qualities.size() == 1 && qualities.iterator().next().getTypeCode().equals(ReadingQualityType.MDM_VALIDATED_OK_CODE)) {
            return ValidationResult.VALID;
        }
        return ValidationResult.SUSPECT;
    }

}
