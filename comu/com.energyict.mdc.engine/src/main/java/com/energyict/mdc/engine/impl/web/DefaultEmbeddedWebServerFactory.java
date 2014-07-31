package com.energyict.mdc.engine.impl.web;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides factory services for {@link EmbeddedWebServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (10:17)
 */
@Component(name = "com.energyict.mdc.engine.web.embedded.server", service = EmbeddedWebServerFactory.class, immediate = true)
public final class DefaultEmbeddedWebServerFactory implements EmbeddedWebServerFactory {

    private volatile WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    public DefaultEmbeddedWebServerFactory() {
        super();
    }

    // For testing purposes only
    public DefaultEmbeddedWebServerFactory(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        super();
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    @Reference
    public void setWebSocketEventPublisherFactory(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    public EmbeddedWebServer findOrCreateFor(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        return EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public EmbeddedWebServer findOrCreateEventWebServer (ComServer comServer) {
        try {
            URI eventRegistrationUri = new URI(comServer.getEventRegistrationUriIfSupported());
            return EmbeddedJettyServer.newForEventMechanism(eventRegistrationUri, new EmbeddedJettyServerServiceProvider());
        }
        catch (BusinessException e) {
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
            throw CodingException.validationFailed(e, comServerClassName, propertyName);
        }
    }

    @Override
    public EmbeddedWebServer findOrCreateRemoteQueryWebServer (RunningOnlineComServer runningComServer) {
        try {
            String queryApiPostUri = runningComServer.getComServer().getQueryApiPostUriIfSupported();
            return EmbeddedJettyServer.newForQueryApi(new URI(queryApiPostUri), runningComServer);
        }
        catch (BusinessException e) {
            // Event registration is not supported
            return new VoidEmbeddedWebServer();
        }
        catch (URISyntaxException e) {
            throw CodingException.validationFailed(e, "OnlineComServerImpl", "onlineComServer.queryAPIPostURI");
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