/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.time.Instant;

public interface JournaledChannelReadingRecord extends IntervalReadingRecord {
    @Override
    JournaledChannelReadingRecord filter(ReadingType readingType);

    Instant getJournalTime();

    String getUserName();

    Channel getChannel();

}

