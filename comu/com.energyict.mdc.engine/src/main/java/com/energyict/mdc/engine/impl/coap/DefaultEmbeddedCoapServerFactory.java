/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;

/**
 * Provides factory services for {@link EmbeddedCoapServer}s.
 */
public final class DefaultEmbeddedCoapServerFactory implements EmbeddedCoapServerFactory {

    private final WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    public DefaultEmbeddedCoapServerFactory(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        super();
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    public EmbeddedCoapServer findOrCreateFor(CoapBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        return EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    /**
     * Provides an implementation for an "null" {@link EmbeddedCoapServer}.
     */
    private static class VoidEmbeddedWebServer implements EmbeddedCoapServer {
        @Override
        public ServerProcessStatus getStatus() {
            return ServerProcessStatus.STARTING;
        }

        @Override
        public void start() {
            // The null object does not need an implementation here
        }

        @Override
        public void shutdown() {
            // The null object does not need an implementation here
        }

        @Override
        public void shutdownImmediate() {
            // The null object does not need an implementation here
        }
    }

    private class EmbeddedCaliforniumServerServiceProvider implements EmbeddedCaliforniumServer.ServiceProvider {
        @Override
        public WebSocketEventPublisherFactory webSocketEventPublisherFactory() {
            return webSocketEventPublisherFactory;
        }
    }
}