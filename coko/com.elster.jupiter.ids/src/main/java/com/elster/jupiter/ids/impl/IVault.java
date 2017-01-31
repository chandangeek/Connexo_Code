/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IVault extends Vault {

    List<TimeSeriesEntry> getEntries(TimeSeriesImpl timeSeries, Range<Instant> interval);

    SqlFragment getRawValuesSql(TimeSeriesImpl timeSeries, Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames);

    List<TimeSeriesEntry> getEntriesUpdatedSince(TimeSeriesImpl timeSeries, Range<Instant> interval, Instant since);

    Optional<TimeSeriesEntry> getEntry(TimeSeriesImpl timeSeries, Instant when);

    List<TimeSeriesEntry> getEntriesBefore(TimeSeriesImpl timeSeries, Instant when, int entryCount, boolean includeBoundary);

    void removeEntries(TimeSeriesImpl timeSeries, Range<Instant> range);

}