package com.energyict.mdc.common.services;

import com.energyict.mdc.common.rest.QueryParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of Finder that allows paging over a list of objects
 * Merely intended as temporary functionality while we wait for back end sorting to be completed
 */
public class ListFinder<T> implements Finder<T> {

    private final List<T> elements = new ArrayList<>();
    private Integer start;
    private Integer pageSize;

    private ListFinder(List<T> elements, Comparator<T> comparator) {
        this.elements.addAll(elements);
        Collections.sort(this.elements, comparator);
    }

    public static <T> ListFinder<T> of(List<T> elements, Comparator<T> comparator) {
        return new ListFinder<>(elements, comparator);
    }

    @Override
    public Finder<T> paged(Integer start, Integer pageSize) {
        this.start=start;
        this.pageSize=pageSize;
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, boolean sortOrder) {
        return this;
    }

    @Override
    public Finder<T> defaultSortColumn(String sortColumn) {
        return this;
    }

    @Override
    public List<T> find() {
        if (start!=null && pageSize!=null) {
            return elements.subList(this.start, this.start+this.pageSize);
        } else {
            return elements;
        }
    }

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        this.paged(queryParameters.getStart(), queryParameters.getLimit());
        return this;
    }
}
