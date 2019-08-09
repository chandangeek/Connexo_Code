/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.device.lifecycle.config;

import java.util.Optional;

/**
 * Models a number of tiny actions that will be executed by the
 * device life cycle engine as part of an {@link AuthorizedAction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (10:04)
 */
public enum MicroAction {

    /**
     * Sets the last reading timestamp on all
     * load profiles and log books of the device.
     * Requires that the user specifies that last reading timestamp.
     */
    SET_LAST_READING(MicroCategory.COMMUNICATION),

    /**
     * Enables data validation on the device.
     * Requires that the user specifies the timestamp
     * from which data should be validated.
     */
    ENABLE_VALIDATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_validation"),

    /**
     * Disables data validation on the device.
     */
    DISABLE_VALIDATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_validation"),

    /**
     * Activates all connection tasks on the device.
     */
    ACTIVATE_CONNECTION_TASKS_IN_USE(MicroCategory.COMMUNICATION),

    /**
     * Starts the communication on the device
     * by activating all connection and schedule all
     * communication tasks to execute now.
     *
     * @see #ACTIVATE_CONNECTION_TASKS_IN_USE
     */
    START_COMMUNICATION(MicroCategory.COMMUNICATION),

    /**
     * Disable communication on the device
     * by putting all connection and communication tasks on hold.
     */
    DISABLE_COMMUNICATION(MicroCategory.COMMUNICATION),

    /**
     * Closes the current meter activation on the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should end.
     */
    CLOSE_METER_ACTIVATION(MicroCategory.DATA_COLLECTION),

    /**
     * Removes the device from all enumerated device groups
     * it is contained in.
     */
    REMOVE_DEVICE_FROM_STATIC_GROUPS(MicroCategory.DATA_COLLECTION),

    /**
     * Detaches a slave device from its physical gateway.
     */
    DETACH_SLAVE_FROM_MASTER(MicroCategory.TOPOLOGY),

    /**
     * Enables data estimation on the device.
     */
    ENABLE_ESTIMATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_estimation"),

    /**
     * Disables data estimation on the device.
     */
    DISABLE_ESTIMATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_estimation"),

    /**
     * Moving forward last reading dates of channels and registers, and perform
     * a validation followed by an estimation => channels/registers have an estimated value on given date.
     * Requires that the user specifies the timestamp
     * on which the last reading of channels and registers should be set.
     */
    FORCE_VALIDATION_AND_ESTIMATION(MicroCategory.VALIDATION_AND_ESTIMATION),

    /**
     * Starts the recurring communication on the device
     * by activating all connection and schedule all
     * recurrent communication tasks to execute now.
     */
    START_RECURRING_COMMUNICATION(MicroCategory.COMMUNICATION),

    /**
     * Close the <i>communication and validation</i> issues on the device with the status <i>Won't fix</i>.
     */
    CLOSE_ALL_ISSUES(MicroCategory.ISSUES),

    /**
     * Removes the device and all it's references
     */
    REMOVE_DEVICE(MicroCategory.RETENTION),

    /**
     * Sets the value of a multiplier on the device
     */
    SET_MULTIPLIER(MicroCategory.DATA_COLLECTION),

    /**
     * Removes the location configured on the device
     */
    REMOVE_LOCATION(MicroCategory.DECOMMISSION),

    /**
     * Link the device to the usage point
     */
    LINK_TO_USAGE_POINT(MicroCategory.INSTALLATION),

    /**
     * Cancels all running Service Calls on the device
     */
    CANCEL_ALL_SERVICE_CALLS(MicroCategory.MONITORING);

    private MicroCategory category;
    private String conflictGroupKey;

    MicroAction(MicroCategory category) {
        this.category = category;
    }

    MicroAction(MicroCategory category, String conflictGroupKey) {
        this(category);
        this.conflictGroupKey = conflictGroupKey;
    }

    public MicroCategory getCategory() {
        return category;
    }

    public Optional<String> getConflictGroupKey() {
        return Optional.ofNullable(this.conflictGroupKey);
    }
}