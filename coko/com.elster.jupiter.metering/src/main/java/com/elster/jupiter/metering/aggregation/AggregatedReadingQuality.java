/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.ReadingQualityRecord;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface AggregatedReadingQuality extends ReadingQualityRecord {
    boolean isIndeterministic();
}