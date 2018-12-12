/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

/**
 * Indicates that the {@link DeviceCommandExecutor} is ready to accept the execution
 * of a {@link DeviceCommand}, i.e. it has reserved the necessary system resources
 * to be able to execute the DeviceCommand when it is ready and
 * the DeviceCommandExecutionToken will be passed along.
 * The DeviceCommandExecutor will verify that the token was indeed previously
 * returned by the prepareExecution method and if that is not the case
 * then the execution of the DeviceCommand is refused.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-21 (08:56)
 */
public interface DeviceCommandExecutionToken {
}