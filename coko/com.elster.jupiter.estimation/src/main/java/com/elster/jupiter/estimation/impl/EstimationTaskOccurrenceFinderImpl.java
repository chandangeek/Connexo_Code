package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTaskOccurrence;
import com.elster.jupiter.estimation.EstimationTaskOccurrenceFinder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class EstimationTaskOccurrenceFinderImpl implements EstimationTaskOccurrenceFinder {
    private DataModel dataModel;
    private Condition condition;
    private Order order;
    private Integer start;
    private Integer limit;

    public EstimationTaskOccurrenceFinderImpl() {
    }

    public EstimationTaskOccurrenceFinderImpl(DataModel dataModel, Condition condition, Order order) {
        this.dataModel = dataModel;
        this.condition = condition;
        this.order = order;
    }

    @Override
    public EstimationTaskOccurrenceFinder setStart(Integer start) {
        this.start = start;
        return this;
    }

    @Override
    public EstimationTaskOccurrenceFinder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public EstimationTaskOccurrenceFinder withStartDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.startDate").in(interval));
        return this;
    }

    @Override
    public EstimationTaskOccurrenceFinder withEndDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("taskOccurrence.endDate").in(interval));
        return this;
    }

    @Override
    public List<? extends EstimationTaskOccurrence> find() {
        return dataModel.stream(EstimationTaskOccurrence.class)
                .join(TaskOccurrence.class)
                .filter(condition)
//                .sorted(order)
                .skip(start)
                .limit(limit)
                .select();
    }
}
