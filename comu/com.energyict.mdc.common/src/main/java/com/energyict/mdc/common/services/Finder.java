package com.energyict.mdc.common.services;

import com.elster.jupiter.rest.util.QueryParameters;
import java.util.List;
import javax.ws.rs.core.UriInfo;

/**
 * Generic finder interfaces adding pagability and sortability to any query
 */
public interface Finder<T> {
    public Finder<T> paged(Integer start, Integer pageSize);
    public Finder<T> sorted(String sortColumn, SortOrder sortOrder);
    public List<T> find();
    public Finder<T> from(QueryParameters uriInfo);
}
