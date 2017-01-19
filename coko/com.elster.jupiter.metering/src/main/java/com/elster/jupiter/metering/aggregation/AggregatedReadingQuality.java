package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.ReadingQualityRecord;

public interface AggregatedReadingQuality extends ReadingQualityRecord {

    boolean isIndeterministic();
}
