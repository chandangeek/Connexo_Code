package com.energyict.mdc.engine.events;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;

/**
 * Supports client applications to initiate the event registration mechanism.
 * Returns the URL that should be used by clients to setup a WebSocket.
 * Todo: describe the next steps involved by clients to complete
 *       the registration process and how to register interests in certain events.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (13:22)
 */
public interface EventRegistrationRequestInitiator {

    /**
     * Gets the URL that should be used by the client application
     * to setup a WebSocket to complete the registration process,
     * register interests for events and start receiving events
     * as and when they occur in the {@link ComServer}.
     * Note that only {@link OnlineComServer}s and {@link RemoteComServer}s
     * support the event registration process.
     *
     * @param comServer The ComServer
     * @return The URL to create a WebSocket
     * @throws BusinessException Thrown when the specified ComServer does not support events
     */
    public String getRegistrationURL (ComServer comServer) throws BusinessException;

    /**
     * Gets the URL that should be used by the client application
     * to setup a WebSocket to complete the registration process,
     * register interests for events and start receiving events
     * as and when they occur in the {@link ComServer} with the specified name.
     * Note that only {@link OnlineComServer}s and {@link RemoteComServer}s
     * support the event registration process.
     *
     * @param comServerName The name of the ComServer
     * @return The URL to create a WebSocket
     * @throws BusinessException Thrown when the specified ComServer does not support events
     */
    public String getRegistrationURL (String comServerName) throws BusinessException;

}