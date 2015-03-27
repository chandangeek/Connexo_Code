package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

/**
 * Supports editing {@link AuthorizedAction}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-26 (15:04)
 */
public interface AuthorizedActionEditor {

    /**
     * Saves the changes and returns the saved {@link AuthorizedAction}.
     *
     * @return The save AuthorizedAction
     */
    public AuthorizedAction saveChanges();

}