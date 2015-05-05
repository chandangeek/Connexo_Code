package com.elster.jupiter.ids;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.ZonedDateTime;

@ProviderType
public interface TimeSeriesDataWriter {

    /**
     * nulls will not overrule
     *
     * @param timeSeries
     * @param instant
     * @param values
     */
    void add(TimeSeries timeSeries, Instant instant, Object... values);

    default void add(TimeSeries timeSeries, ZonedDateTime dateTime, Object... values) {
        add(timeSeries, dateTime.toInstant(), values);
    }

    boolean overrules();

    StorerStats execute();

    boolean processed(TimeSeries timeSeries, Instant instant);

    Object doNotUpdateMarker();
}
