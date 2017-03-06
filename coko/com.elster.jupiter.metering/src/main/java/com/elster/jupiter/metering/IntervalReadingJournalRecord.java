/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.IntervalReading;

import java.time.Instant;

public interface IntervalReadingJournalRecord extends BaseReadingRecord, IntervalReading {

    @Override
    IntervalReadingJournalRecord filter(ReadingType readingType);

    Instant getJournalTime();

    String getUserName();

    Boolean getActive();

    long getVersion();

    IntervalReadingRecord getIntervalReadingRecord();

}
