/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscriberSpecImplTest {

    private static final String NAME = "Name";
    private static final String DESTINATION = "Destiny";
    private static final String PAYLOAD_TYPE = "RAW";
    private static final byte[] PAYLOAD_BYTES = "Payload".getBytes();
    private SubscriberSpecImpl subscriberSpec;

    @Mock
    private DestinationSpec destination;
    @Mock
    private OracleConnection connection;
    @Mock
    private AQMessage message;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private DataModel dataModel;
    @Mock
    private NlsService nlsService;

    @Before
    public void setUp() throws SQLException {

        when(destination.getName()).thenReturn(DESTINATION);
        when(destination.getPayloadType()).thenReturn(PAYLOAD_TYPE);
        when(message.getPayload()).thenReturn(PAYLOAD_BYTES);
        when(connection.unwrap(any())).thenReturn(connection);
        when(destination.isTopic()).thenReturn(true);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(dataModel.getInstance(SubscriberSpecImpl.class)).thenReturn(new SubscriberSpecImpl(dataModel, nlsService));
        when(dataModel.getConnection(false)).thenReturn(connection);

        subscriberSpec = SubscriberSpecImpl.from(dataModel, destination, NAME, "TST", Layer.DOMAIN);
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
        when(connection.dequeue(eq(DESTINATION), any(AQDequeueOptions.class), eq(PAYLOAD_TYPE))).thenReturn(message);

        Message received = subscriberSpec.receive();

        assertThat(received.getPayload()).isEqualTo(message.getPayload());
    }

    @Test(timeout = 2000)
    public void testCancelBreaksOutOfABlockingReceive() throws Exception {
        mockConnectionBlockingOnEmptyQueue();

        RunnableFuture<Message> task = new FutureTask<>(new Callable<Message>(){
            @Override
            public Message call() {
                return subscriberSpec.receive();
            }
        });
        Thread receiver = new Thread(task);
        receiver.start();
        while (Thread.State.BLOCKED != receiver.getState() && Thread.State.WAITING != receiver.getState()) {
            Thread.yield();  // spin wait until receive is blocking, yielding may improve the chance that the other thread gets there faster
        }

        subscriberSpec.cancel();

        verify(connection).cancel();

        assertThat(task.get(1, TimeUnit.SECONDS)).isNull();

        verify(connection).close();
    }

    private void mockConnectionBlockingOnEmptyQueue() throws SQLException {
        final CountDownLatch canceled = new CountDownLatch(1);

        when(connection.dequeue(eq(DESTINATION), any(AQDequeueOptions.class), eq(PAYLOAD_TYPE))).thenAnswer(new Answer<Object>() {
            @Override
            public Message answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (((AQDequeueOptions) invocationOnMock.getArguments()[1]).getWait() == 0) {
                    throw new SQLTimeoutException();
                }
                boolean keepBlocking = true;
                while (keepBlocking) {
                    try {
                        canceled.await();
                        keepBlocking = false;
                    } catch (InterruptedException e) {
                        // simulating blocking IO, so ignoring interruption
                    }
                }
                throw new SQLTimeoutException();
            }
        });
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                canceled.countDown();
                return null;
            }
        }).when(connection).cancel();
    }

    @Test
    public void testSubscribe() throws SQLException {

        subscriberSpec.subscribe();

        verify(preparedStatement).execute();
    }

    @Test
    public void testUnsubscribe() throws SQLException {

        subscriberSpec.unSubscribe();

        verify(preparedStatement).execute();
    }

    @Test(timeout = 2000)
    public void testReceiveNoWait() throws SQLException {
        mockConnectionBlockingOnEmptyQueue();

        assertThat(subscriberSpec.receiveNow()).isNull();
    }


}
