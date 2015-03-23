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
     * Enables data validation on the device.
     */
    ENABLE_VALIDATION,

    /**
     * Disables data validation on the device.
     */
    DISABLE_VALIDATION,

    /**
     * Removes the device from all enumerated device groups
     * it is contained in.
     */
    REMOVE_DEVICE_FROM_STATIC_GROUPS;

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