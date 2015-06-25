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
    SET_LAST_READING,

    /**
     * Enables data validation on the device.
     * Requires that the user specifies the timestamp
     * from which data should be validated.
     */
    // storage = bits 2
    ENABLE_VALIDATION,

    /**
     * Disables data validation on the device.
     */
    // storage = bits 4
    DISABLE_VALIDATION,

    /**
     * Activates all connection tasks on the device.
     */
    // storage = bits 8
    ACTIVATE_CONNECTION_TASKS,

    /**
     * Starts the communication on the device
     * by activating all connection and schedule all
     * communication tasks to execute now.
     *
     * @see #ACTIVATE_CONNECTION_TASKS
     */
    // storage = bits 16
    START_COMMUNICATION,

    /**
     * Disable communication on the device
     * by putting all connection and communication tasks on hold.
     */
    // storage = bits 32
    DISABLE_COMMUNICATION,

    /**
     * Creates a meter activation for the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should start.
     */
    // storage = bits 64
    CREATE_METER_ACTIVATION,

    /**
     * Closes the current meter activation on the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should end.
     */
    // storage = bits 128
    CLOSE_METER_ACTIVATION,

    /**
     * Removes the device from all enumerated device groups
     * it is contained in.
     */
    // storage = bits 256
    REMOVE_DEVICE_FROM_STATIC_GROUPS,

    /**
     * Detaches a slave device from its physical gateway.
     */
    // storage = bits 512
    DETACH_SLAVE_FROM_MASTER,

    /**
     * Enables data estimation on the device.
     */
    // storage = bits 1024
    ENABLE_ESTIMATION,

    /**
     * Disables data estimation on the device.
     */
    // storage = bits 2048
    DISABLE_ESTIMATION,

    /**
     * Moving forward lastreading dates of channels and registers, and perform
     * a validation followed by an estimation => channels/registers have an estimated value on given date.
     * Requires that the user specifies the timestamp
     * on which the lastreading of channels and registers should be set.
     */
    // storage = bits 4096
    FORCE_VALIDATION_AND_ESTIMATION,

    /**
    * Starts the recurring communication on the device
    * by activating all connection and schedule all
    * recurrent communication tasks to execute now.
    */
    // storage = bits 8192
    START_RECURRING_COMMUNICATION,

    /**
     * Close the <i>communication and validation</i> issues on the device with the status <i>Won't fix</i>.
     */
    // storage = bits 16384
    CLOSE_ALL_ISSUES;

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