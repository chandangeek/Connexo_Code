package com.energyict.mdc.device.config;

import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

/**
 * Provides building services to enable {@link com.energyict.mdc.tasks.ComTask}
 * in a {@link com.energyict.mdc.device.config.DeviceConfiguration}.
 * Every method will return the builder on which it was invoked
 * to support method chaining while building.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-22 (09:48)
 */
@ProviderType
public interface ComTaskEnablementBuilder {

    public ComTaskEnablementBuilder setIgnoreNextExecutionSpecsForInbound (boolean flag);

    /**
     * Sets the {@link com.energyict.mdc.device.config.PartialConnectionTask} that specifies
     * the preferred way to setup a connection for the enabled {@link ComTask}.
     * Note that when you set this, you cannot specify to use the default ConnectionTask.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#setPartialConnectionTask(PartialConnectionTask)
     */
    public ComTaskEnablementBuilder setPartialConnectionTask(PartialConnectionTask partialConnectionTask);

    public ComTaskEnablementBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

    /**
     * Sets the flag that indicates if the execution of the related {@link ComTask}
     * on a Device of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * should use the default ConnectionTask configured on that Device.
     *
     * @param flagValue The flag, <code>true</code> indicates that the default connection task should be used
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#useDefaultConnectionTask(boolean)
     */
    public ComTaskEnablementBuilder useDefaultConnectionTask(boolean flagValue);

    /**
     * Gets the preferred execution priority for the execution
     * of the related {@link ComTask} on a Device
     * of the related {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#setPriority(int)
     */
    public ComTaskEnablementBuilder setPriority(int priority);

    /**
     * Completes the building process, i.e. enables the {@link com.energyict.mdc.tasks.ComTask}
     * on the {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     *
     * @return The ComTaskEnablement
     */
    public ComTaskEnablement add();
}