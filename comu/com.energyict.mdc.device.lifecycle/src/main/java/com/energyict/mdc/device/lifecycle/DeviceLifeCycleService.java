package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

/**
 * Supports the life cycle of a {@link Device} as defined by the
 * {@link DeviceLifeCycle} of the Device's {@link DeviceType type}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (15:54)
 */
public interface DeviceLifeCycleService {

    String COMPONENT_NAME = "DLC";

    /**
     * Triggers the execution of the {@link AuthorizedAction}
     * on the specified {@link Device} and throws
     * a SecurityException if the current user is not
     * allowed to execute it according to the security
     * levels that are configured on the action.
     * Remember that when no levels are configured
     * then only the system is allowed to execute the action.
     *
     * @see AuthorizedAction#getLevels()
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     */
    public void triggerExecution(AuthorizedAction action, Device device) throws SecurityException, DeviceLifeCycleActionViolationException;

}