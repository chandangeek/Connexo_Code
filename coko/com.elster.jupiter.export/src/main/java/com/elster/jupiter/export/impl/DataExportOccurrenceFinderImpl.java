package com.elster.jupiter.export.impl;


import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.orm.DataModel;
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
    public DataExportOccurrenceFinder setStart(Integer start) {
        this.start = start;
        return this;
    }

    @Override
    public DataExportOccurrenceFinder setLimit(Integer limit) {
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
    public List<? extends DataExportOccurrence> find() {
        return dataModel.stream(DataExportOccurrence.class)
                .join(TaskOccurrence.class)
                .filter(condition)
                .sorted(order)
                .skip(start)
                .limit(limit)
                .select();
    }

    // documentation only
    public List<? extends DataExportOccurrence> findOldSyntax() {
        return dataModel.query(DataExportOccurrence.class, TaskOccurrence.class)
                .select(condition, new Order[]{order}, true, null, start + 1, start + limit);
    }

}
