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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscriberSpecImplConcurrencyTest {

    private static final String NAME = "Name";
    private static final String DESTINATION = "Destiny";
    private static final String PAYLOAD_TYPE = "RAW";
    private static final byte[] PAYLOAD_BYTES = "Payload".getBytes();
    private SubscriberSpecImpl subscriberSpec;

    @Mock
    private DestinationSpec destination;
    @Mock
    private OracleConnection connection1, connection2, connection3, connection4;
    @Mock
    private AQMessage message;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private DataModel dataModel;
    @Mock
    private NlsService nlsService;
    private volatile CountDownLatch allThreadsBlocking;
    private AtomicInteger cancelCounter = new AtomicInteger();

    @Before
    public void setUp() throws SQLException {

        when(destination.getName()).thenReturn(DESTINATION);
        when(destination.getPayloadType()).thenReturn(PAYLOAD_TYPE);
        when(message.getPayload()).thenReturn(PAYLOAD_BYTES);
        when(destination.isTopic()).thenReturn(true);
        when(dataModel.getInstance(SubscriberSpecImpl.class)).thenReturn(new SubscriberSpecImpl(dataModel, nlsService));
        when(dataModel.getConnection(false)).thenReturn(connection1, connection2, connection3, connection4);

        subscriberSpec = SubscriberSpecImpl.from(dataModel, destination, NAME, "TST", Layer.DOMAIN);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMultipleTryToReceive() throws SQLException, InterruptedException {
        allThreadsBlocking = new CountDownLatch(4);
        mockConnectionBlockingOnEmptyQueue(connection1);
        mockConnectionBlockingOnEmptyQueue(connection2);
        mockConnectionBlockingOnEmptyQueue(connection3);
        mockConnectionBlockingOnEmptyQueue(connection4);

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            executorService.submit((Runnable) () -> subscriberSpec.receive());
        }
        executorService.shutdown();
        allThreadsBlocking.await(2, TimeUnit.SECONDS);
        subscriberSpec.cancel();
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        assertThat(cancelCounter.get()).isEqualTo(4);
        verify(connection1, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
        verify(connection2, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
        verify(connection3, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
        verify(connection4, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
    }

    @Test
    public void testMultipleCancels() throws SQLException, InterruptedException {
        allThreadsBlocking = new CountDownLatch(4);
        mockConnectionBlockingOnEmptyQueue(connection1);
        mockConnectionBlockingOnEmptyQueue(connection2);
        mockConnectionBlockingOnEmptyQueue(connection3);
        mockConnectionBlockingOnEmptyQueue(connection4);

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            executorService.submit((Runnable) () -> subscriberSpec.receive());
        }
        executorService.shutdown();
        allThreadsBlocking.await(2, TimeUnit.SECONDS);
        subscriberSpec.cancel();
        subscriberSpec.cancel();
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        assertThat(cancelCounter.get()).isEqualTo(4);
        verify(connection1, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
        verify(connection2, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
        verify(connection3, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
        verify(connection4, times(1)).dequeue(anyString(), any(AQDequeueOptions.class), anyString());
    }

    private void mockConnectionBlockingOnEmptyQueue(OracleConnection connection) throws SQLException {
        final CountDownLatch canceled = new CountDownLatch(1);

        when(connection.unwrap(any())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.dequeue(eq(DESTINATION), any(AQDequeueOptions.class), eq(PAYLOAD_TYPE))).thenAnswer(new Answer<Object>() {
            @Override
            public Message answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (((AQDequeueOptions) invocationOnMock.getArguments()[1]).getWait() == 0) {
                    throw new SQLTimeoutException();
                }
                boolean keepBlocking = true;
                if (allThreadsBlocking != null) {
                    allThreadsBlocking.countDown();
                }
                while (keepBlocking) {
                    try {
                        canceled.await();
                        keepBlocking = false;
                    } catch (InterruptedException e) {
                        // simulating blocking IO, so ignoring interruption
                    }
                }
                cancelCounter.incrementAndGet();
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


}
