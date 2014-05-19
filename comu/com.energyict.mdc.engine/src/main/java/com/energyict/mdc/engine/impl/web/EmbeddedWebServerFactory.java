package com.energyict.mdc.engine.impl.web;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides factory services for {@link EmbeddedWebServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-04 (12:03)
 */
public interface EmbeddedWebServerFactory {

    public AtomicReference<EmbeddedWebServerFactory> DEFAULT = new AtomicReference<EmbeddedWebServerFactory>(new DefaultEmbeddedWebServerFactory());

    /**
     * Finds or creates the {@link EmbeddedWebServer} that hosts
     * the servlet that supports inbound communication
     * on the specified ServerServletBasedInboundComPort ComPort.
     *
     * @param comPort The ServerServletBasedInboundComPort
     * @param comServerDAO The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param serviceProvider The IssueService
     * @return The EmbeddedWebServer
     */
    public EmbeddedWebServer findOrCreateFor(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider);

    /**
     * Finds or creates the {@link EmbeddedWebServer} that hosts
     * the servlet that supports remote clients to register for
     * and receive events about what is going on in the specified
     * {@link com.energyict.mdc.engine.model.ComServer}.
     *
     * @param comServer The ComServer
     * @return The EmbeddedWebServer
     */
    public EmbeddedWebServer findOrCreateEventWebServer (ComServer comServer);

    /**
     * Finds or creates the {@link EmbeddedWebServer} that hosts
     * the servlet that supports {@link com.energyict.mdc.engine.model.RemoteComServer}s
     * to execute queries using the specified
     * {@link com.energyict.mdc.engine.model.OnlineComServer}.
     *
     * @param comServer The OnlineComServer
     * @return The EmbeddedWebServer
     */
    public EmbeddedWebServer findOrCreateRemoteQueryWebServer (OnlineComServer comServer);

}