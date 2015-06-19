package com.energyict.mdc.device.lifecycle.config;

import java.util.EnumSet;
import java.util.Set;

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
    // storage = bits 1
    SET_LAST_READING(MicroCategory.DATA_COLLECTION),

    /**
     * Enables data validation on the device.
     * Requires that the user specifies the timestamp
     * from which data should be validated.
     */
    // storage = bits 2
    ENABLE_VALIDATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_validation"),

    /**
     * Disables data validation on the device.
     */
    // storage = bits 4
    DISABLE_VALIDATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_validation"),

    /**
     * Activates all connection tasks on the device.
     */
    // storage = bits 8
    ACTIVATE_CONNECTION_TASKS(MicroCategory.COMMUNICATION),

    /**
     * Starts the communication on the device
     * by activating all connection and schedule all
     * communication tasks to execute now.
     *
     * @see #ACTIVATE_CONNECTION_TASKS
     */
    // storage = bits 16
    START_COMMUNICATION(MicroCategory.COMMUNICATION),

    /**
     * Disable communication on the device
     * by putting all connection and communication tasks on hold.
     */
    // storage = bits 32
    DISABLE_COMMUNICATION(MicroCategory.COMMUNICATION),

    /**
     * Creates a meter activation for the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should start.
     */
    // storage = bits 64
    CREATE_METER_ACTIVATION(MicroCategory.DATA_COLLECTION, "conflict_meter_activation"),

    /**
     * Closes the current meter activation on the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should end.
     */
    // storage = bits 128
    CLOSE_METER_ACTIVATION(MicroCategory.DATA_COLLECTION),

    /**
     * Removes the device from all enumerated device groups
     * it is contained in.
     */
    // storage = bits 256
    REMOVE_DEVICE_FROM_STATIC_GROUPS(MicroCategory.DATA_COLLECTION),

    /**
     * Detaches a slave device from its physical gateway.
     */
    // storage = bits 512
    DETACH_SLAVE_FROM_MASTER(MicroCategory.TOPOLOGY),

    /**
     * Enables data estimation on the device.
     */
    // storage = bits 1024
    ENABLE_ESTIMATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_estimation"),

    /**
     * Disables data estimation on the device.
     */
    // storage = bits 2048
    DISABLE_ESTIMATION(MicroCategory.VALIDATION_AND_ESTIMATION, "conflict_estimation"),

    /**
     * Moving forward lastreading dates of channels and registers, and perform
     * a validation followed by an estimation => channels/registers have an estimated value on given date.
     * Requires that the user specifies the timestamp
     * on which the lastreading of channels and registers should be set.
     */
    // storage = bits 4096
    FORCE_VALIDATION_AND_ESTIMATION(MicroCategory.VALIDATION_AND_ESTIMATION);

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

    public String getConflictGroupKey() {
        return conflictGroupKey;
    }

    /**
     * Gets the Set of {@link MicroCheck}s that are implied
     * by this MicroAction and that therefore need to
     * be included in the {@link AuthorizedTransitionAction}
     * that uses this MicroAction.
     *
     * @return The Set of MicroCheck
     */
    public Set<MicroCheck> impliedChecks() {
        return EnumSet.noneOf(MicroCheck.class);
    }

    }