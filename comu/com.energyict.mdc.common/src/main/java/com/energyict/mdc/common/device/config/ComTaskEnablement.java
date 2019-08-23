/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.TaskPriorityConstants;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

/**
 * Enables the execution of a {@link ComTask} against Devices
 * of a {@link DeviceConfiguration} and specifies the security
 * requirements for that execution.
 * In addition, specifies preferred scheduling (e.g. every day or every week)
 * and preferred {@link PartialConnectionTask} or if the execution of the ComTask
 * should use the default ConnectionTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-21 (11:30)
 */
@ConsumerType
public interface ComTaskEnablement extends HasId {

    int HIGHEST_PRIORITY = TaskPriorityConstants.HIGHEST_PRIORITY;
    int LOWEST_PRIORITY = TaskPriorityConstants.LOWEST_PRIORITY;
    int DEFAULT_PRIORITY = TaskPriorityConstants.DEFAULT_PRIORITY;

    DeviceConfiguration getDeviceConfiguration ();

    ComTask getComTask ();

    SecurityPropertySet getSecurityPropertySet ();

    void  setSecurityPropertySet (SecurityPropertySet securityPropertySet);

    /**
     * Gets the flag that indicates if the ComTaskExecution
     * should ignore the {@link NextExecutionSpecs} and therefore
     * always execute in an inbound context.
     *
     * @return The flag that indicates if the NextExecutionSpecs should be ignored in an inbound context
     */
    boolean isIgnoreNextExecutionSpecsForInbound ();

    void setIgnoreNextExecutionSpecsForInbound (boolean flag);

    /**
     * Gets the preferred execution priority for the execution
     * of the related {@link ComTask} on a Device
     * of the related {@link DeviceConfiguration}.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The preferred priority
     */
    int getPriority ();

    void setPriority(int priority);

    /**
     * Tests if this ComTaskEnablement is suspended.
     * Suspending a ComTaskEnablement will set
     * all ComTaskExecution on hold.
     *
     * @return A flag that indicates if this ComTaskEnablement is suspended
     */
    boolean isSuspended ();

    /**
     * Suspends this ComTaskEnablement , i.e. temporarily
     * disables the execution of the related {@link ComTask}
     * on all Devices of the related
     * {@link DeviceConfiguration}.
     * Note that this only works on existing ComTaskEnablements, i.e.
     */
    void suspend ();

    /**
     * Resumes this ComTaskEnablement , i.e. reverts any previous suspension.
     */
    void resume ();

    /**
     * Tests if the execution of the related {@link ComTask}
     * on a Device of the related
     * {@link DeviceConfiguration} should use the default
     * ConnectionTask configured on that Device.
     *
     * @return <code>true</code> if the related ComTask should use the default ConnectionTask
     *         when executed against a Device, <code>false</code> otherwise.
     */
    boolean usesDefaultConnectionTask();

    /**
     * Sets the flag that indicates if the execution of the related {@link ComTask}
     * on a Device of the related {@link DeviceConfiguration}
     * should use the default ConnectionTask configured on that Device.
     *
     * @param flagValue The flag, <code>true</code> indicates that the default connection task should be used
     */
    void useDefaultConnectionTask(boolean flagValue);

    boolean hasPartialConnectionTask();

    /**
     * Gets the {@link PartialConnectionTask} that specifies
     * the preferred way to setup a connection for the execution
     * of the related {@link ComTask} for all Devices
     * of the related {@link DeviceConfiguration}.
     *
     * @return The PartialConnectionTask
     */
    Optional<PartialConnectionTask> getPartialConnectionTask();

    void setPartialConnectionTask(PartialConnectionTask partialConnectionTask);

    /**
     * Gets an optional containing the {@link ConnectionFunction} for this {@link ComTaskEnablement}
     * or else an empty optional in case no function is provided
     *
     * @return the {@link ConnectionFunction}
     */
    Optional<ConnectionFunction> getConnectionFunction();

    void setConnectionFunction(ConnectionFunction connectionFunction);

    /**
     * Saves the changes applied to this ComTaskEnablement.
     * Note that it is not necessary to save this ComTaskEnablement
     * when it was first created, as soon as it was completed by the
     * building process by adding it to the configuration,
     * it was saved on your behalf.
     *
     * @see ComTaskEnablementBuilder#add()
     */
    void save();

    long getVersion();

    int getMaxNumberOfTries ();

    void setMaxNumberOfTries(int maxNumberOfTries);
}