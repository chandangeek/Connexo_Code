package com.elster.jupiter.tasks;

import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;

public class TaskLogEntryFinder {
    private QueryExecutor<TaskLogEntry> queryExecutor;
    private Condition condition;
    private Order[] order;
    private Integer start;
    private Integer limit;

    public TaskLogEntryFinder() {}

    public TaskLogEntryFinder(QueryExecutor<TaskLogEntry> queryExecutor, Condition condition, Order order) {
        this.queryExecutor = queryExecutor;
        this.condition = condition;
        this.order = new Order[] {order};
    }

    public TaskLogEntryFinder with (Condition condition) {
        this.condition = this.condition.and(condition);
        return this;
    }

    public TaskLogEntryFinder setStart (Integer start) {
        this.start = start;
        return this;
    }

    public TaskLogEntryFinder setLimit (Integer limit) {
        this.limit = limit;
        return this;
    }

    public List<? extends TaskLogEntry> find() {
        return queryExecutor.select(condition, order, true, null, start, limit);
    }
}
