/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.services;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

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
    public Finder<T> from(QueryParameters queryParameters) {
        return this;
    }

    public interface Callback<T> {
        public List<T> getDataFromFactory();
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
