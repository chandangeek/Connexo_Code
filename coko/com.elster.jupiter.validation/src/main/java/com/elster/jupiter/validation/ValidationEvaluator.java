package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;

/**
 * Created by tgr on 5/09/2014.
 */
public interface ValidationEvaluator {

    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities);
}
