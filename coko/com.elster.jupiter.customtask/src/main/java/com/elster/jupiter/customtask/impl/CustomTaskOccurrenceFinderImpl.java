/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskOccurrenceFinder;
import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

class CustomTaskOccurrenceFinderImpl implements CustomTaskOccurrenceFinder {
    private DataModel dataModel;
    private Condition condition;
    private Order defaultOrder;
    private Order[] sortingColumns = new Order[0];
    private Integer start;
    private Integer limit;

    CustomTaskOccurrenceFinderImpl() {}

    CustomTaskOccurrenceFinderImpl(DataModel dataModel, Condition condition, Order order) {
        this();
        this.dataModel = dataModel;
        this.condition = condition;
        this.defaultOrder = order;
    }

    @Override
    public CustomTaskOccurrenceFinder setStart(int start) {
        this.start = start;
        return this;
    }

    @Override
    public CustomTaskOccurrenceFinder setLimit(int limit) {
        this.limit = limit;
        return this;
    }


    @Override
    public CustomTaskOccurrenceFinder setOrder(List<Order> sortingColumns) {
        this.sortingColumns = sortingColumns.toArray(new Order[sortingColumns.size()]);
        return this;
    }

    @Override
    public CustomTaskOccurrenceFinder withStartDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.startDate").in(interval));
        return this;
    }

    @Override
    public CustomTaskOccurrenceFinder withEndDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.endDate").in(interval));
        return this;
    }

    @Override
    public CustomTaskOccurrenceFinder withStatus(List<CustomTaskStatus> statuses) {
        if (!statuses.isEmpty()) {
            this.condition = this.condition.and(where("status").in(statuses));
        }
        return this;
    }

    @Override
    public CustomTaskOccurrenceFinder setId(long id) {
        this.condition = this.condition.and(where("TASKOCC").isEqualTo(id));
        return this;
    }

    @Override
    public List<? extends CustomTaskOccurrence> find() {
        return stream().select();
    }

    @Override
    public QueryStream<CustomTaskOccurrence> stream() {
        QueryStream<CustomTaskOccurrence> queryStream = dataModel.stream(CustomTaskOccurrence.class)
                .join(TaskOccurrence.class)
                .join(RecurrentTask.class)
                .filter(condition)
                .sorted(defaultOrder, sortingColumns);
        if (start != null) {
            queryStream.skip(start);
        }
        if (limit != null) {
            queryStream.limit(limit);
        }
        return queryStream;
    }

}