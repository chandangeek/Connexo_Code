/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.MicroActionTranslationKey;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Supports the life cycle of a {@link Device} as defined by the
 * {@link DeviceLifeCycle} of the Device's {@link DeviceType type}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (15:54)
 */
@ProviderType
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
    enum MicroActionPropertyName {
        LAST_CHECKED(Keys.LAST_CHECKED_TIMESTAMP_PROPERTY_NAME),
        MULTIPLIER(MicroActionTranslationKey.Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.SET_MULTIPLIER),
        USAGE_POINT(MicroActionTranslationKey.Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.LINK_TO_USAGE_POINT),
        ;

        private String key;

        MicroActionPropertyName(String key) {
            this.key = key;
        }

        public String key() {
            return this.key;
        }

        public static class Keys {
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
    List<ExecutableAction> getExecutableActions(Device device);

    /**
     * Gets the {@link ExecutableAction} for the current user
     * against the specified {@link Device} that relates to
     * the specified {@link StateTransitionEventType}.
     *
     * @param device    The Device
     * @param eventType The StateTransitionEventType
     * @return The ExecutableAction
     */
    Optional<ExecutableAction> getExecutableActions(Device device, StateTransitionEventType eventType);

    /**
     * Gets the {@link PropertySpec}s for the specified {@link MicroAction}.
     * Note that all required PropertySpecs must be specified when executing
     * an {@link AuthorizedTransitionAction} that is configured to
     * execute the MicroAction when it is executed.
     * Note that the name of each PropertySpec will be one provided
     * by the MicroActionPropertyName enum class.
     *
     * @param action The MicroAction
     * @return The List of PropertySpec
     * @see #execute(AuthorizedTransitionAction, Device, Instant, List)
     */
    List<PropertySpec> getPropertySpecsFor(MicroAction action);

    /**
     * Creates an ExecutableActionProperty with the specified value
     * for the specified {@link PropertySpec}.
     * Validates that the specified value is compatible
     * the PropertySpec and will throw an {@link InvalidValueException}
     * when that is not the case.
     *
     * @param value        The value
     * @param propertySpec The PropertySpec
     * @return The ExecutableActionProperty
     * @throws InvalidValueException Thrown when the value is not compatible with the PropertySpec
     */
    ExecutableActionProperty toExecutableActionProperty(Object value, PropertySpec propertySpec) throws InvalidValueException;

    /**
     * Executes the {@link AuthorizedTransitionAction} on the specified {@link Device}
     * and throws a SecurityException if the current user is not allowed
     * to execute it according to the security levels
     * that are configured on the action.
     * Remember that:
     * <ul>
     * <li>Only the system is allowed to execute the action when no levels are configured</li>
     * <li>A value must be specified for all required {@link PropertySpec}s of all the
     * {@link MicroAction}s that are configured on the AuthorizedTransitionAction</li>
     * </ul>
     *
     * @param action             The AuthorizedTransitionAction
     * @param device             The Device
     * @param effectiveTimestamp The point in time when this transition will become effective, i.e. when the resulting state change will become effective
     * @param properties         The properties for all the MicroAction that are configured on the AuthorizedTransitionAction  @see AuthorizedTransitionAction#getLevels()
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     * @see AuthorizedTransitionAction#getActions()
     * @see DeviceLifeCycleService#getPropertySpecsFor(MicroAction)
     * @see DeviceLifeCycleService#toExecutableActionProperty(Object, PropertySpec)
     */
    void execute(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) throws
            SecurityException,
            DeviceLifeCycleActionViolationException;

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
     * @param action             The AuthorizedBusinessProcessAction
     * @param device             The Device
     * @param effectiveTimestamp The point in time when this transition will become effective, i.e. when the resulting state change will become effective
     * @throws SecurityException Thrown when the current user is not allowed to execute this action
     * @see AuthorizedBusinessProcessAction#getLevels()
     */
    void execute(AuthorizedBusinessProcessAction action, Device device, Instant effectiveTimestamp) throws SecurityException, DeviceLifeCycleActionViolationException;

    /**
     * Triggers a new {@link StateTransitionTriggerEvent} for the
     * {@link CustomStateTransitionEventType event type} and the {@link Device}.
     *
     * @param eventType          The CustomStateTransitionEventType
     * @param device             The Device
     * @param effectiveTimestamp The point in time when the resulting state change should become effective
     */
    void triggerEvent(CustomStateTransitionEventType eventType, Device device, Instant effectiveTimestamp);

    String getKey(DefaultMicroCheck microCheck);

    String getName(DefaultMicroCheck microCheck);

    String getDescription(DefaultMicroCheck microCheck);

    String getCategoryName(DefaultMicroCheck microCheck);

    String getName(MicroAction microAction);

    String getDescription(MicroAction microAction);

    String getCategoryName(MicroAction microAction);
}
