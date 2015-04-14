package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventImpl;

import java.util.List;
import java.util.Map;

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
     * Gets the {@link ExecutableAction}s for the current user
     * against the specified {@link Device}.
     *
     * @param device The Device
     * @return The List of ExecutableAction
     */
    public List<ExecutableAction> getExecutableActions(Device device);

    /**
     * Executes the {@link AuthorizedAction} on the specified {@link Device}
     * and throws a SecurityException if the current user is not allowed
     * to execute it according to the security levels
     * that are configured on the action. Remember that when no levels
     * are configured then only the system is allowed to execute the action.
     *
     * @see AuthorizedAction#getLevels()
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     */
    public void execute(AuthorizedAction action, Device device) throws SecurityException, DeviceLifeCycleActionViolationException;

    /**
     * Triggers a new {@link CustomStateTransitionEventType} for the
     * {@link CustomStateTransitionEventType event type} and the {@link Device}.
     *
     * @param eventType The CustomStateTransitionEventType
     * @param device The Device
     */
    public void triggerEvent(CustomStateTransitionEventType eventType, Device device);

}