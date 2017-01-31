/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.time.Duration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BytesMessageBuilderTest {

    private static final String MESSAGE = "message";
    private static final String DESTINATION_NAME = "destinationName";

    @Mock
    private DestinationSpec destination;
    @Mock
    private AQFacade aqFacade;
    @Mock
    private AQMessage aqMessage;
    @Mock
    private OracleConnection connection;
    @Mock
    private Publisher publisher;
    @Mock
    private AQMessageProperties aqMessageProperties;
    @Mock
    private DataModel dataModel;

    @Before
    public void setUp() throws SQLException {
        when(aqFacade.create(any(AQMessageProperties.class))).thenReturn(aqMessage);
        when(connection.unwrap(any())).thenReturn(connection);
        when(destination.getName()).thenReturn(DESTINATION_NAME);
        when(aqFacade.createAQMessageProperties()).thenReturn(aqMessageProperties);
        when(dataModel.getConnection(false)).thenReturn(connection);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testNormalSend() throws SQLException {
        new BytesMessageBuilder(dataModel, aqFacade, publisher, destination, MESSAGE.getBytes()).send();

        verify(aqMessage).setPayload(MESSAGE.getBytes());
        verify(connection).enqueue(eq(DESTINATION_NAME), any(AQEnqueueOptions.class), eq(aqMessage));
    }

    @Test
    public void testExpiringSend() throws SQLException {
        new BytesMessageBuilder(dataModel, aqFacade, publisher, destination, MESSAGE.getBytes())
                .expiringAfter(Duration.ofSeconds(60))
                .send();

        verify(aqMessage).setPayload(MESSAGE.getBytes());
        verify(aqMessageProperties).setExpiration(60);
        verify(connection).enqueue(eq(DESTINATION_NAME), any(AQEnqueueOptions.class), eq(aqMessage));
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testExpiringSendThatFailsWithSqlException() throws SQLException {
        doThrow(SQLException.class).when(aqMessageProperties).setExpiration(anyInt());

        new BytesMessageBuilder(dataModel, aqFacade, publisher, destination, MESSAGE.getBytes())
                .expiringAfter(Duration.ofSeconds(60))
                .send();
   }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testSendingFailsWithSQLException() throws SQLException {
        doThrow(SQLException.class).when(connection).enqueue(anyString(), any(AQEnqueueOptions.class), any(AQMessage.class));

        BytesMessageBuilder bytesMessageBuilder = new BytesMessageBuilder(dataModel, aqFacade, publisher, destination, MESSAGE.getBytes());

        bytesMessageBuilder.send();

        verify(aqMessage).setPayload(MESSAGE.getBytes());
        verify(connection).enqueue(eq(DESTINATION_NAME), any(AQEnqueueOptions.class), eq(aqMessage));
    }




}
