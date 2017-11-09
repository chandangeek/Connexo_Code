/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportStatus;
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

class DataExportOccurrenceFinderImpl implements DataExportOccurrenceFinder {
    private DataModel dataModel;
    private Condition condition;
    private Order defaultOrder;
    private Order[] sortingColumns = new Order[0];
    private Integer start;
    private Integer limit;

    DataExportOccurrenceFinderImpl() {}

    DataExportOccurrenceFinderImpl(DataModel dataModel, Condition condition, Order order) {
        this();
        this.dataModel = dataModel;
        this.condition = condition;
        this.defaultOrder = order;
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
    public DataExportOccurrenceFinder setOrder(List<Order> sortingColumns) {
        this.sortingColumns = sortingColumns.toArray(new Order[sortingColumns.size()]);
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
    public DataExportOccurrenceFinder withExportTask(List<Long> exportTasksIds) {
        this.condition = this.condition.and(where("RTEXPORTTASK").in(exportTasksIds));
        return this;
    }

    @Override
    public DataExportOccurrenceFinder setId(long id) {
        this.condition = this.condition.and(where("TASKOCC").isEqualTo(id));
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