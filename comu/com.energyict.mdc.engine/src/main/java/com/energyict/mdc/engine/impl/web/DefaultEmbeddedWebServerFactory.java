/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides factory services for {@link EmbeddedWebServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (10:17)
 */
public final class DefaultEmbeddedWebServerFactory implements EmbeddedWebServerFactory {

    private final WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    public DefaultEmbeddedWebServerFactory(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        super();
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    public EmbeddedWebServer findOrCreateFor(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        return EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public EmbeddedWebServer findOrCreateEventWebServer (ComServer comServer, EventAPIStatistics eventAPIStatistics) {
        try {
            URI eventRegistrationUri = new URI(comServer.getEventRegistrationUriIfSupported());
            return EmbeddedJettyServer.newForEventMechanism(eventRegistrationUri, eventAPIStatistics, new EmbeddedJettyServerServiceProvider());
        }
        catch (UnsupportedOperationException e) {
            // Event registration is not supported
            return new VoidEmbeddedWebServer();
        }
        catch (URISyntaxException e) {
            String comServerClassName;
            String propertyName;
            if (comServer.isOnline()) {
                comServerClassName = "OnlineComServerImpl";
                propertyName = "onlineComServer.eventRegistrationURI";
            }
            else {
                comServerClassName = "RemoteComServerImpl";
                propertyName = "remoteComServer.eventRegistrationURI";
            }
            throw CodingException.validationFailed(e, comServerClassName, propertyName, MessageSeeds.VALIDATION_FAILED);
        }
    }

    @Override
    public EmbeddedWebServer findOrCreateRemoteQueryWebServer (RunningOnlineComServer runningComServer, QueryAPIStatistics queryAPIStatistics) {
        try {
            String queryApiPostUri = runningComServer.getComServer().getQueryApiPostUriIfSupported();
            return EmbeddedJettyServer.newForQueryApi(new URI(queryApiPostUri), runningComServer, queryAPIStatistics);
        }
        catch (UnsupportedOperationException e) {
            // Event registration is not supported
            return new VoidEmbeddedWebServer();
        }
        catch (URISyntaxException e) {
            throw CodingException.validationFailed(e, "OnlineComServerImpl", "onlineComServer.queryAPIPostURI", MessageSeeds.VALIDATION_FAILED);
        }
    }

    /**
     * Provides an implementation for an "null" {@link EmbeddedWebServer}.
     */
    private static class VoidEmbeddedWebServer implements EmbeddedWebServer {
        @Override
        public ServerProcessStatus getStatus () {
            return ServerProcessStatus.STARTING;
        }

        @Override
        public void start () {
            // The null object does not need an implementation here
        }

        @Override
        public void shutdown () {
            // The null object does not need an implementation here
        }

        @Override
        public void shutdownImmediate () {
            // The null object does not need an implementation here
        }

    }

    private class EmbeddedJettyServerServiceProvider implements EmbeddedJettyServer.ServiceProvider {
        @Override
        public WebSocketEventPublisherFactory webSocketEventPublisherFactory() {
            return webSocketEventPublisherFactory;
        }
    }

}