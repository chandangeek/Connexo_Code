package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.shadow.servers.RemoteComServerShadow;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.RemoteComServer} this is is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (15:39)
 */
public interface ServerRemoteComServer extends RemoteComServer, ServerComServer<RemoteComServerShadow> {
}