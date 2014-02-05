package com.energyict.mdc.common.services;

import com.elster.jupiter.rest.util.QueryParameters;
import java.util.List;
import javax.ws.rs.core.UriInfo;

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
    public Finder<T> paged(Integer start, Integer pageSize) {
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, SortOrder sortOrder) {
        return this;
    }

    @Override
    public List<T> find() {
        return callback.getDataFromFactory();
    }

    @Override
    public Finder<T> from(QueryParameters queryParameters) {
        return this;
    }

    public interface Callback<T> {
        public List<T> getDataFromFactory();
    }
}
