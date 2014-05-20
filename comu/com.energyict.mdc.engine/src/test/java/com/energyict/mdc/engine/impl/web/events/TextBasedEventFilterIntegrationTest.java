package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.util.time.Clock;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.runner.*;

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
    private EngineModelService engineModelService;
    @Mock
    private DeviceDataService deviceDataService;

    private ServiceProvider serviceProvider = new FakeServiceProvider();

    /**
     * Tests that the a client receives a "message not understood"
     * message when a malformed event request is sent.
     *
     * @throws Exception Indicates failure
     */
    @Test
    public void testRegisterMalformedEventRequest () throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();
        EventPublisherImpl.setInstance(eventGenerator);

        CountDownLatch registrationLatch = new CountDownLatch(1);
        WebSocketEventPublisherFactory.setInstance(new LatchDrivenWebSocketEventPublisherFactory(registrationLatch));

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        String eventRegistrationURL = "ws://localhost:8080/events/registration";
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);
        RegisterAndReceiveAllEventCategories webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        EmbeddedWebServer webServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);
        webServer.start();
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        WebSocketClient webSocketClient = factory.newWebSocketClient();
        webSocketClient.open(new URI(eventRegistrationURL), webSocket, 5, TimeUnit.SECONDS);

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.registerMalformedRequest();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Assert that "message not understood" have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for message not understood reply from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(1);
            assertThat(webSocket.getReceivedMessages().get(0)).startsWith("Message not understood");
        }
        finally {
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
    public void testRegisterForAllEventsAndReceiveMockedConnectionEvents () throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();
        EventPublisherImpl.setInstance(eventGenerator);

        CountDownLatch registrationLatch = new CountDownLatch(1);
        WebSocketEventPublisherFactory.setInstance(new LatchDrivenWebSocketEventPublisherFactory(registrationLatch));

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        String eventRegistrationURL = "ws://localhost:8080/events/registration";
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch = new CountDownLatch(3);
        RegisterAndReceiveAllEventCategories webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        EmbeddedWebServer webServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);
        webServer.start();
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        WebSocketClient webSocketClient = factory.newWebSocketClient();
        webSocketClient.open(new URI(eventRegistrationURL), webSocket, 5, TimeUnit.SECONDS);

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

        }
        finally {
            webSocket.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    @Test
    public void testRegisterTwoClientsForAllEventsAndReceiveMockedConnectionEvents () throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();
        EventPublisherImpl.setInstance(eventGenerator);

        CountDownLatch registrationLatch = new CountDownLatch(2);
        WebSocketEventPublisherFactory.setInstance(new LatchDrivenWebSocketEventPublisherFactory(registrationLatch));

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        String eventRegistrationURL = "ws://localhost:8080/events/registration";
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch1 = new CountDownLatch(3);
        CountDownLatch messagesReceivedLatch2 = new CountDownLatch(3);
        RegisterAndReceiveAllEventCategories webSocket1 = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch1);
        RegisterAndReceiveAllEventCategories webSocket2 = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch2);
        EmbeddedWebServer webServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);
        webServer.start();
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        WebSocketClient webSocketClient1 = factory.newWebSocketClient();
        webSocketClient1.open(new URI(eventRegistrationURL), webSocket1, 5, TimeUnit.HOURS);
        WebSocketClient webSocketClient2 = factory.newWebSocketClient();
        webSocketClient2.open(new URI(eventRegistrationURL), webSocket2, 5, TimeUnit.HOURS);

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
        }
        finally {
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
    public void testRegisterForOnlyComTaskEventsAndPublishMockedConnectionEvents () throws Exception {
        // Create an EventPublisherImpl that will generate mocked events
        EventGenerator eventGenerator = new EventGenerator();
        EventPublisherImpl.setInstance(eventGenerator);

        CountDownLatch registrationLatch = new CountDownLatch(1);
        WebSocketEventPublisherFactory.setInstance(new LatchDrivenWebSocketEventPublisherFactory(registrationLatch));

        // Start the EventServlet in a jetty context
        ComServer comServer = mock(ComServer.class);
        String eventRegistrationURL = "ws://localhost:8080/events/registration";
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);
        RegisterAndReceiveAllEventCategories webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        EmbeddedWebServer webServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);
        webServer.start();
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        WebSocketClient webSocketClient = factory.newWebSocketClient();
        webSocketClient.open(new URI(eventRegistrationURL), webSocket, 5, TimeUnit.SECONDS);

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
        }
        finally {
            webSocket.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    private class RegisterAndReceiveAllEventCategories implements WebSocket.OnTextMessage {

        private CountDownLatch messageReceivedLatch;
        private List<String> receivedMessages = new ArrayList<>();
        private Connection connection;

        private RegisterAndReceiveAllEventCategories () {
            super();
        }

        private RegisterAndReceiveAllEventCategories (CountDownLatch messageReceivedLatch) {
            this();
            this.messageReceivedLatch = messageReceivedLatch;
        }

        public void closeIfOpen () {
            if (this.isOpen()) {
                this.connection.close();
            }
        }

        public boolean isOpen () {
            return this.connection != null;
        }

        public void registerMalformedRequest () throws IOException {
            this.connection.sendMessage("Anything as long as it does not conform to the expected parse format");
        }

        public void register () throws IOException {
            this.connection.sendMessage("Register request for info:");
        }

        public void registerForComTasksOnly () throws IOException {
            this.connection.sendMessage("Register request for debugging: COMTASK");
        }

        public synchronized List<String> getReceivedMessages () {
            return receivedMessages;
        }

        @Override
        public synchronized void onMessage (String data) {
            this.receivedMessages.add(data);
            if (this.messageReceivedLatch != null) {
                this.messageReceivedLatch.countDown();
            }
        }

        @Override
        public void onOpen (Connection connection) {
            this.connection = connection;
        }

        @Override
        public void onClose (int closeCode, String message) {
            this.connection = null;
        }
    }

    private class EventGenerator extends EventPublisherImpl {
        private BaseDevice device;
        private OutboundConnectionTask connectionTask;
        private OutboundComPort comPort;
        private OutboundComPortPool comPortPool;

        private EventGenerator () {
            super(clock, engineModelService, deviceDataService);
            this.device = mock(BaseDevice.class);
            this.connectionTask = mock(OutboundConnectionTask.class);
            this.comPort = mock(OutboundComPort.class);
            this.comPortPool = mock(OutboundComPortPool.class);
            List<OutboundComPort> outboundComPorts = Arrays.asList(this.comPort);
            when(this.comPortPool.getComPorts()).thenReturn(outboundComPorts);
        }

        public void produceConnectDisconnectEvents () {
            this.sendMockedConnectionEstablishedEvent();
            this.sendMockedConnectionClosedEvent();
        }

        private void sendMockedConnectionEstablishedEvent () {
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

        private void sendMockedConnectionClosedEvent () {
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

        private LatchDrivenWebSocketEventPublisher (CountDownLatch latch) {
            super(serviceProvider);
            this.latch = latch;
        }

        @Override
        public void onMessage (String message) {
            super.onMessage(message);
            this.latch.countDown();
        }
    }

    private class LatchDrivenWebSocketEventPublisherFactory extends WebSocketEventPublisherFactory {
        private CountDownLatch latch;

        private LatchDrivenWebSocketEventPublisherFactory (CountDownLatch latch) {
            super();
            this.latch = latch;
        }

        @Override
        public WebSocketEventPublisher newWebSocketEventPublisher () {
            return new LatchDrivenWebSocketEventPublisher(this.latch);
        }
    }

}