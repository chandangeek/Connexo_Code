/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.orm.QueryStream;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface DataExportOccurrenceFinder {
    DataExportOccurrenceFinder setStart(int start);

    DataExportOccurrenceFinder setLimit(int limit);

    DataExportOccurrenceFinder withStartDateIn(Range<Instant> interval);

    DataExportOccurrenceFinder withEndDateIn(Range<Instant> interval);

    DataExportOccurrenceFinder withExportPeriodContaining(Instant timeStamp);

    List<? extends DataExportOccurrence> find();

    QueryStream<DataExportOccurrence> stream();
}
