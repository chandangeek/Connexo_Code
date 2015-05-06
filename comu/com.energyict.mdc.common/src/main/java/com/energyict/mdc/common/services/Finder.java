package com.energyict.mdc.common.services;

import com.elster.jupiter.domain.util.QueryParameters;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generic finder interfaces adding pagability and sortability to any query
 */
public interface Finder<T> {
    public Finder<T> paged(int start, int pageSize);

    public Finder<T> sorted(String sortColumn, boolean ascending);

    public Finder<T> defaultSortColumn(String sortColumn);

    public List<T> find();

    public Finder<T> from(QueryParameters uriInfo);

    default Stream<T> stream() {
        return this.find().stream();
    }
}
