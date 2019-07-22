/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.ComTask;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the classes that are involved in
 * the text based event registration/publishing mechanism
 * by starting a jetty server and posting
 * registration requests on a WebSocket
 * and waiting for events to be received.
 * The generation of the events is mocked.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (14:54)
 */
@RunWith(MockitoJUnitRunner.class)
public class TextBasedEventFilterIntegrationTest {

    @Mock
    private Clock clock;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private OnlineComServer comServer;
    @Mock
    private RunningOnlineComServer runningComServer;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private RequestParser.ServiceProvider requestParserServiceProvider;
    @Mock
    private EventAPIStatistics eventApiStatistics;

    private EmbeddedWebServerFactory embeddedWebServerFactory;
    private String eventRegistrationURL = "ws://localhost:8181/events/registration";


    @Before
    public void initializeMocks() {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
    }

    @Before
    public void setupEmbeddedWebServerFactory() {
        EventGenerator eventGenerator = new EventGenerator();
        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this.runningComServer,
                        this.connectionTaskService,
                        this.communicationTaskService,
                        this.deviceService,
                        this.engineConfigurationService,
                        this.identificationService,
                        eventGenerator);
        this.embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);
    }

    /**
     * Tests that the a client receives a "message not understood"
     * message when a malformed event request is sent.
     *
     * @throws Exception Indicates failure
     */
    @Test
    public void testRegisterMalformedEventRequest() throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();

        CountDownLatch registrationLatch = new CountDownLatch(1);
        LatchDrivenWebSocketEventPublisherFactory webSocketEventPublisherFactory = new LatchDrivenWebSocketEventPublisherFactory(registrationLatch, eventGenerator);
        this.embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);
        RegisterAndReceiveAllEventCategories webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        EmbeddedWebServer webServer = this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer, eventApiStatistics);
        webServer.start();
        //WebSocketClientFactory factory = new WebSocketClientFactory();
        //factory.start();
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.setConnectTimeout(TimeUnit.SECONDS.toMillis(5));
        webSocketClient.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        Future<Session> future = webSocketClient.connect(webSocket, new URI(eventRegistrationURL), request);
        future.get();
        //webSocketClient.open(new URI(eventRegistrationURL), webSocket, 5, TimeUnit.SECONDS);

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.registerMalformedRequest();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Assert that "message not understood" have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for message not understood reply from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(1);
            assertThat(webSocket.getReceivedMessages().get(0)).startsWith("Message not understood");
        } finally {
            webSocket.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    /**
     * Tests that the a client that registers to receive
     * all event categories, receives connection established
     * and connection closed events.
     *
     * @throws Exception Indicates failure
     */
    @Test
    public void testRegisterForAllEventsAndReceiveMockedConnectionEvents() throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();

        CountDownLatch registrationLatch = new CountDownLatch(1);
        LatchDrivenWebSocketEventPublisherFactory webSocketEventPublisherFactory = new LatchDrivenWebSocketEventPublisherFactory(registrationLatch, eventGenerator);
        this.embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch = new CountDownLatch(3);
        RegisterAndReceiveAllEventCategories webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        EmbeddedWebServer webServer = this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer, eventApiStatistics);
        webServer.start();
       // WebSocketClientFactory factory = new WebSocketClientFactory();
        //factory.start();
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.setConnectTimeout(TimeUnit.SECONDS.toMillis(5));
        webSocketClient.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        Future<Session> future = webSocketClient.connect(webSocket, new URI(eventRegistrationURL), request);
        future.get();

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.register();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Produce a connect and disconnect event
            eventGenerator.produceConnectDisconnectEvents();

            // Assert that both events have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for messages from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(3); // First message confirms the registration, then 1 message for every event

        } finally {
            webSocket.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    @Test
    public void testRegisterTwoClientsForAllEventsAndReceiveMockedConnectionEvents() throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();

        CountDownLatch registrationLatch = new CountDownLatch(2);
        LatchDrivenWebSocketEventPublisherFactory webSocketEventPublisherFactory = new LatchDrivenWebSocketEventPublisherFactory(registrationLatch, eventGenerator);
        this.embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch1 = new CountDownLatch(3);
        CountDownLatch messagesReceivedLatch2 = new CountDownLatch(3);
        RegisterAndReceiveAllEventCategories webSocket1 = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch1);
        RegisterAndReceiveAllEventCategories webSocket2 = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch2);
        EmbeddedWebServer webServer = this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer, eventApiStatistics);
        webServer.start();
        //WebSocketClientFactory factory = new WebSocketClientFactory();
        //factory.start();
        WebSocketClient webSocketClient1 = new WebSocketClient();
        webSocketClient1.setConnectTimeout(TimeUnit.SECONDS.toMillis(5));
        webSocketClient1.start();
        ClientUpgradeRequest request1 = new ClientUpgradeRequest();
        Future<Session> future1 = webSocketClient1.connect(webSocket1, new URI(eventRegistrationURL), request1);
        future1.get();

        WebSocketClient webSocketClient2 = new WebSocketClient();
        webSocketClient2.setConnectTimeout(TimeUnit.SECONDS.toMillis(5));
        webSocketClient2.start();
        ClientUpgradeRequest request2 = new ClientUpgradeRequest();
        Future<Session> future2 = webSocketClient1.connect(webSocket2, new URI(eventRegistrationURL), request2);
        future2.get();



        try {
            assertThat(webSocket1.isOpen()).isTrue();
            webSocket1.register();
            webSocket2.register();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Produce a connect and disconnect event
            eventGenerator.produceConnectDisconnectEvents();

            // Assert that both events have been received
            assertThat(messagesReceivedLatch1.await(5, TimeUnit.SECONDS)).as("Timeout while client 1 is waiting for messages from server.").isTrue();
            assertThat(messagesReceivedLatch2.await(5, TimeUnit.SECONDS)).as("Timeout while client 2 is waiting for messages from server.").isTrue();
            assertThat(webSocket1.getReceivedMessages()).hasSize(3); // First message confirms the registration, then 1 message for every event
            assertThat(webSocket2.getReceivedMessages()).hasSize(3); // First message confirms the registration, then 1 message for every event
        } finally {
            webSocket1.closeIfOpen();
            webSocket2.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    /**
     * Tests that a client that only registers for {@link ComTask}
     * related events, does not receive connection established
     * and connection closed events.
     *
     * @throws Exception Indicates failure
     */
    @Test
    public void testRegisterForOnlyComTaskEventsAndPublishMockedConnectionEvents() throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();

        CountDownLatch registrationLatch = new CountDownLatch(1);
        LatchDrivenWebSocketEventPublisherFactory webSocketEventPublisherFactory = new LatchDrivenWebSocketEventPublisherFactory(registrationLatch, eventGenerator);
        this.embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);
        RegisterAndReceiveAllEventCategories webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        EmbeddedWebServer webServer = this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer, eventApiStatistics);
        webServer.start();
        //WebSocketClientFactory factory = new WebSocketClientFactory();
        //factory.start();
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.setConnectTimeout(TimeUnit.SECONDS.toMillis(5));
        webSocketClient.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        Future<Session> future = webSocketClient.connect(webSocket, new URI(eventRegistrationURL), request);
        future.get();

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.registerForComTasksOnly();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Produce a connect and disconnect event
            eventGenerator.produceConnectDisconnectEvents();

            // Assert that both events have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for messages from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(1); // Single message that confirms the registration
        } finally {
            webSocket.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    @WebSocket
    private class RegisterAndReceiveAllEventCategories {

        private CountDownLatch messageReceivedLatch;
        private List<String> receivedMessages = new ArrayList<>();
        private Session session;

        private RegisterAndReceiveAllEventCategories() {
            super();
        }

        private RegisterAndReceiveAllEventCategories(CountDownLatch messageReceivedLatch) {
            this();
            this.messageReceivedLatch = messageReceivedLatch;
        }

        public void closeIfOpen() {
            if (this.isOpen()) {
                this.session.close();
            }
        }

        public boolean isOpen() {
            return this.session != null;
        }

        public void registerMalformedRequest() throws IOException {
            this.session.getRemote().sendString("Anything as long as it does not conform to the expected parse format");
        }

        public void register() throws IOException {
            this.session.getRemote().sendString("Register request for info:");
        }

        public void registerForComTasksOnly() throws IOException {
            this.session.getRemote().sendString("Register request for debugging: COMTASK");
        }

        public synchronized List<String> getReceivedMessages() {
            return receivedMessages;
        }

        @OnWebSocketMessage
        public synchronized void onMessage(String data) {
            this.receivedMessages.add(data);
            if (this.messageReceivedLatch != null) {
                this.messageReceivedLatch.countDown();
            }
        }

        @OnWebSocketConnect
        public void onOpen(Session session) {
            this.session = session;
        }

        @OnWebSocketClose
        public void onClose(int closeCode, String message) {
            this.session = null;
        }
    }

    private class EventGenerator extends EventPublisherImpl {
        private Device device;
        private OutboundConnectionTask connectionTask;
        private OutboundComPort comPort;
        private OutboundComPortPool comPortPool;

        private EventGenerator() {
            super(runningComServer);
            this.device = mock(Device.class);
            this.connectionTask = mock(OutboundConnectionTask.class);
            this.comPort = mock(OutboundComPort.class);
            this.comPortPool = mock(OutboundComPortPool.class);
            List<OutboundComPort> outboundComPorts = Arrays.asList(this.comPort);
            when(this.comPortPool.getComPorts()).thenReturn(outboundComPorts);
        }

        public void produceConnectDisconnectEvents() {
            this.sendMockedConnectionEstablishedEvent();
            this.sendMockedConnectionClosedEvent();
        }

        private void sendMockedConnectionEstablishedEvent() {
            ConnectionEvent connectionEvent = mock(ConnectionEvent.class);
            when(connectionEvent.getCategory()).thenReturn(Category.CONNECTION);
            when(connectionEvent.isEstablishing()).thenReturn(true);
            when(connectionEvent.isClosed()).thenReturn(false);
            when(connectionEvent.isFailure()).thenReturn(false);
            when(connectionEvent.isDeviceRelated()).thenReturn(true);
            when(connectionEvent.getDevice()).thenReturn(this.device);
            when(connectionEvent.isConnectionTaskRelated()).thenReturn(true);
            when(connectionEvent.getConnectionTask()).thenReturn(this.connectionTask);
            when(connectionEvent.isComPortRelated()).thenReturn(true);
            when(connectionEvent.getComPort()).thenReturn(this.comPort);
            this.publish(connectionEvent);
        }

        private void sendMockedConnectionClosedEvent() {
            ConnectionEvent connectionEvent = mock(ConnectionEvent.class);
            when(connectionEvent.getCategory()).thenReturn(Category.CONNECTION);
            when(connectionEvent.isClosed()).thenReturn(true);
            when(connectionEvent.isEstablishing()).thenReturn(false);
            when(connectionEvent.isFailure()).thenReturn(false);
            when(connectionEvent.isDeviceRelated()).thenReturn(true);
            when(connectionEvent.getDevice()).thenReturn(this.device);
            when(connectionEvent.isConnectionTaskRelated()).thenReturn(true);
            when(connectionEvent.getConnectionTask()).thenReturn(this.connectionTask);
            when(connectionEvent.isComPortRelated()).thenReturn(true);
            when(connectionEvent.getComPort()).thenReturn(this.comPort);
            this.publish(connectionEvent);
        }

    }

    private class LatchDrivenWebSocketEventPublisher extends WebSocketEventPublisher {
        private CountDownLatch latch;

        private LatchDrivenWebSocketEventPublisher(CountDownLatch latch, EventPublisher eventPublisher, WebSocketCloseEventListener closeEventListener) {
            super(runningComServer, requestParserServiceProvider, eventPublisher, closeEventListener);
            this.latch = latch;
        }

        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            this.latch.countDown();
        }
    }

    private class LatchDrivenWebSocketEventPublisherFactory extends WebSocketEventPublisherFactoryImpl {
        private CountDownLatch latch;
        private EventPublisher eventPublisher;

        private LatchDrivenWebSocketEventPublisherFactory(CountDownLatch latch, EventPublisher eventPublisher) {
            super(runningComServer, connectionTaskService, communicationTaskService, deviceService, engineConfigurationService, identificationService, eventPublisher);
            this.latch = latch;
            this.eventPublisher = eventPublisher;
        }

        @Override
        public WebSocketEventPublisher newWebSocketEventPublisher(WebSocketCloseEventListener closeEventListener) {
            return new LatchDrivenWebSocketEventPublisher(this.latch, this.eventPublisher, closeEventListener);
        }
    }

}