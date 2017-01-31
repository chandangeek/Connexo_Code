/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.Message;

import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscriberSpecImplTest {

    private static final String NAME = "Name";
    private static final String DISPLAYNAME = "Displayname";
    private static final String DESTINATION = "Destiny";
    private static final String PAYLOAD_TYPE = "RAW";
    private static final byte[] PAYLOAD_BYTES = "Payload".getBytes();
    private TransientSubscriberSpec subscriberSpec;

    @Mock
    private TransientDestinationSpec destination;
    @Mock
    private AQMessage message;

    @Before
    public void setUp() throws SQLException {

        when(destination.getName()).thenReturn(DESTINATION);
        when(destination.getPayloadType()).thenReturn(PAYLOAD_TYPE);
        when(message.getPayload()).thenReturn(PAYLOAD_BYTES);
        when(destination.isTopic()).thenReturn(true);

        subscriberSpec = new TransientSubscriberSpec(destination, NAME, DISPLAYNAME);
    }

    @Test
    public void testGetDestination() {
        assertThat(subscriberSpec.getDestination()).isEqualTo(destination);
    }

    @Test
    public void testGetName() {
        assertThat(subscriberSpec.getName()).isEqualTo(NAME);
    }

    @Test
    public void testReceive() throws SQLException {
        subscriberSpec.addMessage(new TransientMessage(PAYLOAD_BYTES));

        Message received = subscriberSpec.receive();

        assertThat(received.getPayload()).isEqualTo(PAYLOAD_BYTES);
    }

    @Test(timeout = 2000)
    public void testCancelBreaksOutOfABlockingReceive() throws Exception {

        RunnableFuture<Message> task = new FutureTask<>(() -> subscriberSpec.receive());
        Thread receiver = new Thread(task);
        receiver.start();
        while (Thread.State.BLOCKED != receiver.getState() && Thread.State.WAITING != receiver.getState()) {
            Thread.yield();  // spin wait until receive is blocking, yielding may improve the chance that the other thread gets there faster
        }

        subscriberSpec.cancel();

        assertThat(task.get(1, TimeUnit.SECONDS)).isNull();
    }

}