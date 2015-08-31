package com.elster.insight.common.services;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of Finder that allows paging over a list of objects
 * Merely intended as temporary functionality while we wait for back end sorting to be completed
 */
public class ListPager<T> implements Finder<T> {

    private final List<T> elements = new ArrayList<>();
    private Integer start;
    private Integer pageSize;

    private ListPager(List<T> elements) {
        this.elements.addAll(elements);
    }

    public static <T> ListPager<T> of(List<T> elements, Comparator<T> comparator) {
        ListPager<T> listPager = new ListPager<>(elements);
        Collections.sort(listPager.elements, comparator);
        return listPager;
    }

    public static <T> ListPager<T> of(List<T> elements) {
        return new ListPager<>(elements);
    }

    public ListPager<T> paged(int start, int pageSize) {
        this.start=start;
        this.pageSize=pageSize;
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, boolean ascending) {
        throw new RuntimeException("Sorting not supported yet. Use DefaultFinder instead");
    }

    public List<T> find() {
        if (start!=null && pageSize!=null) {
            if (start>=elements.size()) {
                return Collections.emptyList();
            }
            int toIndex = this.start + this.pageSize + 1;
            if (toIndex > elements.size()) {
                toIndex = elements.size();
            }
            return elements.subList(this.start, toIndex);
        } else {
            return elements;
        }
    }

    public ListPager<T> from(QueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            this.paged(queryParameters.getStart().get(), queryParameters.getLimit().get());
        }
        return this;
    }

    @Override
    public Subquery asSubQuery(String... fieldNames) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public SqlFragment asFragment(String... fieldNames) {
        throw new IllegalStateException("Not implemented");
    }

}
