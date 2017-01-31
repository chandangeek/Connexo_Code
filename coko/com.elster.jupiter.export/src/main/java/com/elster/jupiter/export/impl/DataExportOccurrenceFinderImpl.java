/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;


import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

class DataExportOccurrenceFinderImpl implements DataExportOccurrenceFinder {
    private DataModel dataModel;
    private Condition condition;
    private Order order;
    private Integer start;
    private Integer limit;

    public DataExportOccurrenceFinderImpl() {
    }

    public DataExportOccurrenceFinderImpl(DataModel dataModel, Condition condition, Order order) {
        this.dataModel = dataModel;
        this.condition = condition;
        this.order = order;
    }

    @Override
    public DataExportOccurrenceFinder setStart(int start) {
        this.start = start;
        return this;
    }

    @Override
    public DataExportOccurrenceFinder setLimit(int limit) {
        this.limit = limit;
        return this;
    }


    @Override
    public DataExportOccurrenceFinder withStartDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.startDate").in(interval));
        return this;
    }

    @Override
    public DataExportOccurrenceFinder withEndDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.endDate").in(interval));
        return this;
    }

    @Override
    public DataExportOccurrenceFinder withExportPeriodContaining(Instant timeStamp) {
        condition = condition.and(where("exportedDataInterval").isEffective(timeStamp));
        return this;
    }

    @Override
    public DataExportOccurrenceFinder withExportStatus(List<DataExportStatus> statuses) {
        if (!statuses.isEmpty()) {
            this.condition = this.condition.and(where("status").in(statuses));
        }
        return this;
    }

    @Override
    public List<? extends DataExportOccurrence> find() {
        return stream().select();
    }

    @Override
    public QueryStream<DataExportOccurrence> stream() {
        QueryStream<DataExportOccurrence> queryStream = dataModel.stream(DataExportOccurrence.class)
                .join(TaskOccurrence.class)
                .filter(condition)
                .sorted(order);
        if (start != null) {
            queryStream.skip(start);
        }
        if (limit != null) {
            queryStream.limit(limit);
        }
        return queryStream;
    }

    // documentation only
    public List<? extends DataExportOccurrence> findOldSyntax() {
        return dataModel.query(DataExportOccurrence.class, TaskOccurrence.class)
                .select(condition, new Order[]{order}, true, null, start + 1, start + limit);
    }

}
