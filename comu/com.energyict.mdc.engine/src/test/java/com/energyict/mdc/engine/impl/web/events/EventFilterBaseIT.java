package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.monitor.EventAPIStatisticsImpl;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public abstract class EventFilterBaseIT {
    public static final String EVENT_REGISTRATION_URL = "ws://localhost:8181/events/registration";

    @Mock
    protected ComServer comServer;
    @Mock
    protected RunningComServer runningComServer;
    @Mock
    DeviceService deviceService;
    @Mock
    private RequestParser.ServiceProvider serviceProvider;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    protected CountDownLatch registrationLatch;
    protected CountDownLatch messagesReceivedLatch;
    protected RegisterAndReceiveAllEventCategories webSocket;
    protected EmbeddedWebServer webServer;
    private EmbeddedWebServerFactory factory;

    public void setup(int regCount, int msgCount) throws Exception {
        registrationLatch = new CountDownLatch(regCount);
        //new LatchDrivenWebSocketEventPublisherFactory(registrationLatch, runningComServer, connectionTaskService, communicationTaskService, deviceService,engineConfigurationService, identificationService, eventPublisher, serviceProvider);

        // Start the EventServlet in a jetty context
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(EVENT_REGISTRATION_URL);
        messagesReceivedLatch = new CountDownLatch(msgCount);
        webSocket = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch);
        this.factory = new DefaultEmbeddedWebServerFactory(this.webSocketEventPublisherFactory);
        webServer = this.factory.findOrCreateEventWebServer(comServer, new EventAPIStatisticsImpl());
        webServer.start();
        connectClient(EVENT_REGISTRATION_URL, webSocket, TimeUnit.SECONDS.toMillis(5));
    }

    public void shutdown() {
        webSocket.closeIfOpen();
        webServer.shutdownImmediate();
    }

    public void checkResults(int replies, int messages, int events) throws InterruptedException {
        // Assert that both events have been received
        assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for messages from server.").isTrue();
        assertThat(webSocket.getReceivedReplies()).isEqualTo(replies); // First message confirms the registration, then 1 message for every event
        assertThat(webSocket.getReceivedMessages()).hasSize(messages);
        assertThat(webSocket.getReceivedEvents()).hasSize(events);
    }

    public static void connectClient(String eventRegistrationURL, RegisterAndReceiveAllEventCategories webSocket, long connectionTimeout) throws Exception {
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.setConnectTimeout(connectionTimeout);
        webSocketClient.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        Future<Session> future = webSocketClient.connect(webSocket, new URI(eventRegistrationURL), request);
        future.get();
    }

}