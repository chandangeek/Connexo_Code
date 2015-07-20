package com.elster.jupiter.domain.util;

import com.elster.jupiter.domain.util.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for paged, sortable queries using the datamapper's Query
 * @param <T>
 */
public final class DefaultFinder<T> implements Finder<T> {

    private final Condition condition;
    private final QueryExecutor<T> query;

    private Integer start;
    private Integer pageSize;
    private List<Order> sortingColumns = new ArrayList<>();
    private Order defaultSort;
    private Integer maxPageSize = null;
    private Thesaurus thesaurus;

    public static <T> DefaultFinder<T> of(Class<T> clazz, DataModel dataModel, Class<?> ... eagers) {
        return of(clazz, Condition.TRUE, dataModel, eagers);
    }

    public static <T> DefaultFinder<T> of(Class<T> clazz, Condition condition, DataModel dataModel, Class<?> ... eagers) {
        return new DefaultFinder<>(clazz, condition, dataModel, eagers);
    }

    private DefaultFinder(Class<T> clazz, Condition condition, DataModel dataModel, Class<?> ... eagers) {
        query = dataModel.query(clazz, eagers);
        this.condition = condition;
    }

    @Override
    public Finder<T> paged(int start, int pageSize) {
        this.start=start;
        this.pageSize=pageSize;
        return this;
    }

    @Override
    public Finder<T> sorted(String fieldName, boolean ascending) {
        if (fieldName!=null && !fieldName.isEmpty()) {
            this.sortingColumns.add(ascending?Order.ascending(fieldName):Order.descending(fieldName));
        }
        return this;
    }

    public Finder<T> defaultSortColumn(String sortColumn) {
        this.defaultSort = Order.ascending(sortColumn).toLowerCase();
        return this;
    }

    @Override
    public List<T> find() {
        if (sortingColumns.isEmpty() && defaultSort!=null) {
            sortingColumns.add(defaultSort);
        }
        if (start==null || pageSize ==null) {
            if (maxPageSize!=null) {
                throw new UnpagedNotAllowed(thesaurus, maxPageSize);
            }
            return query.select(condition, sortingColumns.toArray(new Order[sortingColumns.size()]));
        } else {
            if (maxPageSize!=null && pageSize>this.maxPageSize) {
                throw new MaxPageSizeExceeded(thesaurus, maxPageSize);
            }

            return query.select(condition, sortingColumns.toArray(new Order[sortingColumns.size()]), true, new String[0], this.start + 1, this.start + this.pageSize + 1);
        }
    }

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            this.paged(queryParameters.getStart().get(), queryParameters.getLimit().get());
        }
        for (Order columnSort : queryParameters.getSortingColumns()) {
            this.sorted(columnSort.getName(), columnSort.ascending());
        }

        return this;
    }

    @Override
    public Finder<T> maxPageSize(Thesaurus thesaurus, int maxPageSize) {
        this.thesaurus = thesaurus;
        if (this.maxPageSize!=null) {
            throw new IllegalArgumentException("Maximum page size has already been set");
        }
        if (maxPageSize<=0) {
            throw new IllegalArgumentException("Maximum page size must be greater than 0");
        }
        this.maxPageSize = maxPageSize;
        return this;
    }

    @Override
    public Subquery asSubQuery(String... fieldNames) {
        return query.asSubquery(condition,fieldNames);
    }

    @Override
    public SqlFragment asFragment(String... fieldNames) {
        return query.asFragment(condition, fieldNames);
    }

}

class MaxPageSizeExceeded extends LocalizedException {

    protected MaxPageSizeExceeded(Thesaurus thesaurus, int size) {
        super(thesaurus, MessageSeeds.MAX_PAGE_SIZE_EXCEEDED, size);
    }
}

class UnpagedNotAllowed extends LocalizedException {

    protected UnpagedNotAllowed(Thesaurus thesaurus, int size) {
        super(thesaurus, MessageSeeds.UNPAGED_NOT_ALLOWED, size);
    }
}
