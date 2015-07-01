package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Enables the execution of a {@link com.energyict.mdc.tasks.ComTask} against Devices
 * of a {@link com.energyict.mdc.device.config.DeviceConfiguration} and specifies the security
 * requirements for that execution.
 * In addition, specifies preferred scheduling (e.g. every day or every week)
 * and preferred {@link com.energyict.mdc.device.config.PartialConnectionTask} or if the execution of the ComTask
 * should use the default ConnectionTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-21 (11:30)
 */
@ProviderType
public interface ComTaskEnablement extends HasId {

    public static final int HIGHEST_PRIORITY = TaskPriorityConstants.HIGHEST_PRIORITY;
    public static final int LOWEST_PRIORITY = TaskPriorityConstants.LOWEST_PRIORITY;
    public static final int DEFAULT_PRIORITY = TaskPriorityConstants.DEFAULT_PRIORITY;

    public DeviceConfiguration getDeviceConfiguration ();

    public ComTask getComTask ();

    public SecurityPropertySet getSecurityPropertySet ();

    public void  setSecurityPropertySet (SecurityPropertySet securityPropertySet);

    /**
     * Gets the flag that indicates if the ComTaskExecution
     * should ignore the {@link com.energyict.mdc.scheduling.NextExecutionSpecs} and therefore
     * always execute in an inbound context.
     *
     * @return The flag that indicates if the NextExecutionSpecs should be ignored in an inbound context
     */
    public boolean isIgnoreNextExecutionSpecsForInbound ();

    public void setIgnoreNextExecutionSpecsForInbound (boolean flag);

    /**
     * Gets the preferred execution priority for the execution
     * of the related {@link ComTask} on a Device
     * of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The preferred priority
     */
    public int getPriority ();

    public void setPriority(int priority);

    /**
     * Tests if this ComTaskEnablement is suspended.
     * Suspending a ComTaskEnablement will set
     * all ComTaskExecution on hold.
     *
     * @return A flag that indicates if this ComTaskEnablement is suspended
     */
    public boolean isSuspended ();

    /**
     * Suspends this ComTaskEnablement , i.e. temporarily
     * disables the execution of the related {@link ComTask}
     * on all Devices of the related
     * {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     * Note that this only works on existing ComTaskEnablements, i.e.
     */
    public void suspend ();

    /**
     * Resumes this ComTaskEnablement , i.e. reverts any previous suspension.
     */
    public void resume ();

    /**
     * Tests if the execution of the related {@link ComTask}
     * on a Device of the related
     * {@link com.energyict.mdc.device.config.DeviceConfiguration} should use the default
     * ConnectionTask configured on that Device.
     *
     * @return <code>true</code> if the related ComTask should use the default ConnectionTask
     *         when executed against a Device, <code>false</code> otherwise.
     */
    public boolean usesDefaultConnectionTask();

    /**
     * Sets the flag that indicates if the execution of the related {@link ComTask}
     * on a Device of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * should use the default ConnectionTask configured on that Device.
     *
     * @param flagValue The flag, <code>true</code> indicates that the default connection task should be used
     */
    public void useDefaultConnectionTask(boolean flagValue);

    public boolean hasPartialConnectionTask();

    /**
     * Gets the {@link com.energyict.mdc.device.config.PartialConnectionTask} that specifies
     * the preferred way to setup a connection for the execution
     * of the related {@link ComTask} for all Devices
     * of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     *
     * @return The PartialConnectionTask
     */
    public Optional<PartialConnectionTask> getPartialConnectionTask();

    public void setPartialConnectionTask(PartialConnectionTask partialConnectionTask);


    /**
     * Gets the ProtocolDialectConfigurationProperties.
     *
     * @return the ProtocolDialectConfigurationProperties
     */
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    public void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

    /**
     * Saves the changes applied to this ComTaskEnablement.
     * Note that it is not necessary to save this ComTaskEnablement
     * when it was first created, as soon as it was completed by the
     * building process by adding it to the configuration,
     * it was saved on your behalf.
     *
     * @see ComTaskEnablementBuilder#add()
     */
    public void save();

}