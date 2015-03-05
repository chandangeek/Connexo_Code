package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

public interface ReadingTypeInEstimationRule {

    EstimationRule getRule();

    ReadingType getReadingType();
}
