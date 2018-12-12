/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (10:38)
 */
public interface ConnectionTaskReportService {

    /**
     * Counts all {@link ConnectionTask}s in the system,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @return The numbers, broken down by TaskStatus
     */
    Map<TaskStatus, Long> getConnectionTaskStatusCount();

    /**
     * Counts all {@link ConnectionTask}s that relate to
     * {@link com.energyict.mdc.device.data.Device}s
     * that are part of the specified {@link EndDeviceGroup},
     * grouping them by their respective {@link TaskStatus}.
     *
     * @return The numbers, broken down by TaskStatus
     */
    Map<TaskStatus, Long> getConnectionTaskStatusCount(EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ConnectionTask}s whose current status is
     * in the Set of {@link TaskStatus} grouping them by
     * {@link com.energyict.mdc.engine.config.ComPortPool}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by ComPortPool and TaskStatus
     */
    Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Counts all {@link ConnectionTask}s that relate to
     * {@link com.energyict.mdc.device.data.Device}s
     * that are part of the specified {@link EndDeviceGroup}
     * and whose current status is in the Set of {@link TaskStatus}
     * grouping them by {@link ComPortPool}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by ComPortPool and TaskStatus
     */
    Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ConnectionTask}s whose current status is
     * in the Set of {@link TaskStatus} grouping them by
     * {@link com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Counts all {@link ConnectionTask}s that relate to
     * {@link com.energyict.mdc.device.data.Device}s
     * that are part of the specified {@link EndDeviceGroup}
     * and whose current status is in the Set of {@link TaskStatus}
     * grouping them by {@link com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ConnectionTask}s whose current status is
     * in the Set of {@link TaskStatus} grouping them by
     * {@link DeviceType}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Counts all {@link ConnectionTask}s that relate to
     * {@link com.energyict.mdc.device.data.Device}s
     * that are part of the specified {@link EndDeviceGroup}
     * and whose current status is in the Set of {@link TaskStatus}
     * grouping them by {@link DeviceType}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup);

    /**
     * Counts the last {@link ComSession} of all {@link ConnectionTask}s,
     * grouping them by their respective {@link ConnectionTask.SuccessIndicator}.
     *
     * @return The numbers, broken down by SuccessIndicator
     */
    Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount();

    /**
     * Counts the last {@link ComSession} of all {@link ConnectionTask}s
     * that relate to device of the specified {@link EndDeviceGroup},
     * grouping them by their respective {@link ConnectionTask.SuccessIndicator}.
     *
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The numbers, broken down by SuccessIndicator
     */
    Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount(EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ConnectionTask}s grouping them by their
     * respective {@link ConnectionTypePluggableClass} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @return The counters
     */
    Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap();

    /**
     * Counts all {@link ConnectionTask}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup}, grouping them by their
     * respective {@link ConnectionTypePluggableClass} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @param deviceGroup The EndDeviceGroup
     * @return The counters
     */
    Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap(EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ConnectionTask}s grouping them by their
     * respective {@link DeviceType} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @return The counters
     */
    Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap();

    /**
     * Counts all {@link ConnectionTask}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup}, grouping them by their
     * respective {@link DeviceType} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The counters
     */
    Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ConnectionTask}s grouping them by their
     * respective {@link ComPortPool} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @return The counters
     */
    Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap();

    /**
     * Counts all {@link ConnectionTask}s that relate to the {@link Device}s
     * in the specified {@link EndDeviceGroup}, grouping them by their
     * respective {@link ComPortPool} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The counters
     */
    Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup);

    /**
     * Counts the number of {@link ConnectionTask}s whose last {@link ComSession}
     * completed successfully but has at least one failing task.
     * Note that the status of the task is not taken into account at all.
     *
     * @return The count
     */
    long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

    /**
     * Counts the number of {@link ConnectionTask}s that are currently waiting
     * to be executed and whose last {@link ComSession}
     * completed successfully but has at least one failing task.
     *
     * @return The count
     */
    long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

    /**
     * Counts the number of {@link ConnectionTask}s in the specified {@link EndDeviceGroup},
     * whose last {@link ComSession}
     * completed successfully but has at least one failing task.
     * Note that the status of the task is not taken into account at all.
     *
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The count
     */
    long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(EndDeviceGroup deviceGroup);

    /**
     * Counts the number of {@link ConnectionTask}s in the specified {@link EndDeviceGroup},
     * that are currently waiting
     * to be executed and whose last {@link ComSession}
     * completed successfully but has at least one failing task.
     *
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The count
     */
    long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask(EndDeviceGroup deviceGroup);

    /**
     * Calculates the {@link ConnectionTaskBreakdowns}
     * for all {@link ConnectionTask}s in the system.
     *
     * @return The CommunicationTaskBreakdowns
     */
    ConnectionTaskBreakdowns getConnectionTaskBreakdowns();

    /**
     * Calculates the {@link ConnectionTaskBreakdowns}
     * for all {@link ConnectionTask}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup}.
     *
     * @param deviceGroup The EndDeviceGroup
     * @return The CommunicationTaskBreakdowns
     */
    ConnectionTaskBreakdowns getConnectionTaskBreakdowns(EndDeviceGroup deviceGroup);

}