package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;
import org.joda.time.Seconds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

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
    private ServiceLocator serviceLocator;
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

    @Before
    public void setUp() throws SQLException {
        Bus.setServiceLocator(serviceLocator);
        when(serviceLocator.getAQFacade()).thenReturn(aqFacade);
        when(aqFacade.create(any(AQMessageProperties.class))).thenReturn(aqMessage);
        when(serviceLocator.getConnection()).thenReturn(connection);
        when(connection.unwrap(any(Class.class))).thenReturn(connection);
        when(serviceLocator.getPublisher()).thenReturn(publisher);
        when(destination.getName()).thenReturn(DESTINATION_NAME);
        when(aqFacade.createAQMessageProperties()).thenReturn(aqMessageProperties);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testNormalSend() throws SQLException {
        new BytesMessageBuilder(destination, MESSAGE.getBytes())
                .send();

        verify(aqMessage).setPayload(MESSAGE.getBytes());
        verify(connection).enqueue(eq(DESTINATION_NAME), any(AQEnqueueOptions.class), eq(aqMessage));
    }

    @Test
    public void testExpiringSend() throws SQLException {
        new BytesMessageBuilder(destination, MESSAGE.getBytes())
                .expiringAfter(Seconds.seconds(60))
                .send();

        verify(aqMessage).setPayload(MESSAGE.getBytes());
        verify(aqMessageProperties).setExpiration(60);
        verify(connection).enqueue(eq(DESTINATION_NAME), any(AQEnqueueOptions.class), eq(aqMessage));
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testExpiringSendThatFailsWithSqlException() throws SQLException {
        doThrow(SQLException.class).when(aqMessageProperties).setExpiration(anyInt());

        new BytesMessageBuilder(destination, MESSAGE.getBytes())
                .expiringAfter(Seconds.seconds(60))
                .send();
   }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testSendingFailsWithSQLException() throws SQLException {
        doThrow(SQLException.class).when(connection).enqueue(anyString(), any(AQEnqueueOptions.class), any(AQMessage.class));

        BytesMessageBuilder bytesMessageBuilder = new BytesMessageBuilder(destination, MESSAGE.getBytes());

        bytesMessageBuilder.send();

        verify(aqMessage).setPayload(MESSAGE.getBytes());
        verify(connection).enqueue(eq(DESTINATION_NAME), any(AQEnqueueOptions.class), eq(aqMessage));
    }




}
