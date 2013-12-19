package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.shadow.servers.OfflineComServerShadow;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.OfflineComServer} this is is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (15:39)
 */
public interface ServerOfflineComServer extends OfflineComServer, ServerComServer<OfflineComServerShadow> {
}