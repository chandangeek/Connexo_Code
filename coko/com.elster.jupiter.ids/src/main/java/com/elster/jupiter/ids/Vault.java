/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.TimeZone;
import java.util.logging.Logger;

public interface Vault {
    String getComponentName();

    long getId();

    String getDescription();

    void setDescription(String description);

    Instant getMinDate();

    Instant getMaxDate();

    boolean isRegular();

    boolean hasJournal();

    int getSlotCount();

    int getTextSlotCount();

    boolean hasLocalTime();

    boolean isPartitioned();

    boolean isActive();

    void activate(Instant to);

    Instant extendTo(Instant to, Logger logger);

    default TimeSeries createRegularTimeSeries(RecordSpec spec, TimeZone timeZone, TemporalAmount interval, long offset) {
        return createRegularTimeSeries(spec, timeZone.toZoneId(), interval, offset);
    }

    default TimeSeries createIrregularTimeSeries(RecordSpec spec, TimeZone timeZone) {
        return createIrregularTimeSeries(spec, timeZone.toZoneId());
    }

    TimeSeries createRegularTimeSeries(RecordSpec spec, ZoneId zoneId, TemporalAmount interval, long offset);

    TimeSeries createIrregularTimeSeries(RecordSpec spec, ZoneId zoneId);

    boolean isValidInstant(Instant instant);

    void purge(Logger logger);

    Period getRetention();

    void setRetentionDays(int numberOfDays);
}
