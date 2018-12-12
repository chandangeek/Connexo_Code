/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        this.start = start;
        this.pageSize = pageSize;
        return this;
    }

    public ListPager<T> from(com.elster.jupiter.domain.util.QueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            this.paged(queryParameters.getStart().get(), queryParameters.getLimit().get());
        }
        return this;
    }

    public ListPager<T> from(QueryParameters queryParameters) {
        this.paged(queryParameters.getStartInt(), queryParameters.getLimit() == -1 ? null : queryParameters.getLimit());
        return this;
    }

    public List<T> find() {
        if (start != null && pageSize != null) {
            if (start >= elements.size()) {
                return Collections.emptyList();
            }
            int toIndex = this.start + this.pageSize + 1;
            if (toIndex > elements.size()) {
                toIndex = elements.size();
            }
            return elements.subList(this.start, toIndex);
        } else {
            return Collections.unmodifiableList(elements);
        }
    }

}