package com.elster.jupiter.metering.groups.impl;

import javax.management.openmbean.CompositeData;

/**
 * Defines the interface for the JMX layer
 * for a component that will monitor the
 * member counts of {@link com.elster.jupiter.metering.groups.EndDeviceGroup}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-09 (16:06)
 */
public interface EndDeviceGroupMemberCountMonitorImplMBean {

    /**
     * Gets the ExecutionStatistics (in nanos) of the requests to count the
     * number of members of an {@link com.elster.jupiter.metering.groups.EndDeviceGroup}.
     *
     * @return The ExecutionStatistics
     */
    CompositeData getCountExecutionStatisticsCompositeData();

}