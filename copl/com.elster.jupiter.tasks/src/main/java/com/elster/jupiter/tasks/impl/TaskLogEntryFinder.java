/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.logging.LogEntryFinder;

import java.util.List;

class TaskLogEntryFinder implements LogEntryFinder {
    private QueryExecutor<TaskLogEntry> queryExecutor;
    private Condition condition;
    private Order[] orders;
    private Integer start;
    private Integer limit;

    TaskLogEntryFinder(QueryExecutor<TaskLogEntry> queryExecutor, Condition condition, Order[] orders) {
        this.queryExecutor = queryExecutor;
        this.condition = condition;
        this.orders = orders;
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
        return queryExecutor.select(condition, orders, true, null, start+1, start+limit+1);
    }

}