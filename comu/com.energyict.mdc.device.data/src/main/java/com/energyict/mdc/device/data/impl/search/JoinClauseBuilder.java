package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

/**
 * Models the behavior of a component that is capable of building
 * join clauses for queries. The benefit is in the fact
 * that component do not need to know about the join clauses
 * that were added by other components.
 * A JoinClauseBuilder therefore behaves a bit like a Set does,
 * a join clause can be added multiple times but will
 * at the end appear only once in the actual query.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-29 (08:47)
 */
public interface JoinClauseBuilder {

    JoinClauseBuilder addEndDevice();

    JoinClauseBuilder addEndDeviceStatus();

    JoinClauseBuilder addFiniteState();

    JoinClauseBuilder addBatch();

    JoinClauseBuilder addUsagePoint();

    JoinClauseBuilder addServiceCategory();

    JoinClauseBuilder addTopologyForSlaves();

    JoinClauseBuilder addTopologyForMasters();

    JoinClauseBuilder addMeterValidation();

    JoinClauseBuilder addDeviceType();

    JoinClauseBuilder addConnectionTaskProperties(ConnectionTypePluggableClass connectionTypePluggableClass);

    JoinClauseBuilder addProtocolDialectProperties(long deviceProtocolId, String relationTableName);

    class Aliases {
        private Aliases(){ /* constant class */}

        public static final String DEVICE = "dev";
        public static final String END_DEVICE = "ed";
        public static final String DEVICE_TYPE = "dev_Type";
        public static final String PROTOCOL_DIALECT_PROPS = "pdprops";
    }
}