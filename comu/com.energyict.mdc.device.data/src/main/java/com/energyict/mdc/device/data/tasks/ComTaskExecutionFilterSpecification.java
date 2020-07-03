/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.CompletionCode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Models the filtering that can be applied by client code to count
 * or find {@link ComTaskExecution}s
 * by a number of criteria that can be mixed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-22 (10:52)
 */
public class ComTaskExecutionFilterSpecification {

    /**
     * The Set of {@link ComTask} or an empty set
     * if you want all ComTasks that are configured in the system.
     */
    public Set<ComTask> comTasks = new HashSet<>();

    /**
     * The Set of {@link ComSchedule} or an empty set
     * if you want all ComSchedules that are configured in the system.
     */
    public Set<ComSchedule> comSchedules = new HashSet<>();

    /**
     * The Set of {@link DeviceType} or an empty set
     * if you want all DeviceType that are configured in the system.
     */
    public Set<DeviceType> deviceTypes = new HashSet<>();

    /**
     * The Set of {@link TaskStatus}es.
     */
    public Set<TaskStatus> taskStatuses = EnumSet.noneOf(TaskStatus.class);

    /**
     * The Set of {@link CompletionCode}s.
     * Note that using <strong>ALL</strong> enum values is equal to using none.
     * The set supports null element; it means the case where communication task has never started and has no latestResult.
     */
    public Set<CompletionCode> latestResults = new HashSet<>();

    /**
     * The Interval in which the start time of the last session is expected
     * or <code>null</code> if the counter of the filter should not
     * take this into account.
     */
    public Interval lastSessionStart = null;

    /**
     * The Interval in which the end time of the last session is expected
     * or <code>null</code> if the counter of the filter should not
     * take this into account.
     */
    public Interval lastSessionEnd = null;

    /**
     * The {@link com.elster.jupiter.metering.groups.EndDeviceGroup}s that contain the {@link Device}s
     * to which the matching {@link ComTaskExecution}s should be linked
     * or an empty set if the filter should not take this into account.
     */
    public Set<EndDeviceGroup> deviceGroups = new HashSet<>();

    /**
     * The Set of device states
     * Comtasks of devices in such states will be excluded from the result
     * Default value: exclude comtasks of "In stock" and "Decommissioned" devices
     */
    public Set<String> restrictedDeviceStates = new HashSet<>();

    /**
     * The device(name) you want to filter on
     */
    public String deviceName;

    /**
     * The Set of {@link ConnectionTypePluggableClass} or an empty set
     * if you want all ConnectionTypePluggableClasses that are configured in the system.
     */
    public Set<ConnectionTypePluggableClass> connectionTypes = new HashSet<>();

    /**
     * The Set of {@link ConnectionTask} or an empty set
     * if you want all ConnectionTasks
     */
    public List<Long> connectionMethods = new ArrayList<>();

    /**
     * The Set of device stages
     * Comtasks of devices in such stages will be excluded from the result
     * Default value: exclude comtasks of "Pre-operational" and "Post-operational" devices
     */
    public Set<String> restrictedDeviceStages = new HashSet<>();

    public Long locationId;

    public boolean showSlaveComTaskExecutions;
}