package com.energyict.mdc.device.config;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.TypeId;
import com.energyict.mdc.common.UserAction;

/**
 * A Privilege is an action that can be performed on a certain object type.
 *
 * @author Steven Willems.
 * @since Jun 2, 2009.
 */
public interface Privilege extends IdBusinessObject {

    /**
     * Get the {@link UserAction Action}.
     *
     * @return the allowed action.
     */
    UserAction getAction();

    /**
     * Get the {@link TypeId TypeId} the action is allowed on.
     *
     * @return The object type specified by its TypeId.
     */
    TypeId getTypeId();

    /**
     * Get the {@link Role Role} the privilege is defined for.
     *
     * @return The role this privilege is defined for.
     */
    Role getRole();

}
