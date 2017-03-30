/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class DataValidationOccurrenceFinderImpl implements DataValidationOccurrenceFinder {
    private DataModel dataModel;
    private Condition condition;
    private Order order;
    private Integer start;
    private Integer limit;

    public DataValidationOccurrenceFinderImpl() {
    }

    public DataValidationOccurrenceFinderImpl(DataModel dataModel, Condition condition, Order order) {
        this.dataModel = dataModel;
        this.condition = condition;
        this.order = order;
    }

    @Override
    public DataValidationOccurrenceFinder setStart(Integer start) {
        this.start = start;
        return this;
    }

    @Override
    public DataValidationOccurrenceFinder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }


    @Override
    public DataValidationOccurrenceFinder withStartDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.startDate").in(interval));
        return this;
    }

    @Override
    public DataValidationOccurrenceFinder withEndDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.endDate").in(interval));
        return this;
    }

    @Override
    public List<? extends DataValidationOccurrence> find() {
        return dataModel.stream(DataValidationOccurrence.class)
                .join(TaskOccurrence.class)
                .filter(condition)
                .sorted(order)
                .skip(start)
                .limit(limit)
                .select();
    }

    // documentation only
    public List<? extends DataValidationOccurrence> findOldSyntax() {
        return dataModel.query(DataValidationOccurrence.class, TaskOccurrence.class)
                .select(condition, new Order[]{order}, true, null, start + 1, start + limit);
    }

}
