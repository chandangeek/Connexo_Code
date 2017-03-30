/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides reporting services that relate to {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-30 (09:04)
 */
public interface CommunicationTaskReportService {

    /**
     * Counts all {@link ComTaskExecution}s in the system,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @return The numbers, broken down by TaskStatus
     */
    Map<TaskStatus, Long> getComTaskExecutionStatusCount();

    /**
     * Counts all {@link ComTaskExecution}s that relate to
     * {@link Device}s in the specified {@link EndDeviceGroup},
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The numbers, broken down by TaskStatus
     */
    Map<TaskStatus, Long> getComTaskExecutionStatusCount(EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ComTaskExecution}s that match the specified filter,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param filter The ComTaskExecutionFilterSpecification
     * @return The numbers, broken down by TaskStatus
     */
    Map<TaskStatus, Long> getComTaskExecutionStatusCount(ComTaskExecutionFilterSpecification filter);

    /**
     * Counts all {@link ComTaskExecution}s for {@link ComSchedule}s,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by ComSchedule and TaskStatus
     */
    Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Counts all {@link ComTaskExecution}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup} for {@link ComSchedule}s,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The numbers, broken down by ComSchedule and TaskStatus
     */
    Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ComTaskExecution}s,
     * grouping them by the {@link com.energyict.mdc.device.config.DeviceType}
     * of the related {@link Device}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Counts all {@link ComTaskExecution}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup},
     * grouping them by the {@link DeviceType}
     * of the related Device.
     *
     * @param taskStatuses The Set of TaskStatus
     * @param deviceGroup The QueryEndDeviceGroup
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup);

    /**
     * Calculates the {@link CommunicationTaskBreakdowns}
     * for all {@link ComTaskExecution}s in the system.
     *
     * @return The CommunicationTaskBreakdowns
     */
    CommunicationTaskBreakdowns getCommunicationTaskBreakdowns();

    /**
     * Calculates the {@link CommunicationTaskBreakdowns}
     * for all {@link ComTaskExecution}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup}.
     *
     * @param deviceGroup The EndDeviceGroup
     * @return The CommunicationTaskBreakdowns
     */
    CommunicationTaskBreakdowns getCommunicationTaskBreakdowns(EndDeviceGroup deviceGroup);

    /**
     * Counts the last {@link ComSession} of all {@link ComTaskExecution}s,
     * grouping them by their respective highest priority {@link CompletionCode}.
     *
     * @return The numbers, broken down by SuccessIndicator
     */
    Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount();

    /**
     * Counts the last {@link ComSession} of all {@link ComTaskExecution}s
     * that relate to {@link Device}s in the specified {@link EndDeviceGroup},
     * grouping them by their respective highest priority {@link CompletionCode}.
     *
     * @param deviceGroup The EndDeviceGroup
     * @return The numbers, broken down by SuccessIndicator
     */
    Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount(EndDeviceGroup deviceGroup);

    /**
     * Counts all {@link ComTaskExecution}s grouping them by their
     * respective {@link DeviceType} and the {@link CompletionCode}
     * of the task's last {@link ComTaskExecutionSession session}.
     * The counters are returned in the order of CompletionCode.
     *
     * @return The counters
     */
    Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap();

    /**
     * Counts all {@link ComTaskExecution}s that relate to {@link Device}s
     * in the specified {@link EndDeviceGroup},
     * grouping them by the {@link DeviceType} and the {@link CompletionCode}
     * of the task's last {@link ComTaskExecutionSession session}.
     * The counters are returned in the order of CompletionCode.
     *
     * @return The counters
     */
    Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap(EndDeviceGroup deviceGroup);

}