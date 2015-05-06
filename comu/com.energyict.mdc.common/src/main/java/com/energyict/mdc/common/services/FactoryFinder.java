package com.energyict.mdc.common.services;

import com.energyict.mdc.common.rest.QueryParameters;
import java.util.List;

/**
 * The Kore Finder backported for use by legacy factories
 * @param <T>
 */
public class FactoryFinder<T> implements Finder<T> {

    private final Callback<T> callback;

    public FactoryFinder(Callback<T> callback) {
        this.callback = callback;
    }

    public static <T> Finder<T> of(Callback<T> callback) {
        return new FactoryFinder<T>(callback);
    }
    @Override
    public Finder<T> paged(int start, int pageSize) {
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, boolean sortOrder) {
        return this;
    }

    @Override
    public List<T> find() {
        return callback.getDataFromFactory();
    }

    @Override
    public Finder<T> defaultSortColumn(String sortColumn) {
        return this;
    }

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        return this;
    }

    public interface Callback<T> {
        public List<T> getDataFromFactory();
    }
}
