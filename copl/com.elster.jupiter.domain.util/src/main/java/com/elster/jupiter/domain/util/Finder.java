/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Hint;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.stream.Stream;

/**
 * Generic finder interfaces adding pageability and sortability to any query.
 */
@ConsumerType
public interface Finder<T> {

    /**
     * Adds paging information to the query. Paging information will be automatically
     * set when using the from(QueryParameters) method. One additional element will be
     * returned to indicate the next page exists.
     *
     * @param start First line to return.
     * @param pageSize The number of lines in page (i.e. number of lines to return minus one:
     * one additional is returned to know if the next page exists).
     * @return A Finder that will return the requested page when asked.
     */
    Finder<T> paged(int start, int pageSize);

    Finder<T> sorted(String sortColumn, boolean ascending);

    Finder<T> withHint(Hint hint);

    Finder<T> sorted(Order order);

    List<T> find();

    default Finder<T> from(QueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            this.paged(queryParameters.getStart().get(), queryParameters.getLimit().get());
        }
        queryParameters.getSortingColumns().forEach(this::sorted);
        return this;
    }

    /**
     * Create stream over the results from the Finder. If the finder was paged, only the results from the requested page
     * will be contained in the stream. If the Finder was unpaged, all results from the Finder will be contained in the stream.
     *
     * @return Stream containing found results.
     */
    default Stream<T> stream() {
        return this.find().stream();
    }

    /**
     * This method was added to Finder to allow reuse of this finder/query in other queries.
     * For the use of Subquery, see the documentation there
     */
    Subquery asSubQuery(String... fieldNames);

    /**
     * This method was added to Finder to allow reuse of this finder/query in other queries.
     * For the use of Subquery, see the documentation there
     */
    SqlFragment asFragment(String... fieldNames);

    /**
     * Imposes a maximum page size for the query. If the actual query is performed with a page larger than the page size,
     * or unpaged altogether, an exception is thrown. This method can only be called once, typically in domain layer, to
     * prevent front end from lauching huge queries that could potentially damage the system. Once set, it can not be reset
     * (by rest layer), this is by design.
     * @param thesaurus Requered to throw the translatable exception
     * @param maxPageSize The maximum allowed page size.
     * @return A finder with the additional restriction of maximum page size
     */
    default Finder<T> maxPageSize(Thesaurus thesaurus, int maxPageSize) { return this; }

    default int count() {
        throw new UnsupportedOperationException("The method is not implemented on this Finder");
    }
}
