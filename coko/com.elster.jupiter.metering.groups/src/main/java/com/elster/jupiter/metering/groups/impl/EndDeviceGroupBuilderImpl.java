package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroupBuilder;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Arrays;

class EndDeviceGroupBuilderImpl implements EndDeviceGroupBuilder {

    private final Provider<EnumeratedEndDeviceGroupImpl> enumeratedProvider;
    private final Provider<QueryEndDeviceGroupImpl> queryProvider;
    private boolean built;

    @Inject
    EndDeviceGroupBuilderImpl(Provider<EnumeratedEndDeviceGroupImpl> enumeratedProvider, Provider<QueryEndDeviceGroupImpl> queryProvider) {
        this.enumeratedProvider = enumeratedProvider;
        this.queryProvider = queryProvider;
    }

    @Override
    public EnumeratedEndDeviceGroupBuilder containing(EndDevice... endDevices) {
        return new EnumeratedEndDeviceGroupBuilderImpl().containing(endDevices);
    }

    @Override
    public QueryEndDeviceGroupBuilder withCondition(Condition condition) {
        return new QueryEndDeviceGroupBuilderImpl().withCondition(condition);
    }

    private class EnumeratedEndDeviceGroupBuilderImpl implements EnumeratedEndDeviceGroupBuilder {

        private EnumeratedEndDeviceGroupImpl underConstruction = enumeratedProvider.get();
        private Instant since = Instant.EPOCH;

        @Override
        public EnumeratedEndDeviceGroup create() {
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
        public EnumeratedEndDeviceGroupBuilder at(Instant at) {
            since = at;
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder containing(EndDevice... endDevices) {
            Range<Instant> range = Range.atLeast(since);
            Arrays.stream(endDevices)
                    .forEach(endDevice -> underConstruction.add(endDevice, range));
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder setName(String name) {
            underConstruction.setName(name);
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder setMRID(String mRID) {
            underConstruction.setMRID(mRID);
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder setAliasName(String aliasName) {
            underConstruction.setAliasName(aliasName);
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder setType(String type) {
            underConstruction.setType(type);
            return this;
        }

        @Override
        public EnumeratedEndDeviceGroupBuilder setLabel(String label) {
            underConstruction.setLabel(label);
            return this;
        }
    }

    private class QueryEndDeviceGroupBuilderImpl implements QueryEndDeviceGroupBuilder {

        private QueryEndDeviceGroupImpl underConstruction = queryProvider.get();

        @Override
        public QueryEndDeviceGroup create() {
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
        public QueryEndDeviceGroupBuilder withCondition(Condition condition) {
            underConstruction.setCondition(condition);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setName(String name) {
            underConstruction.setName(name);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setMRID(String mRID) {
            underConstruction.setMRID(mRID);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setAliasName(String aliasName) {
            underConstruction.setAliasName(aliasName);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setType(String type) {
            underConstruction.setType(type);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setLabel(String label) {
            underConstruction.setLabel(label);
            return this;
        }

        @Override
        public QueryEndDeviceGroupBuilder setQueryProviderName(String queryProviderName) {
            underConstruction.setQueryProviderName(queryProviderName);
            return this;
        }
    }
}
