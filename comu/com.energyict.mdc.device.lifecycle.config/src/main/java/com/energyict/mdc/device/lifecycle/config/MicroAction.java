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
    SET_LAST_READING,

    /**
     * Enables data validation on the device.
     * Requires that the user specifies the timestamp
     * from which data should be validated.
     */
    ENABLE_VALIDATION,

    /**
     * Disables data validation on the device.
     */
    DISABLE_VALIDATION,

    /**
     * Activates all connection tasks on the device.
     */
    ACTIVATE_CONNECTION_TASKS,

    /**
     * Starts the communication on the device
     * by activating all connection and schedule all
     * communication tasks to execute now.
     *
     * @see #ACTIVATE_CONNECTION_TASKS
     */
    START_COMMUNICATION,

    /**
     * Disable communication on the device
     * by putting all connection and communication tasks on hold.
     */
    DISABLE_COMMUNICATION,

    /**
     * Creates a meter activation for the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should start.
     */
    CREATE_METER_ACTIVATION,

    /**
     * Closes the current meter activation on the device.
     * Requires that the user specifies the timestamp
     * on which the meter activation should end.
     */
    CLOSE_METER_ACTIVATION,

    /**
     * Removes the device from all enumerated device groups
     * it is contained in.
     */
    REMOVE_DEVICE_FROM_STATIC_GROUPS,

    /**
     * Detaches a slave device from its physical gateway.
     */
    DETACH_SLAVE_FROM_MASTER;

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