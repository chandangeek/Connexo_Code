package com.energyict.mdc.common.services;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;
import java.util.List;

/**
 * Wraps an actual finder to allow post processing.
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
    public List<T> find() {
        List<S> list = delegate.find();
        return convert(list);
    }

    @Override
    public Finder<T> maxPageSize(Thesaurus thesaurus, int maxPageSize) {
        delegate.maxPageSize(thesaurus,maxPageSize);
        return this;
    }

    public abstract List<T> convert(List<S> list);

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        delegate.from(queryParameters);
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

