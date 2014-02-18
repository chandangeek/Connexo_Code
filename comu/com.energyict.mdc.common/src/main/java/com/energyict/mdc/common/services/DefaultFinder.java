package com.energyict.mdc.common.services;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.rest.QueryParameters;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for paged, sortable queries using the datamapper's Query
 * @param <T>
 */
public class DefaultFinder<T> implements Finder<T> {

    private final Condition condition;
    private final QueryExecutor<T> query;

    private Integer start;
    private Integer pageSize;
    private List<Order> sortingColumns = new ArrayList<>();

    public static <T> Finder<T> of(Class<T> clazz, DataModel dataModel) {
        return of(clazz, Condition.TRUE, dataModel);
    }

    public static <T> Finder<T> of(Class<T> clazz, Condition condition, DataModel dataModel) {
        return new DefaultFinder<>(clazz, condition, dataModel);
    }

    public static <T> Finder<T>  of(Class<T> clazz, DataModel dataModel) {
        return new DefaultFinder<>(clazz, Condition.TRUE, dataModel);
    }

    private DefaultFinder(Class<T> clazz, Condition condition, DataModel dataModel) {
        query = dataModel.query(clazz);
        this.condition = condition;
    }

    @Override
    public Finder<T> paged(Integer start, Integer pageSize) {
        if (start!=null && pageSize!=null) {
            this.start=start;
            this.pageSize=pageSize;
        }
        return this;
    }

    @Override
    public Finder<T> sorted(String fieldName, boolean ascending) {
        if (fieldName!=null && !fieldName.isEmpty()) {
            this.sortingColumns.add(ascending?Order.ascending(fieldName):Order.descending(fieldName));
        }
        return this;
    }

    @Override
    public List<T> find() {
        if (start==null || pageSize ==null) {
            return query.select(condition, sortingColumns.toArray(new Order[sortingColumns.size()]));
        } else {
            return query.select(condition, sortingColumns.toArray(new Order[sortingColumns.size()]), true, new String[0], this.start + 1, this.start + this.pageSize);
        }
    }

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        this.paged(queryParameters.getStart(), queryParameters.getLimit());
        for (Order columnSort : queryParameters.getSortingColumns()) {
            this.sorted(columnSort.getName(), columnSort.ascending());
        }

        return this;
    }

}
