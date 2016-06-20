package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.ReadingQualityRecord;

/**
 * Created by igh on 20/06/2016.
 */
public interface AggregatedReadingQuality extends ReadingQualityRecord {

    boolean isIndeterministic();
}
