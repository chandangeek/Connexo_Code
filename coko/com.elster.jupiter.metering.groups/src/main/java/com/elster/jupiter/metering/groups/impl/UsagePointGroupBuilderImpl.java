package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroupBuilder;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Arrays;

class UsagePointGroupBuilderImpl implements UsagePointGroupBuilder {

    private final Provider<EnumeratedUsagePointGroupImpl> enumeratedProvider;
    private final Provider<QueryUsagePointGroupImpl> queryProvider;
    private boolean built;

    @Inject
    UsagePointGroupBuilderImpl(Provider<EnumeratedUsagePointGroupImpl> enumeratedProvider, Provider<QueryUsagePointGroupImpl> queryProvider) {
        this.enumeratedProvider = enumeratedProvider;
        this.queryProvider = queryProvider;
    }

    @Override
    public EnumeratedUsagePointGroupBuilder enumerated() {
        return new EnumeratedUsagePointGroupBuilderImpl();
    }

    @Override
    public QueryUsagePointGroupBuilder withConditions(Condition... conditions) {
        return new QueryUsagePointGroupBuilderImpl().withConditions(conditions);
    }

    private class EnumeratedUsagePointGroupBuilderImpl implements EnumeratedUsagePointGroupBuilder {

        private EnumeratedUsagePointGroupImpl underConstruction = enumeratedProvider.get();
        private Instant since = Instant.EPOCH;

        @Override
        public EnumeratedUsagePointGroup create() {
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
        public EnumeratedUsagePointGroupBuilder at(Instant at) {
            since = at;
            return this;
        }

        @Override
        public EnumeratedUsagePointGroupBuilder containing(UsagePoint... usagePoints) {
            Range<Instant> range = Range.atLeast(since);
            Arrays.stream(usagePoints)
                    .forEach(usagePoint -> underConstruction.add(usagePoint, range));
            return this;
        }

        @Override
        public EnumeratedUsagePointGroupBuilder setName(String name) {
            underConstruction.setName(name);
            return this;
        }

        @Override
        public EnumeratedUsagePointGroupBuilder setMRID(String mRID) {
            underConstruction.setMRID(mRID);
            return this;
        }

        @Override
        public EnumeratedUsagePointGroupBuilder setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public EnumeratedUsagePointGroupBuilder setAliasName(String aliasName) {
            underConstruction.setAliasName(aliasName);
            return this;
        }

        @Override
        public EnumeratedUsagePointGroupBuilder setType(String type) {
            underConstruction.setType(type);
            return this;
        }
    }

    private class QueryUsagePointGroupBuilderImpl implements QueryUsagePointGroupBuilder {

        private QueryUsagePointGroupImpl underConstruction = queryProvider.get();

        @Override
        public QueryUsagePointGroup create() {
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
        public QueryUsagePointGroupBuilder withConditions(Condition... conditions) {
            Condition condition = Arrays.stream(conditions)
                    .reduce(Condition.TRUE, Condition::and);
            underConstruction.setCondition(condition);
            return this;
        }

        @Override
        public QueryUsagePointGroupBuilder setName(String name) {
            underConstruction.setName(name);
            return this;
        }

        @Override
        public QueryUsagePointGroupBuilder setMRID(String mRID) {
            underConstruction.setMRID(mRID);
            return this;
        }

        @Override
        public QueryUsagePointGroupBuilder setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public QueryUsagePointGroupBuilder setAliasName(String aliasName) {
            underConstruction.setAliasName(aliasName);
            return this;
        }

        @Override
        public QueryUsagePointGroupBuilder setType(String type) {
            underConstruction.setType(type);
            return this;
        }

    }
}
