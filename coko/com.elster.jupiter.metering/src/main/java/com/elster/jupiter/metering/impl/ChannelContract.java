/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChannelContract extends Channel {

    Object[] toArray(BaseReading reading, ReadingType readingType, ProcessStatus status);

    Object[] toArray(BaseReadingRecord readingRecord);

    void validateValues(BaseReading reading, Object[] values);

    TimeSeries getTimeSeries();

    DerivationRule getDerivationRule(IReadingType readingType);

    Optional<Range<Instant>> getTimePeriod(BaseReading reading, Object[] values);

    RecordSpecs getRecordSpecDefinition();

    @Override
    List<IReadingType> getReadingTypes();
}
