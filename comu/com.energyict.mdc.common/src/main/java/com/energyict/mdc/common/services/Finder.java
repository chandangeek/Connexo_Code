package com.energyict.mdc.common.services;

import java.util.List;

/**
 * Generic finder interfaces adding pagability and sortability to any query
 */
public interface Finder<T> {
    public Finder<T> paged(Integer start, Integer pageSize);
    public Finder<T> sorted(String sortColumn, SortOrder sortOrder);

    public List<T> find();
}
