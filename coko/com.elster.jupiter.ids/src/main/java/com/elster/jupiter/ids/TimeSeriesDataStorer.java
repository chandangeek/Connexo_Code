/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.ZonedDateTime;

@ProviderType
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
