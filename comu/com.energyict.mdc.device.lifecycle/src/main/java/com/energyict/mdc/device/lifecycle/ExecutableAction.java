package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

/**
 * Wraps an {@link AuthorizedAction} as being executable
 * on a specific {@link Device} given the privileges of the current user.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (15:40)
 */
public interface ExecutableAction {

    public AuthorizedAction getAction();

    public Device getDevice();

    /**
     * Executes the {@link AuthorizedAction} on the {@link Device}
     * and throws a SecurityException if the current user is not allowed
     * to execute it according to the security levels
     * that are configured on the action.
     *
     * @see AuthorizedAction#getLevels()
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     * @see DeviceLifeCycleService#execute(AuthorizedAction, Device)
     */
    public void execute() throws SecurityException, DeviceLifeCycleActionViolationException;

}