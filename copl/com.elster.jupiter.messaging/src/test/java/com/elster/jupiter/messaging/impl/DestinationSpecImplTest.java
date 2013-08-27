package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.google.common.collect.ImmutableList;
import oracle.AQ.AQQueueTable;
import oracle.jdbc.OracleConnection;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.QueueConnection;
import javax.jms.Session;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DestinationSpecImplTest {

    private static final String NAME = "name";
    private static final int RETRY_DELAY = 60;
    private static final String RAW = "RAW";
    private static final String SUBSCRIBER = "SUBSCRIBER";
    private static final String QUEUE_TABLE_NAME = "QUEUE_TABLE_NAME";

    private DestinationSpecImpl destinationSpec;

    @Mock
    private QueueTableSpecImpl queueTableSpec;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private DataMapper<SubscriberSpec> consumerSpecFactory;
    @Mock
    private OracleConnection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private AQFacade aqFacade;
    @Mock
    private QueueConnection queueConnection;
    @Mock
    private AQQueueTable aqTable;
    @Mock
    private AQjmsSession queueSession;
    @Mock
    private AQjmsDestination destination;
    @Mock
    private SubscriberSpec consumer1, consumer2;
    @Mock
    private SubscriberSpec subscriber;

    @Before
    public void setUp() throws Exception {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getOrmClient().getConsumerSpecFactory()).thenReturn(consumerSpecFactory);
        when(serviceLocator.getConnection()).thenReturn(connection);
        when(serviceLocator.getAQFacade().createQueueConnection(connection)).thenReturn(queueConnection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(queueTableSpec.getName()).thenReturn(QUEUE_TABLE_NAME);
        when(connection.unwrap(any(Class.class))).thenReturn(connection);
        when(subscriber.getName()).thenReturn(SUBSCRIBER);

        destinationSpec = new DestinationSpecImpl(queueTableSpec, NAME, RETRY_DELAY);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testConstruction() {
        assertThat(destinationSpec.getName()).isEqualTo(NAME);
        assertThat(destinationSpec.isActive()).isFalse();
        assertThat(destinationSpec.getQueueTableSpec()).isEqualTo(queueTableSpec);
    }

    @Test
    public void testIsTopicDelegatesToQueueTableSpec() {
        when(queueTableSpec.isMultiConsumer()).thenReturn(true, false);

        assertThat(destinationSpec.isTopic()).isTrue();
        assertThat(destinationSpec.isTopic()).isFalse();
    }

    @Test
    public void testIsQueueDelegatesToQueueTableSpec() {
        when(queueTableSpec.isMultiConsumer()).thenReturn(true, false);

        assertThat(destinationSpec.isQueue()).isFalse();
        assertThat(destinationSpec.isQueue()).isTrue();
    }

    @Test
    public void testGetPayloadTypeDelegatesToQueueTableSpec() {
        when(queueTableSpec.getPayloadType()).thenReturn(RAW);

        assertThat(destinationSpec.getPayloadType()).isEqualTo(RAW);
    }

    @Test(expected = InactiveDestinationException.class)
    public void testSubscribeOnInactiveDestinationThrowsException() {
        destinationSpec.subscribe(SUBSCRIBER, 2);
    }

    @Test
    public void testActivationOnAQ() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);

        destinationSpec.activate();

        verify(preparedStatement, atLeast(1)).setString(anyInt(), eq(NAME));
        verify(preparedStatement).setString(anyInt(), eq(QUEUE_TABLE_NAME));
        verify(preparedStatement).setInt(anyInt(), eq(RETRY_DELAY));
        verify(preparedStatement).execute();
        assertThat(destinationSpec.isActive()).isTrue();
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testActivationOnAQQThrowsSQLException() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        doThrow(SQLException.class).when(preparedStatement).execute();

        destinationSpec.activate();
    }

    @Test
    public void testActivationOnJmsForQueue() throws Exception {
        when(queueTableSpec.isJms()).thenReturn(true);
        when(queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(queueSession);
        when(queueTableSpec.getAqQueueTable(queueSession)).thenReturn(aqTable);
        when(queueSession.createQueue(eq(aqTable), eq(NAME), any(AQjmsDestinationProperty.class))).thenReturn(destination);

        destinationSpec.activate();

        InOrder inOrder = inOrder(queueConnection, destination);
        inOrder.verify(queueConnection).start();
        inOrder.verify(destination).start(queueSession, true, true);
        inOrder.verify(queueConnection).close();
    }

    @Test
    public void testDeactivate() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        Mockito.reset(preparedStatement); // clear interactions that may have occurred in activate

        destinationSpec.deactivate();

        verify(preparedStatement, atLeast(1)).setString(anyInt(), eq(NAME));
        verify(preparedStatement).execute();

        assertThat(destinationSpec.isActive()).isFalse();
    }

    @Test
    public void testGetConsumers() {
        ImmutableList<SubscriberSpec> subscribers = ImmutableList.of(consumer1, consumer2);

        when(consumerSpecFactory.find("destination", destinationSpec)).thenReturn(subscribers);

        assertThat(destinationSpec.getConsumers()).isEqualTo(subscribers);
    }

    @Test(expected = DuplicateSubscriberNameException.class)
    public void testSubscribeDuplicate() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(consumerSpecFactory.find("destination", destinationSpec)).thenReturn(ImmutableList.of(subscriber));

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        Mockito.reset(preparedStatement); // clear interactions that may have occurred in activate

        destinationSpec.subscribe(SUBSCRIBER, 3);
    }

    @Test(expected = AlreadyASubscriberForQueueException.class)
    public void testSubscribeMultipleOnQueue() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);
        when(consumerSpecFactory.find("destination", destinationSpec)).thenReturn(ImmutableList.of(subscriber));

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        Mockito.reset(preparedStatement); // clear interactions that may have occurred in activate

        destinationSpec.subscribe(SUBSCRIBER + "2", 3);
    }

    @Test
    public void testSubscribeSuccess() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);
        when(consumerSpecFactory.find("destination", destinationSpec)).thenReturn(ImmutableList.<SubscriberSpec>of());

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        Mockito.reset(preparedStatement); // clear interactions that may have occurred in activate

        SubscriberSpec subscriberSpec = destinationSpec.subscribe(SUBSCRIBER, 3);

        verify(consumerSpecFactory).persist(subscriberSpec);

        assertThat(subscriberSpec.getName()).isEqualTo(SUBSCRIBER);
        assertThat(subscriberSpec.getDestination()).isEqualTo(destinationSpec);
    }


}
