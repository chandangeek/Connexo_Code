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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Builder for paged, sortable queries using the datamapper's Query
 *
 * @param <T>
 */
public final class DefaultFinder<T> implements Finder<T> {

    public static final int DEFAULT_MAX_PAGE_SIZE = 100;
    private final Condition condition;
    private final QueryExecutor<T> query;

    private Integer start;
    private Integer pageSize;
    private List<Order> sortingColumns = new ArrayList<>();
    private Order defaultSort;
    private Integer maxPageSize = null;
    private Thesaurus thesaurus;

    public static <T> DefaultFinder<T> of(Class<T> clazz, DataModel dataModel, Class<?>... eagers) {
        return of(clazz, Condition.TRUE, dataModel, eagers);
    }

    public static <T> DefaultFinder<T> of(Class<T> clazz, Condition condition, DataModel dataModel, Class<?>... eagers) {
        return new DefaultFinder<>(clazz, condition, dataModel, eagers);
    }

    private DefaultFinder(Class<T> clazz, Condition condition, DataModel dataModel, Class<?>... eagers) {
        query = dataModel.query(clazz, eagers);
        this.condition = condition;
    }

    @Override
    public DefaultFinder<T> paged(int start, int pageSize) {
        this.start = start;
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public DefaultFinder<T> sorted(String fieldName, boolean ascending) {
        if (fieldName != null && !fieldName.isEmpty()) {
            this.sortingColumns.add(ascending ? Order.ascending(fieldName) : Order.descending(fieldName));
        }
        return this;
    }

    public DefaultFinder<T> defaultSortColumn(String sortColumn) {
        this.defaultSort = Order.ascending(sortColumn).toLowerCase();
        return this;
    }

    @Override
    public List<T> find() {
        if (start == null || pageSize == null) {
            if (maxPageSize != null) {
                throw new UnpagedNotAllowed(thesaurus, maxPageSize);
            }
            return query.select(condition, getActualSortingColumns());
        } else {
            if (maxPageSize != null && pageSize > this.maxPageSize) {
                throw new MaxPageSizeExceeded(thesaurus, maxPageSize);
            }
            return query.select(condition, getActualSortingColumns(), true, new String[0], this.start + 1, this.start + this.pageSize + 1);
        }
    }

    @Override
    public DefaultFinder<T> from(QueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            this.paged(queryParameters.getStart().get(), queryParameters.getLimit().get());
        }
        for (Order columnSort : queryParameters.getSortingColumns()) {
            this.sorted(columnSort.getName(), columnSort.ascending());
        }

        return this;
    }

    @Override
    public DefaultFinder<T> maxPageSize(Thesaurus thesaurus, int maxPageSize) {
        this.thesaurus = thesaurus;
        if (this.maxPageSize != null) {
            throw new IllegalArgumentException("Maximum page size has already been set");
        }
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("Maximum page size must be greater than 0");
        }
        this.maxPageSize = maxPageSize;
        return this;
    }

    @Override
    public Subquery asSubQuery(String... fieldNames) {
        if (start == null || pageSize == null) {
            return query.asSubquery(condition, fieldNames, Order.NOORDER);
        } else {
            return query.asSubquery(condition, start + 1, start + pageSize + 1, fieldNames, getActualSortingColumns());
        }
    }

    @Override
    public SqlFragment asFragment(String... fieldNames) {
        return query.asFragment(condition, fieldNames, Order.NOORDER);
    }

    @Override
    public Stream<T> stream() {
        if (start != null && pageSize != null) {
            return Finder.super.stream();
        }
        Iterable<T> iterable = PagingIterator::new;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public Order[] getActualSortingColumns() {
        int sortingColumnsCount = sortingColumns.size();
        return sortingColumnsCount == 0 && defaultSort != null ?
                new Order[]{defaultSort} :
                sortingColumns.toArray(new Order[sortingColumnsCount]);
    }

    /**
     * This class iterates over an QueryExcuter's results, allowing a Stream to be build from it
     * The iterator will use paging to avoid loading all results in a list
     */
    private class PagingIterator<E> implements Iterator<E> {
        private final int pageSize = maxPageSize == null ? DEFAULT_MAX_PAGE_SIZE : Math.min(DEFAULT_MAX_PAGE_SIZE, maxPageSize);
        private int currentPage = 0;
        private int currentItemInPage = 0;
        private List<E> items = null;

        @Override
        public boolean hasNext() {
            if (needsToLoadNewPage()) {
                loadNextPage();
            }
            return currentItemInPage < items.size() || items.size() > pageSize;
        }

        @Override
        public E next() {
            if (needsToLoadNewPage()) {
                loadNextPage();
            }
            return items.get(currentItemInPage++);
        }

        private boolean needsToLoadNewPage() {
            return items == null || (this.currentItemInPage == pageSize && items.size() == pageSize + 1);
        }

        private void loadNextPage() {
            items = (List<E>) query.select(condition, getActualSortingColumns(), true, new String[0], currentPage + 1, currentPage + pageSize + 1);
            currentPage += pageSize;
            currentItemInPage = 0;
        }
    }

    static class MaxPageSizeExceeded extends LocalizedException {

        protected MaxPageSizeExceeded(Thesaurus thesaurus, int size) {
            super(thesaurus, MessageSeeds.MAX_PAGE_SIZE_EXCEEDED, size);
        }
    }

    static class UnpagedNotAllowed extends LocalizedException {

        protected UnpagedNotAllowed(Thesaurus thesaurus, int size) {
            super(thesaurus, MessageSeeds.UNPAGED_NOT_ALLOWED, size);
        }
    }

}
