/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import java.sql.ResultSet;

/**
 * Models the behavior of a component that will process
 * a {@link BreakdownResult} that was parsed from a ResultSet.
 * Implementing classes are allowed to throw java.lang.UnsupportedOperationException
 * when the {@link BreakdownType} is not expected.
 * @see BreakdownType#parse(ResultSet)
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-06 (09:16)
 */
interface BreakdownResultProcessor {

    /**
     * Adds the {@link BreakdownResult} to this BreakdownResultProcessor.
     * This is the starting point of a three way dispatch mechanism between
     * BreakdownResult, BreakdownType and BreakdownResultProcessor.
     * The BreakdownResult is requested to process with this BreakdownResultProcessor,
     * will in turn delegate to its type and that will then call
     * one of the break down type specific add methods on this BreakdownResultProcessor.
     *
     * @param breakdownResult The BreakdownResult
     */
    default void add(BreakdownResult breakdownResult) {
        breakdownResult.processWith(this);
    }

    /**
     * Adds a {@link BreakdownResult} for {@link BreakdownType#None}.
     *
     * @param breakdownResult The BreakdownResult
     */
    void addOverallStatusCount(BreakdownResult breakdownResult);

    /**
     * Adds a {@link BreakdownResult} for {@link BreakdownType#ConnectionType}.
     *
     * @param breakdownResult The BreakdownResult
     */
    void addConnectionTypeStatusCount(BreakdownResult breakdownResult);

    /**
     * Adds a {@link BreakdownResult} for {@link BreakdownType#ComPortPool}.
     *
     * @param breakdownResult The BreakdownResult
     */
    void addComPortPoolStatusCount(BreakdownResult breakdownResult);

    /**
     * Adds a {@link BreakdownResult} for {@link BreakdownType#ComSchedule}.
     *
     * @param breakdownResult The BreakdownResult
     */
    void addComScheduleStatusCount(BreakdownResult breakdownResult);

    /**
     * Adds a {@link BreakdownResult} for {@link BreakdownType#ComTask}.
     *
     * @param breakdownResult The BreakdownResult
     */
    void addComTaskStatusCount(BreakdownResult breakdownResult);

    /**
     * Adds a {@link BreakdownResult} for {@link BreakdownType#DeviceType}.
     *
     * @param breakdownResult The BreakdownResult
     */
    void addDeviceTypeStatusCount(BreakdownResult breakdownResult);

}