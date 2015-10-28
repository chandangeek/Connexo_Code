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

    JoinClauseBuilder addConnectionTask();

    JoinClauseBuilder addComTaskExecution();

    JoinClauseBuilder addComSchedule();

    JoinClauseBuilder addBatch();

    JoinClauseBuilder addUsagePoint();

    JoinClauseBuilder addServiceCategory();

    JoinClauseBuilder addTopologyForSlaves();

    JoinClauseBuilder addTopologyForMasters();

    JoinClauseBuilder addMeterValidation();

    JoinClauseBuilder addDeviceEstimation();

    JoinClauseBuilder addRegisterSpec();

    JoinClauseBuilder addRegisterReadingType();

    JoinClauseBuilder addDeviceType();

    JoinClauseBuilder addChannelReadingType();

    JoinClauseBuilder addChannelSpec();

    JoinClauseBuilder addLogbook();

    JoinClauseBuilder addLogbookSpec();

    JoinClauseBuilder addLogbookType();

    JoinClauseBuilder addLoadProfile();

    JoinClauseBuilder addLoadProfileSpec();

    JoinClauseBuilder addLoadProfileType();

    JoinClauseBuilder addConnectionTaskProperties(ConnectionTypePluggableClass connectionTypePluggableClass);

    class Aliases {
        private Aliases(){ /* constant class */}

        public static final String DEVICE = "dev";
        public static final String DEVICE_TYPE = "dev_Type";
        public static final String DEVICE_LOGBOOK = "dev_lb";
        public static final String LOGBOOK_SPEC = "lb_spec";
        public static final String LOGBOOK_TYPE = "lb_type";
        public static final String DEVICE_LOADPROFILE = "dev_lp";
        public static final String LOADPROFILE_SPEC = "lp_spec";
        public static final String LOADPROFILE_TYPE = "lp_type";
    }
}