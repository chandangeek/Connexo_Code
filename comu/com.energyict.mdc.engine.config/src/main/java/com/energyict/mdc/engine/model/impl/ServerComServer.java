package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.ComServer} this is is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (15:27)
 */
public interface ServerComServer extends ComServer {
    /**
     * Gets the URI on which the event registration mechanism runs
     * if that mechanism is supported.
     *
     * @return The URI
     * @throws BusinessException Thrown if this ComServer does not support event registration
     */
    public String getEventRegistrationUriIfSupported () throws BusinessException;

    /**
     * Gets the URI on which the remote query api runs if that is supported.
     *
     * @return The URI
     * @throws BusinessException Thrown if this ComServer does not support the remote query api
     */
    public String getQueryApiPostUriIfSupported () throws BusinessException;

}