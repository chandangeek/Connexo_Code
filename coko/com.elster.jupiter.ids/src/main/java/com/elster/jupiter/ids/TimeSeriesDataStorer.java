package com.elster.jupiter.ids;

import java.time.Instant;
import java.time.ZonedDateTime;

public interface TimeSeriesDataStorer {
    void add(TimeSeries timeSeries, Instant instant, Object... values);

    default void add(TimeSeries timeSeries, ZonedDateTime dateTime, Object... values) {
        add(timeSeries, dateTime.toInstant(), values);
    }

    boolean overrules();

    StorerStats execute();

    boolean processed(TimeSeries timeSeries, Instant instant);

    Object doNotUpdateMarker();
}
