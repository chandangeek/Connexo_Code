package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.logging.LogEntryFinder;

import java.util.List;

public class RecurrentTaskFinder implements TaskFinder {
    private QueryExecutor<RecurrentTask> queryExecutor;
    private Condition condition;
    private Order[] orders;
    private Integer start;
    private Integer limit;

    public RecurrentTaskFinder() {}

    public RecurrentTaskFinder(QueryExecutor<RecurrentTask> queryExecutor, Condition condition, Order[] orders) {
        this.queryExecutor = queryExecutor;
        this.condition = condition;
        this.orders = orders;
    }

    public RecurrentTaskFinder with (Condition condition) {
        this.condition = this.condition.and(condition);
        return this;
    }

    public RecurrentTaskFinder setStart (Integer start) {
        this.start = start;
        return this;
    }

    public RecurrentTaskFinder setLimit (Integer limit) {
        this.limit = limit;
        return this;
    }

    public TaskFinder setCondition (Condition condition) {
        this.condition = condition;
        return this;
    }

    public List<? extends RecurrentTask> find() {
        return queryExecutor.select(condition, orders, true, null, start+1, start+limit+1);
    }
}

