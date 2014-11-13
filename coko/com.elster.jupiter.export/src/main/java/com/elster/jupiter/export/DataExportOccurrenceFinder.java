package com.elster.jupiter.export;


import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;

public class DataExportOccurrenceFinder {
    private QueryExecutor<DataExportOccurrence> queryExecutor;
    private Condition condition;
    private Order[] order;
    private Integer start;
    private Integer limit;

    public DataExportOccurrenceFinder() {}

    public DataExportOccurrenceFinder(QueryExecutor<DataExportOccurrence> queryExecutor, Condition condition, Order order) {
        this.queryExecutor = queryExecutor;
        this.condition = condition;
        this.order = new Order[] {order};
    }

    public DataExportOccurrenceFinder with (Condition condition) {
        this.condition = this.condition.and(condition);
        return this;
    }

    public DataExportOccurrenceFinder setStart (Integer start) {
       this.start = start;
       return this;
    }

    public DataExportOccurrenceFinder setLimit (Integer limit) {
        this.limit = limit;
        return this;
    }

    public List<? extends DataExportOccurrence> find() {
        return queryExecutor.select(condition, order, true, null, start, limit);
    }
}
