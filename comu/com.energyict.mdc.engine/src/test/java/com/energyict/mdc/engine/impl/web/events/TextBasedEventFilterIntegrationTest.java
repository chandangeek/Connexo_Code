/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.FilteringEventReceiverFactory;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
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
public class TextBasedEventFilterIntegrationTest extends EventFilterBaseIT {

    @Mock
    private ComServer comServer;
    private FilteringEventReceiverFactory filteringEventReceiverFactory;
    private EventGenerator eventGenerator;

    @Before
    public void initialize() {
        eventGenerator = new EventGenerator();
        EventPublisherImpl.setInstance(eventGenerator);
    }

    @After
    public void resetEventPublisher() throws Exception {
        EventPublisherImpl.setInstance(null);
    }

    /**
     * Tests that the a client receives a "message not understood"
     * message when a malformed event request is sent.
     *
     * @throws Exception Indicates failure
     */
    @Test
    public void testRegisterMalformedEventRequest() throws Exception {
        setup(1, 1);

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
            shutdown();
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
        setup(1, 3);

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.registerForInfo();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Produce a connect and disconnect event
            ///eventGenerator.start();
            eventGenerator.produceConnectDisconnectEvents();

            // Assert that both events have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for messages from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(3); // First message confirms the registration, then 1 message for every event

        } finally {
            shutdown();
        }
    }

    @Test
    public void testRegisterTwoClientsForAllEventsAndReceiveMockedConnectionEvents() throws Exception {
        setup(2, 3);
        CountDownLatch messagesReceivedLatch2 = new CountDownLatch(3);
        RegisterAndReceiveAllEventCategories webSocket2 = new RegisterAndReceiveAllEventCategories(messagesReceivedLatch2);
        connectClient(EVENT_REGISTRATION_URL, webSocket2, TimeUnit.SECONDS.toMillis(5));

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.registerForInfo();
            webSocket2.registerForInfo();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Produce a connect and disconnect event
            //eventGenerator.start();
            eventGenerator.produceConnectDisconnectEvents();

            // Assert that both events have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while client 1 is waiting for messages from server.").isTrue();
            assertThat(messagesReceivedLatch2.await(5, TimeUnit.SECONDS)).as("Timeout while client 2 is waiting for messages from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(3); // First message confirms the registration, then 1 message for every event
            assertThat(webSocket2.getReceivedMessages()).hasSize(3); // First message confirms the registration, then 1 message for every event
        } finally {
            webSocket2.closeIfOpen();
            shutdown();
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
        setup(1, 1);

        try {
            assertThat(webSocket.isOpen()).isTrue();
            webSocket.registerForComTasksOnly();

            // Wait for the registration to complete
            assertThat(registrationLatch.await(5, TimeUnit.SECONDS)).as("Event registration process did not complete in timely fashion").isTrue();

            // Produce a connect and disconnect event
            //eventGenerator.start();
            eventGenerator.produceConnectDisconnectEvents();

            // Assert that both events have been received
            assertThat(messagesReceivedLatch.await(5, TimeUnit.SECONDS)).as("Timeout while waiting for messages from server.").isTrue();
            assertThat(webSocket.getReceivedMessages()).hasSize(1); // Single message that confirms the registration
        } finally {
            shutdown();
        }
    }

    private class EventGenerator extends EventPublisherImpl {
        private Device device;
        private OutboundConnectionTask connectionTask;
        private OutboundComPort comPort;
        private OutboundComPortPool comPortPool;

        private EventGenerator() {
            super(runningComServer, filteringEventReceiverFactory);
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

}