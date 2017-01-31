/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface TimeSeriesEntry {

    TimeSeries getTimeSeries();

    Instant getTimeStamp();

    Instant getRecordDateTime();

    Instant getInstant(int offset);

    BigDecimal getBigDecimal(int offset);

    int size();

    long getLong(int offset);

    long getVersion();
    
    String getString(int offset);
    
    Object[] getValues();

    Optional<TimeSeriesEntry> getVersion(Instant at);
}
