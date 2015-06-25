package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import java.time.Instant;
import java.util.List;

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
     * with the specified List of {@link ExecutableActionProperty}
     * and throws a SecurityException if the current user is not allowed
     * to execute it according to the security levels
     * that are configured on the action.
     *
     * @param effectiveTimestamp The point in time when this transition will become effective, i.e. when the resulting state change will become effective
     * @param properties The properties
     * @see AuthorizedAction#getLevels()
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     * @see DeviceLifeCycleService#execute(AuthorizedTransitionAction, Device, Instant, List)
     */
    public void execute(Instant effectiveTimestamp, List<ExecutableActionProperty> properties) throws SecurityException, DeviceLifeCycleActionViolationException;

}