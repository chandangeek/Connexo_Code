/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.device.data.impl.tasks.report.AbstractBreakdownSqlExecutor.COUNT_COLUMN_NUMBER;
import static com.energyict.mdc.device.data.impl.tasks.report.AbstractBreakdownSqlExecutor.STATUS_COLUMN_NUMBER;
import static com.energyict.mdc.device.data.impl.tasks.report.AbstractBreakdownSqlExecutor.TARGET_ID_COLUMN_NUMBER;
import static com.energyict.mdc.device.data.impl.tasks.report.AbstractBreakdownSqlExecutor.TYPE_COLUMN_NUMBER;

/**
 * Models the different aspects of {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * and {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s that are used in reports
 * that count these tasks, breaking the counters down by different aspects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-06 (08:52)
 */
enum BreakdownType {
    None {
        @Override
        BreakdownResult parse(ResultSet row) throws SQLException {
            try {
                ServerComTaskStatus taskStatus = ServerComTaskStatus.valueOf(row.getString(STATUS_COLUMN_NUMBER));
                return BreakdownResult
                            .noBreakdown(
                                taskStatus,
                                row.getLong(COUNT_COLUMN_NUMBER));
            } catch (IllegalArgumentException e) {
                LOGGER.severe("ComTaskExecution with id " + row.getString(STATUS_COLUMN_NUMBER) + " is in unknown status for the breakdown queries, assuming " + TaskStatus.initial() + " as default value.");
                return BreakdownResult
                            .noBreakdown(
                                ServerComTaskStatus.NeverCompleted,
                                row.getLong(COUNT_COLUMN_NUMBER));
            }
        }

        @Override
        void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor) {
            processor.addOverallStatusCount(breakdownResult);
        }
    },
    ComPortPool {
        @Override
        boolean supportsComTaskExecution() {
            return false;
        }

        @Override
        void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor) {
            processor.addComPortPoolStatusCount(breakdownResult);
        }
    },
    ConnectionType {
        @Override
        boolean supportsComTaskExecution() {
            return false;
        }

        @Override
        void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor) {
            processor.addConnectionTypeStatusCount(breakdownResult);
        }
    },
    ComSchedule {
        @Override
        boolean supportsConnectionTask() {
            return false;
        }

        @Override
        void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor) {
            processor.addComScheduleStatusCount(breakdownResult);
        }
    },
    ComTask {
        @Override
        boolean supportsConnectionTask() {
            return false;
        }

        @Override
        void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor) {
            processor.addComTaskStatusCount(breakdownResult);
        }
    },
    DeviceType {
        @Override
        void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor) {
            processor.addDeviceTypeStatusCount(breakdownResult);
        }
    };

    Logger LOGGER = Logger.getLogger(BreakdownType.class.getName());

    /**
     * Returns the Set of BreakdownType that support {@link ConnectionTask}s.
     *
     * @return The Set of BreakdownType that support ConnectionTask
     * @see #supportsConnectionTask()
     */
    static Set<BreakdownType> forConnectionTasks() {
        Set<BreakdownType> breakdownTypes = EnumSet.noneOf(BreakdownType.class);
        breakdownTypes.addAll(
                Stream.of(values())
                    .filter(BreakdownType::supportsConnectionTask)
                    .collect(Collectors.toSet()));
        return breakdownTypes;
    }

    /**
     * Returns the Set of BreakdownType that support {@link ComTaskExecution}s.
     *
     * @return The Set of BreakdownType that support ComTaskExecution
     * @see #supportsComTaskExecution()
     */
    static Set<BreakdownType> forComTaskExecutions() {
        Set<BreakdownType> breakdownTypes = EnumSet.noneOf(BreakdownType.class);
        breakdownTypes.addAll(
                Stream.of(values())
                    .filter(BreakdownType::supportsComTaskExecution)
                    .collect(Collectors.toSet()));
        return breakdownTypes;
    }

    static BreakdownResult resultFor(ResultSet row) throws SQLException {
        BreakdownType breakdownType = BreakdownType.valueOf(row.getString(TYPE_COLUMN_NUMBER));
        return breakdownType.parse(row);
    }

    BreakdownResult parse(ResultSet row) throws SQLException {
        return BreakdownResult.from(
                this,
                ServerComTaskStatus.valueOf(row.getString(STATUS_COLUMN_NUMBER)),
                row.getLong(TARGET_ID_COLUMN_NUMBER),
                row.getLong(COUNT_COLUMN_NUMBER));
    }

    /**
     * Tests if this BreakdownType supports {@link ConnectionTask}s.
     * This is only the case when the enum value relates to an
     * attribute of a ConnectionTask.
     *
     * @return A flag that indicates if this BreakdownType supports ConnectionTasks
     */
    boolean supportsConnectionTask() {
        return true;
    }

    /**
     * Tests if this BreakdownType supports {@link ComTaskExecution}s.
     * This is only the case when the enum value relates to an
     * attribute of a ComTaskExecution.
     *
     * @return A flag that indicates if this BreakdownType supports ConnectionTasks
     */
    boolean supportsComTaskExecution() {
        return true;
    }

    /**
     * Adds the {@link BreakdownResult} to the {@link BreakdownResultProcessor}
     * using the appropriate add method for this BreakdownType.
     *
     * @param breakdownResult The BreakdownResult
     * @param processor The BreakdownResultProcessor
     */
    abstract void process(BreakdownResult breakdownResult, BreakdownResultProcessor processor);

}