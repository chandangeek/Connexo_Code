package com.elster.jupiter.export;


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

public class DataExportOccurrenceFinder {
    private QueryExecutor<TaskOccurrence> taskOccurrenceQuery;
    private QueryExecutor<DataExportOccurrence> queryExecutor;
    private Condition condition;
    private Order[] order;
    private Integer start;
    private Integer limit;
    private Condition startDateCondition;
    private Condition endDateCondition;

    public DataExportOccurrenceFinder() {
    }

    public DataExportOccurrenceFinder(QueryExecutor<DataExportOccurrence> queryExecutor, QueryExecutor<TaskOccurrence> taskOccurrenceQuery, Condition condition, Order order) {
        this.queryExecutor = queryExecutor;
        this.taskOccurrenceQuery = taskOccurrenceQuery;
        this.condition = condition;
        this.order = new Order[]{order};
    }

    public DataExportOccurrenceFinder with(Condition condition) {
        this.condition = this.condition.and(condition);
        return this;
    }

    public DataExportOccurrenceFinder setStart(Integer start) {
        this.start = start;
        return this;
    }

    public DataExportOccurrenceFinder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

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


    public void withStartDateIn(Range<Instant> interval) {
        startDateCondition = where("startDate").in(interval);

    }

    public void withEndDateIn(Range<Instant> interval) {
        endDateCondition = where("endDate").in(interval);

    }

    public void withExportPeriodContaining(Instant timeStamp) {
        condition = condition.and(where("exportedDataInterval").isEffective(timeStamp));
    }
}
