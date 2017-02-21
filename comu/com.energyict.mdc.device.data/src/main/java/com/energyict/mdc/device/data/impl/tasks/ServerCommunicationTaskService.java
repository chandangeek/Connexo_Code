/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.ScheduledComTaskExecutionIdRange;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.Optional;

/**
 * Adds behavior to {@link CommunicationTaskService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:48)
 */
public interface ServerCommunicationTaskService extends CommunicationTaskService {

    /**
     * Tests if the specified {@link ComTaskEnablement} is used
     * by at least one {@link ComTaskExecution}.
     *
     * @param comTaskEnablement The ComTaskEnablement
     * @return A flag that indicates if the ComTaskEnablement is used or not
     */
    public boolean hasComTaskExecutions(ComTaskEnablement comTaskEnablement);

    /**
     * Tests if the specified {@link ComSchedule} is used
     * by at least one {@link ComTaskExecution}.
     *
     * @param comSchedule The ComSchedule
     * @return A flag that indicates if the ComSchedule is used or not
     */
    public boolean hasComTaskExecutions(ComSchedule comSchedule);

    /**
     * Gets the {@link ScheduledComTaskExecutionIdRange range} of IDs
     * of ComTasksExecutions that are using the
     * {@link ComSchedule} with the specified ID.
     *
     * @param comScheduleId The ID of the ComSchedule
     * @return The ScheduledComTaskExecutionIdRange
     */
    public Optional<ScheduledComTaskExecutionIdRange> getScheduledComTaskExecutionIdRange(long comScheduleId);

    /**
     * Obsoletes all ComTasksExecutions that
     * are in the specified {@link ScheduledComTaskExecutionIdRange range},
     * i.e. they are related to the {@link ComSchedule} and their
     * ID is between the min and max id of the range.
     *
     * @param idRange The ScheduledComTaskExecutionIdRange
     */
    public void obsoleteComTaskExecutionsInRange(ScheduledComTaskExecutionIdRange idRange);

    /**
     * Disconnects all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} from the
     * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that relates to the {@link PartialConnectionTask}.
     * Note that this will cause the execution to be put on hold
     * because it will no longer show up in the comserver task query.
     *
     * @param comTask               The ComTask
     * @param deviceConfiguration   The DeviceConfiguration
     * @param partialConnectionTask The PartialConnectionTask
     */
    public void removePreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} that were using
     * the default connection task to the specific
     * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that relates to the {@link PartialConnectionTask}.
     *
     * @param comTask               The ComTask
     * @param deviceConfiguration   The DeviceConfiguration
     * @param partialConnectionTask The PartialConnectionTask
     */
    public void switchFromDefaultConnectionTaskToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}
     * to use the default connection task from now on.
     *
     * @param comTask             The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     */
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}
     * to stop using the default connection task from now on.
     * Note that they will continue to use the one that they are connected to,
     * which coincidently may be the default ConnectionTask of the Device.
     *
     * @param comTask             The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     */
    public void switchOffDefault(ComTask comTask, DeviceConfiguration deviceConfiguration);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}, that were using
     * a specific {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that relates to the {@link PartialConnectionTask},
     * to use the default connection task from now on.
     *
     * @param comTask                       The ComTask
     * @param deviceConfiguration           The DeviceConfiguration
     * @param previousPartialConnectionTask The PartialConnectionTask
     */
    public void switchFromPreferredConnectionTaskToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask);

    /**
     * Updates the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * of all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} that have not overruled
     * the previous preferred connection task.
     *
     * @param comTask                       The ComTask
     * @param deviceConfiguration           The DeviceConfiguration
     * @param previousPartialConnectionTask The previous preferred {@link PartialConnectionTask}
     * @param newPartialConnectionTask      The new preferred PartialConnectionTask
     */
    public void preferredConnectionTaskChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask);

    /**
     * Updates the priority of all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} that have not overruled
     * the previous preferred priority.
     *
     * @param comTask                   The ComTask
     * @param deviceConfiguration       The DeviceConfiguration
     * @param previousPreferredPriority The previous preferred priority
     * @param newPreferredPriority      The new preferred priority
     */
    public void preferredPriorityChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority);

    /**
     * Suspends the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}.
     * Note that the {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
     * that will be suspended will not be marked as onHold
     * (i.e. {@link com.energyict.mdc.device.data.tasks.ComTaskExecution#isOnHold()}
     * will not return <code>true</code>) because the planned next execution
     * timestamp is retained so the tasks can be resumed later.
     *
     * @param comTask             The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     */
    public void suspendAll(ComTask comTask, DeviceConfiguration deviceConfiguration);

    /**
     * Resumes the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} that were suspended before.
     * This restores the planned next execution timestamp.
     *
     * @param comTask             The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     */
    public void resumeAll(ComTask comTask, DeviceConfiguration deviceConfiguration);

}