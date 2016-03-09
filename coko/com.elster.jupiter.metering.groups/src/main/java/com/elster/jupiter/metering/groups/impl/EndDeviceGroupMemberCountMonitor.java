package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.time.Instant;

/**
 * Defines the interface for a component that will monitor the
 * member counts of {@link com.elster.jupiter.metering.groups.EndDeviceGroup}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-09 (16:10)
 */
public interface EndDeviceGroupMemberCountMonitor extends EndDeviceGroupMemberCountMonitorImplMBean {

    /**
     * Notifies this component that a request to count the members of
     * an {@link com.elster.jupiter.metering.groups.EndDeviceGroup}
     * completed in the specified number of nano seconds.
     *
     * @param nanos The number of nano seconds it took to count the members
     * @see EndDeviceGroup#getMemberCount(Instant)
     */
    void countExecuted(long nanos);

    /**
     * Gets the ExecutionStatistics (in nanos) of the requests to count the
     * members of an {@link com.elster.jupiter.metering.groups.EndDeviceGroup}.
     *
     * @return The ExecutionStatistics
     */
    ExecutionStatistics getCountExecutionStatistics();

    interface ExecutionStatistics {
        long getCount();
        long getTotal();
        long getMinimum();
        long getMaximum();
        long getAverage();
    }

}