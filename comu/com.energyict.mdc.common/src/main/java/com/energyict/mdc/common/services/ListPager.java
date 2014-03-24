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
public class ListPager<T> {

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

    public ListPager<T> paged(Integer start, Integer pageSize) {
        this.start=start;
        this.pageSize=pageSize;
        return this;
    }

    public List<T> find() {
        if (start!=null && pageSize!=null) {
            if (start>=elements.size()) {
                return Collections.emptyList();
            }
            int limit = Math.min(elements.size()-this.start, this.start+this.pageSize+1);// +1 for the 'there is another page'-indicator
            return elements.subList(this.start, limit + this.start);
        } else {
            return elements;
        }
    }

    public ListPager<T> from(QueryParameters queryParameters) {
        this.paged(queryParameters.getStart(), queryParameters.getLimit());
        return this;
    }
}
