package com.energyict.mdc.common.services;

import com.elster.jupiter.domain.util.QueryParameters;
import java.util.List;

/**
 * Wraps an actual finder to allow post processing
 */
public abstract class WrappingFinder<T, S> implements Finder<T> {
    private final Finder<S> delegate;

    public WrappingFinder(Finder<S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Finder<T> paged(int start, int pageSize) {
        delegate.paged(start, pageSize);
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, boolean sortOrder) {
        delegate.sorted(sortColumn, sortOrder);
        return this;
    }

    @Override
    public Finder<T> defaultSortColumn(String sortColumn) {
        delegate.defaultSortColumn(sortColumn);
        return this;
    }

    @Override
    public List<T> find() {
        List<S> list = delegate.find();
        return convert(list);
    }

    public abstract List<T> convert(List<S> list);

    @Override
    public Finder<T> from(QueryParameters uriInfo) {
        delegate.from(uriInfo);
        return this;
    }
}

