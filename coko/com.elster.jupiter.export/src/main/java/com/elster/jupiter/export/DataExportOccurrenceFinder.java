/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Order;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface DataExportOccurrenceFinder {
    DataExportOccurrenceFinder setId(long id);

    DataExportOccurrenceFinder setStart(int start);

    DataExportOccurrenceFinder setLimit(int limit);

    DataExportOccurrenceFinder setOrder(List<Order> sortingColumns);

    DataExportOccurrenceFinder withStartDateIn(Range<Instant> interval);

    DataExportOccurrenceFinder withEndDateIn(Range<Instant> interval);

    DataExportOccurrenceFinder withExportPeriodContaining(Instant timeStamp);

    DataExportOccurrenceFinder withExportStatus(List<DataExportStatus> statuses);

    DataExportOccurrenceFinder withExportTask(List<Long> exportTasksIds);

    List<? extends DataExportOccurrence> find();

    QueryStream<DataExportOccurrence> stream();
}
