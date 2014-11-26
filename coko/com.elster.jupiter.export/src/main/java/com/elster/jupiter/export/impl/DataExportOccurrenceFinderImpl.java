package com.elster.jupiter.export.impl;


import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.IDataExportOccurrenceFinder;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class DataExportOccurrenceFinderImpl implements IDataExportOccurrenceFinder {
    private QueryExecutor<TaskOccurrence> taskOccurrenceQuery;
    private QueryExecutor<DataExportOccurrence> queryExecutor;
    private Condition condition;
    private Order[] order;
    private Integer start;
    private Integer limit;
    private Condition startDateCondition;
    private Condition endDateCondition;

    public DataExportOccurrenceFinderImpl() {
    }

    public DataExportOccurrenceFinderImpl(QueryExecutor<DataExportOccurrence> queryExecutor, QueryExecutor<TaskOccurrence> taskOccurrenceQuery, Condition condition, Order order) {
        this.queryExecutor = queryExecutor;
        this.taskOccurrenceQuery = taskOccurrenceQuery;
        this.condition = condition;
        this.order = new Order[]{order};
    }

    @Override
    public IDataExportOccurrenceFinder setStart(Integer start) {
        this.start = start;
        return this;
    }

    @Override
    public IDataExportOccurrenceFinder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }


    @Override
    public IDataExportOccurrenceFinder withStartDateIn(Range<Instant> interval) {
        startDateCondition = where("startDate").in(interval);
        return this;
    }

    @Override
    public IDataExportOccurrenceFinder withEndDateIn(Range<Instant> interval) {
        endDateCondition = where("endDate").in(interval);
        return this;
    }

    @Override
    public IDataExportOccurrenceFinder withExportPeriodContaining(Instant timeStamp) {
        condition = condition.and(where("exportedDataInterval").isEffective(timeStamp));
        return this;
    }

    @Override
    public List<? extends DataExportOccurrence> find() {
        if (startDateCondition != null || endDateCondition != null) {
            Condition taskOccurrenceCondition = Condition.TRUE;
            if (startDateCondition != null) {
                taskOccurrenceCondition = taskOccurrenceCondition.and(startDateCondition);
            }
            if (endDateCondition != null) {
                taskOccurrenceCondition = taskOccurrenceCondition.and(endDateCondition);
            }
            Subquery idQuery = taskOccurrenceQuery.asSubquery(taskOccurrenceCondition, "ID");
            condition = condition.and(ListOperator.IN.contains(idQuery, "taskOccurrence"));

        }
        return queryExecutor.select(condition, order, true, null, start + 1, start + limit + 1);
    }
}
