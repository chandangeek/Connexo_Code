/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Order;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface CustomTaskOccurrenceFinder {
    CustomTaskOccurrenceFinder setId(long id);

    CustomTaskOccurrenceFinder setStart(int start);

    CustomTaskOccurrenceFinder setLimit(int limit);

    CustomTaskOccurrenceFinder setOrder(List<Order> sortingColumns);

    CustomTaskOccurrenceFinder withStartDateIn(Range<Instant> interval);

    CustomTaskOccurrenceFinder withEndDateIn(Range<Instant> interval);

    CustomTaskOccurrenceFinder withStatus(List<CustomTaskStatus> statuses);

    List<? extends CustomTaskOccurrence> find();

    QueryStream<CustomTaskOccurrence> stream();
}
