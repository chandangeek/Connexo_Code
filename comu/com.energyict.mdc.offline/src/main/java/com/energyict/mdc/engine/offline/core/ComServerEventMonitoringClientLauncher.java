package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.OfflineExecuter;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.decorators.EventDecorator;
import com.energyict.mdc.engine.offline.gui.decorators.EventDecoratorFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;


import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/09/2014 - 13:33
 */
public class ComServerEventMonitoringClientLauncher {

    /**
     * Fixed URL, only used in detached mode (localhost, offline)
     */
    public static final String EVENT_REGISTRATION_URI = "ws://localhost:8090/events/registration";

    private static final Logger LOGGER = Logger.getLogger(ComServerEventMonitoringClientLauncher.class.getName());
    private final OfflineExecuter offlineExecuter;

    private EventWebSocket webSocket = new EventWebSocket();

    public ComServerEventMonitoringClientLauncher(OfflineExecuter offlineExecuter) {
        this.offlineExecuter = offlineExecuter;
    }

    public void startEventMonitoringClient() {
        try {
            connectToEventPublisher();
        } catch (Exception e) {
            throw new ApplicationException("Failed to connect to the event monitoring: " + e.getMessage());
        }
    }

    private void connectToEventPublisher() throws Exception {
        LOGGER.info("Connecting to the event publisher");
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.setConnectTimeout(TimeUnit.SECONDS.toMillis(20));
        webSocketClient.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        Future<Session> future = webSocketClient.connect(webSocket, new URI(EVENT_REGISTRATION_URI), request);
        future.get();
    }

    public boolean webSocketConnectionStillValid() {
        return webSocket.getConnection() != null;
    }

    public OfflineExecuter getOfflineExecuter() {
        return offlineExecuter;
    }

    private class EventWebSocket implements WebSocketListener {

        private Session connection;

        public void sendEventRegistrationRequest(String eventInterestRegistrationRequest) throws IOException {
            this.connection.getRemote().sendString(eventInterestRegistrationRequest);
        }

        public Session getConnection() {
            return connection;
        }

        @Override
        public void onWebSocketBinary(byte[] bytes, int i, int i1) {
            LOGGER.info("received binary message, discard");
        }

        @Override
        public void onWebSocketText(String data) {
            if (!data.contains("Copy Register request for") && !data.contains("ComPortOperationsLoggingEvent")) {
                EventDecorator eventDecorator = EventDecoratorFactory.decorate(data);
                if (eventDecorator != null) {
                    UiHelper.getMainWindow().notifyOfComServerMonitorEvent(eventDecorator);
                }
            }
        }

        @Override
        public void onWebSocketClose(int i, String s) {
            LOGGER.info("Event publisher closed the event channel...");
            this.connection = null;
        }

        @Override
        public void onWebSocketConnect(Session session) {
            LOGGER.info("Event channel is open for business...");
            this.connection = session;
            try {
                this.sendEventRegistrationRequest("Register request for tracing: CONNECTION,COMTASK,LOGGING");
            } catch (IOException e) {
                System.err.println("Error while sending initial event registration request to receive all logging, see stacktrace below!");
                e.printStackTrace(System.err);
            }
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            cause.printStackTrace(System.err);
        }
    }
}