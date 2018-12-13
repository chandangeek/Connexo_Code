/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.HasId;

import com.google.common.collect.Range;

import javax.inject.Provider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class GroupBuilderImpl<T extends HasId & IdentifiedObject, EG extends AbstractEnumeratedGroup<T>, QG extends AbstractQueryGroup<T>>
        implements GroupBuilder<T, EG, QG> {

    private Provider<EG> enumeratedGroupProvider;
    private Provider<QG> queryGroupProvider;
    private boolean built;

    GroupBuilderImpl(Provider<EG> enumeratedGroupProvider, Provider<QG> queryGroupProvider) {
        this.enumeratedGroupProvider = enumeratedGroupProvider;
        this.queryGroupProvider = queryGroupProvider;
    }

    @SafeVarargs
    @Override
    public final EnumeratedGroupBuilder<T, EG> containing(T... objects) {
        return new EnumeratedGroupBuilderImpl().containing(objects);
    }

    @Override
    public QueryGroupBuilder<T, QG> withConditions(SearchablePropertyValue... conditions) {
        return new QueryGroupBuilderImpl().withConditions(conditions);
    }

    private class EnumeratedGroupBuilderImpl
            implements EnumeratedGroupBuilder<T, EG> {

        private EG underConstruction = enumeratedGroupProvider.get();
        private Instant since = Instant.EPOCH;

        @Override
        public EG create() {
            if (built) {
                throw new IllegalStateException();
            }
            underConstruction.save();
            try {
                return underConstruction;
            } finally {
                built = true;
            }
        }

        @Override
        public EG validate() {
            if (built) {
                throw new IllegalStateException();
            }
            underConstruction.validate();
            return underConstruction;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> at(Instant at) {
            since = at;
            return this;
        }

        @SafeVarargs
        @Override
        public final EnumeratedGroupBuilder<T, EG> containing(T... objects) {
            Range<Instant> range = Range.atLeast(since);
            Arrays.stream(objects)
                    .forEach(object -> underConstruction.add(object, range));
            return this;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> setName(String name) {
            underConstruction.setName(name);
            return this;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> setMRID(String mRID) {
            underConstruction.setMRID(mRID);
            return this;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> setAliasName(String aliasName) {
            underConstruction.setAliasName(aliasName);
            return this;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> setType(String type) {
            underConstruction.setType(type);
            return this;
        }

        @Override
        public EnumeratedGroupBuilder<T, EG> setLabel(String label) {
            underConstruction.setLabel(label);
            return this;
        }
    }

    private class QueryGroupBuilderImpl
            implements QueryGroupBuilder<T, QG> {

        private final QG underConstruction = queryGroupProvider.get();
        private final List<SearchablePropertyValue> conditions = new ArrayList<>();

        @Override
        public QG create() {
            if (built) {
                throw new IllegalStateException();
            }
            underConstruction.save();
            underConstruction.setConditions(conditions);
            try {
                return underConstruction;
            } finally {
                built = true;
            }
        }

        @Override
        public QG validate() {
            if (built) {
                throw new IllegalStateException();
            }
            underConstruction.validate();
            return underConstruction;
        }

        @Override
        public QueryGroupBuilder<T, QG> withConditions(SearchablePropertyValue... conditions) {
            Stream.of(conditions).forEach(this.conditions::add);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setName(String name) {
            underConstruction.setName(name);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setMRID(String mRID) {
            underConstruction.setMRID(mRID);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setAliasName(String aliasName) {
            underConstruction.setAliasName(aliasName);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setType(String type) {
            underConstruction.setType(type);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setLabel(String label) {
            underConstruction.setLabel(label);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setQueryProviderName(String queryProviderName) {
            underConstruction.setQueryProviderName(queryProviderName);
            return this;
        }

        @Override
        public QueryGroupBuilder<T, QG> setSearchDomain(SearchDomain searchDomain) {
            underConstruction.setSearchDomain(searchDomain);
            return this;
        }
    }
}
