package com.energyict.mdc.common.services;

import com.energyict.mdc.common.rest.QueryParameters;
import java.util.List;

/**
 * Generic finder interfaces adding pagability and sortability to any query
 */
public interface Finder<T> {
    public Finder<T> paged(Integer start, Integer pageSize);
    public Finder<T> sorted(String sortColumn, boolean sortOrder);
    public List<T> find();
    public Finder<T> from(QueryParameters uriInfo);
}
