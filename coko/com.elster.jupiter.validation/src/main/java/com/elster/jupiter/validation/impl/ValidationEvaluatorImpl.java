package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.Collection;

/**
* Created by tgr on 5/09/2014.
*/
class ValidationEvaluatorImpl implements ValidationEvaluator {

    @Override
    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        if (qualities.isEmpty()) {
            return ValidationResult.NOT_VALIDATED;
        }
        if(qualities.size() == 1 && qualities.iterator().next().getTypeCode().equals(ReadingQualityType.MDM_VALIDATED_OK_CODE)) {
            return ValidationResult.VALID;
        }
        return ValidationResult.SUSPECT;
    }


}
