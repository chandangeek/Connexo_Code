package com.energyict.mdc.common.services;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.QueryParameters;
import java.util.ArrayList;
import java.util.List;

public class DefaultFinder<T> implements Finder<T> {

    private final Condition condition;
    private final Class<T> clazz;
    private final QueryExecutor<T> query;

    private Integer start;
    private Integer pageSize;
    private List<Pair<String, SortOrder>> sortingColumns = new ArrayList<>();

    public static <T> Finder<T>  of(Class<T> clazz, Condition condition, DataModel dataModel) {
        return new DefaultFinder<>(clazz, condition, dataModel);
    }

    private DefaultFinder(Class<T> clazz, Condition condition, DataModel dataModel) {
        query = dataModel.query(clazz);
        this.condition = condition;
        this.clazz = clazz;
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
    public Finder<T> sorted(String fieldName, SortOrder sortOrder) {
        if (fieldName!=null && !fieldName.isEmpty() && sortOrder!=null) {
            this.sortingColumns.add(Pair.of(fieldName, sortOrder));
        }
        return this;
    }

    @Override
    public List<T> find() {
        if (start==null || pageSize ==null) {
            return query.select(condition, asStrings(sortingColumns));
        } else {
            return query.select(condition, asStrings(sortingColumns), true, new String[0], this.start + 1, this.start + this.pageSize);
        }
    }

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        this.paged(queryParameters.getStart(), queryParameters.getLimit());
        // TODO sorting ignored so far
        return this;
    }

    private String[] asStrings(List<Pair<String, SortOrder>> sortings) {
        List<String> sortingStrings = new ArrayList<>();
        for (Pair<String, SortOrder> sorting : sortings) {
            String fieldName = sorting.getFirst();
            if (!query.hasField(fieldName)) {
                throw new IllegalArgumentException(String.format("%s has no field '%s'", clazz.getSimpleName(), fieldName));
            }
            sortingStrings.add(sorting.getFirst() /* + " " + sorting.getLast().sql() */);
        }
        return sortingStrings.toArray(new String[sortingStrings.size()]);
    }
}
