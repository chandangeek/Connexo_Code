package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedStandardTransitionAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

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
     * Models the names of the different properties
     * that are used by the {@link MicroAction}s.
     * The key of the enum value can be used as
     * a unique identifier by external components
     * that integrate with the platform and will
     * also be used as a translation key.
     */
    public enum MicroActionPropertyName {
        /**
         * The timestamp on which the action will be affective.
         * As an example, the action that activates a device
         * will use the effective timestamp as the timestamp
         * on which the device is considered "active", which
         * is called the activation date.
         */
        EFFECTIVE_TIMESTAMP(Keys.EFFECTIVE_TIMESTAMP_PROPERTY_NAME),
        LAST_CHECKED(Keys.LAST_CHECKED_TIMESTAMP_PROPERTY_NAME);

        private String key;
        MicroActionPropertyName(String key) {
            this.key = key;
        }

        public String key() {
            return this.key;
        }

        public static class Keys {
            public static final String EFFECTIVE_TIMESTAMP_PROPERTY_NAME = "mdc.device.lifecycle.micro.action.effective.timestamp";
            public static final String LAST_CHECKED_TIMESTAMP_PROPERTY_NAME = "mdc.device.lifecycle.micro.action.lastChecked.timestamp";
        }
    }

    /**
     * Gets the {@link ExecutableAction}s for the current user
     * against the specified {@link Device}.
     *
     * @param device The Device
     * @return The List of ExecutableAction
     */
    public List<ExecutableAction> getExecutableActions(Device device);

    /**
     * Gets the {@link PropertySpec}s for the specified {@link MicroAction}.
     * Note that all required PropertySpecs must be specified when executing
     * an {@link AuthorizedStandardTransitionAction} that is configured to
     * execute the MicroAction when it is executed.
     * Note that the name of each PropertySpec will be one provided
     * by the MicroActionPropertyName enum class.
     *
     * @param action The MicroAction
     * @return The List of PropertySpec
     * @see #execute(AuthorizedTransitionAction, Device, List)
     */
    public List<PropertySpec> getPropertySpecsFor(MicroAction action);

    /**
     * Executes the {@link AuthorizedTransitionAction} on the specified {@link Device}
     * and throws a SecurityException if the current user is not allowed
     * to execute it according to the security levels
     * that are configured on the action.
     * Remember that:
     * <ul>
     * <li>Only the system is allowed to execute the action when no levels are configured</li>
     * <li>A value must be specified for all required {@link PropertySpec}s of all the
     *     {@link MicroAction}s that are configured on the AuthorizedStandardTransitionAction</li>
     * </ul>
     *
     * @param action The AuthorizedTransitionAction
     * @param device The Device
     * @param properties The properties for all the MicroAction that are configured on the AuthorizedStandardTransitionAction
     * @see AuthorizedTransitionAction#getLevels()
     * @see AuthorizedTransitionAction#getActions()
     * @see DeviceLifeCycleService#getPropertySpecsFor(MicroAction)
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     */
    public void execute(AuthorizedTransitionAction action, Device device, List<ExecutableActionProperty> properties) throws SecurityException, DeviceLifeCycleActionViolationException;

    /**
     * Executes the {@link AuthorizedBusinessProcessAction} on the specified {@link Device}
     * and throws a SecurityException if the current user is not allowed
     * to execute it according to the security levels
     * that are configured on the action.
     * Remember that:
     * <ul>
     * <li>Only the system is allowed to execute the action when no levels are configured</li>
     * </ul>
     *
     * @see AuthorizedBusinessProcessAction#getLevels()
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     */
    public void execute(AuthorizedBusinessProcessAction action, Device device) throws SecurityException, DeviceLifeCycleActionViolationException;

    /**
     * Triggers a new {@link CustomStateTransitionEventType} for the
     * {@link CustomStateTransitionEventType event type} and the {@link Device}.
     *
     * @param eventType The CustomStateTransitionEventType
     * @param device The Device
     */
    public void triggerEvent(CustomStateTransitionEventType eventType, Device device);

}