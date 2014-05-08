package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.connection.CloseConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.EstablishConnectionEvent;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.google.common.base.Optional;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the classes that are involved in
 * the binary event registration/publishing mechanism
 * by starting a jetty server and posting
 * registration requests on a WebSocket
 * and waiting for events to be received.
 * The generation of the events is mocked.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (09:36)
 */
@RunWith(MockitoJUnitRunner.class)
public class BinaryBasedEventFilterIntegrationTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final long CONNECTIONTASK_ID = COMPORT_ID + 1;
    private static final long COMPORT_POOL_ID = CONNECTIONTASK_ID + 1;

    @Mock
    private EngineModelService engineModelService;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private Device device;
    @Mock
    private ScheduledConnectionTask connectionTask;
    @Mock
    private OutboundComPort comPort;
    @Mock
    private OutboundComPortPool comPortPool;

    @Before
    public void initializeMocksAndFactories () {
        this.device = mock(Device.class);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceDataService.findDeviceById(DEVICE_ID)).thenReturn(this.device);
        this.connectionTask = mock(ScheduledConnectionTask.class);
        when(this.connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.<ConnectionTask>of(this.connectionTask));
        this.comPort = mock(OutboundComPort.class);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(this.comPort);
        this.comPortPool = mock(OutboundComPortPool.class);
        List<OutboundComPort> outboundComPorts = Arrays.asList(this.comPort);
        when(this.comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        when(this.comPortPool.getComPorts()).thenReturn(outboundComPorts);
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
            assertThat(webSocket.getReceivedReplies()).isEqualTo(3); // First message confirms the registration, then 1 message for every event
            assertThat(webSocket.getReceivedMessages()).hasSize(1);
            assertThat(webSocket.getReceivedEvents()).hasSize(2);

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
            assertThat(webSocket1.getReceivedReplies()).isEqualTo(3); // First message confirms the registration, then 1 message for every event
            assertThat(webSocket2.getReceivedMessages()).hasSize(1);
            assertThat(webSocket2.getReceivedEvents()).hasSize(2);
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
            assertThat(webSocket.getReceivedReplies()).isEqualTo(1); // Single message that confirms the registration
            assertThat(webSocket.getReceivedMessages()).hasSize(1);  // Single message that confirms the registration
            assertThat(webSocket.getReceivedEvents()).isEmpty();     // No events expected
        }
        finally {
            webSocket.closeIfOpen();
            webServer.shutdownImmediate();
        }
    }

    private class RegisterAndReceiveAllEventCategories implements WebSocket.OnBinaryMessage {

        private CountDownLatch messageReceivedLatch;
        private AtomicInteger receivedReplies = new AtomicInteger(0);
        private List<String> receivedMessages = new ArrayList<String>();
        private List<ComServerEvent> receivedEvents = new ArrayList<ComServerEvent>();
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

        public void register () throws IOException {
            this.connection.sendMessage("Register request for binary events for warnings:");
        }

        public void registerForComTasksOnly () throws IOException {
            this.connection.sendMessage("Register request for binary events for tracing: COMTASK");
        }

        public synchronized int getReceivedReplies () {
            return this.receivedReplies.get();
        }

        public synchronized List<String> getReceivedMessages () {
            return receivedMessages;
        }

        public synchronized List<ComServerEvent> getReceivedEvents () {
            return receivedEvents;
        }

        @Override
        public void onMessage (byte[] data, int offset, int length) {
            this.receivedReplies.incrementAndGet();
            try {
                byte[] actualBytes = new byte[length];
                System.arraycopy(data, offset, actualBytes, 0, length);
                this.parseReply(actualBytes);
                if (this.messageReceivedLatch != null) {
                    this.messageReceivedLatch.countDown();
                }
            }
            catch (IOException e) {
                e.printStackTrace(System.err);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace(System.err);
            }
        }

        private synchronized void parseReply (byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            if (object instanceof String) {
                String stringReply = (String) object;
                this.receivedMessages.add(stringReply);
            }
            else {
                ComServerEvent event = (ComServerEvent) object;
                this.receivedEvents.add(event);
            }
            ois.close();
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

        private EventGenerator() {
            super(clock, engineModelService);
        }

        public void produceConnectDisconnectEvents () {
            this.sendMockedConnectionEstablishedEvent();
            this.sendMockedConnectionClosedEvent();
        }

        private void sendMockedConnectionEstablishedEvent () {
            EstablishConnectionEvent connectionEvent = new EstablishConnectionEvent(comPort, connectionTask, new DefaultClock(), deviceDataService, engineModelService);
            this.publish(connectionEvent);
        }

        private void sendMockedConnectionClosedEvent () {
            CloseConnectionEvent connectionEvent = new CloseConnectionEvent(comPort, connectionTask, new DefaultClock(), deviceDataService, engineModelService);
            this.publish(connectionEvent);
        }

    }

    private class LatchDrivenWebSocketEventPublisher extends WebSocketEventPublisher {
        private CountDownLatch latch;

        private LatchDrivenWebSocketEventPublisher (CountDownLatch latch) {
            super();
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