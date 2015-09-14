package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTaskOccurrenceFinder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class EstimationTaskOccurrenceFinderImpl implements EstimationTaskOccurrenceFinder {
    private TaskService taskService;
    private Condition condition;
    private Order[] orders;
    private Integer start;
    private Integer limit;

    public EstimationTaskOccurrenceFinderImpl() {
    }

    public EstimationTaskOccurrenceFinderImpl(TaskService taskService, Condition condition, Order... orders) {
        this.taskService = taskService;
        this.condition = condition;
        this.orders = orders;
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
        this.condition = this.condition.and(where("startDate").in(interval));
        return this;
    }

    @Override
    public EstimationTaskOccurrenceFinder withEndDateIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("endDate").in(interval));
        return this;
    }

    @Override
    public List<? extends TaskOccurrence> find() {
        return taskService.getTaskOccurrenceQueryExecutor().select(condition, orders, true, null, start + 1, start + limit);
    }
}
