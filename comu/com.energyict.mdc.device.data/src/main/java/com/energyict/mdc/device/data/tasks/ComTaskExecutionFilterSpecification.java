package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.util.time.Interval;

import java.util.EnumSet;
import java.util.HashSet;
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
     */
    public Set<CompletionCode> latestResults = EnumSet.noneOf(CompletionCode.class);

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

}