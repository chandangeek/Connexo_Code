/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import aQute.bnd.annotation.ConsumerType;

/**
 * Provides building services to enable {@link ComTask}
 * in a {@link DeviceConfiguration}.
 * Every method will return the builder on which it was invoked
 * to support method chaining while building.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-22 (09:48)
 */
@ConsumerType
public interface ComTaskEnablementBuilder {

    ComTaskEnablementBuilder setIgnoreNextExecutionSpecsForInbound(boolean flag);

    /**
     * Sets the {@link PartialConnectionTask} that specifies
     * the preferred way to setup a connection for the enabled {@link ComTask}.<br/>
     * Note that when you set this, you cannot specify to use the default ConnectionTask
     * nor to rely on connection function.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#setPartialConnectionTask(PartialConnectionTask)
     */
    ComTaskEnablementBuilder setPartialConnectionTask(PartialConnectionTask partialConnectionTask);

    /**
     * Sets the flag that indicates if the execution of the related {@link ComTask}
     * on a Device of the related {@link DeviceConfiguration}
     * should use the default ConnectionTask configured on that Device.<br/>
     * Note that when you set this, you cannot specify to use a specific partial connection task
     * nor to rely on connection function.
     *
     * @param flagValue The flag, <code>true</code> indicates that the default connection task should be used
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#useDefaultConnectionTask(boolean)
     */
    ComTaskEnablementBuilder useDefaultConnectionTask(boolean flagValue);

    /**
     * Gets the preferred execution priority for the execution
     * of the related {@link ComTask} on a Device
     * of the related {@link DeviceConfiguration}.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#setPriority(int)
     */
    ComTaskEnablementBuilder setPriority(int priority);

    /**
     * Sets the {@link ConnectionFunction} of the {@link ComTaskEnablement}<br/>
     * Note that when you set this, you cannot specify to use a specific partial connection task
     * nor to use the default connection task.
     *
     * @param connectionFunction the ConnectionFunction
     * @return The ComTaskEnablementBuilder
     * @see ComTaskEnablement#setConnectionFunction(ConnectionFunction)
     */
    ComTaskEnablementBuilder setConnectionFunction(ConnectionFunction connectionFunction);

    ComTaskEnablementBuilder setMaxNumberOfTries(int maxNumberOfTries);
    /**
     * Completes the building process, i.e. enables the {@link ComTask}
     * on the {@link DeviceConfiguration}.
     *
     * @return The ComTaskEnablement
     *
     *
     */
    ComTaskEnablement add();
}