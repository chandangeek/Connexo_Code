package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.UnderlyingJmsException;
import com.elster.jupiter.orm.DataMapper;
import oracle.AQ.AQException;
import oracle.AQ.AQQueueTableProperty;
import oracle.jdbc.OracleConnection;
import oracle.jms.AQjmsSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueTableSpecImplTest {

    private static final String NAME = "name";
    private static final String PAYLOAD_TYPE = "SYS.AQ$_JMS_RAW";
    private static final boolean MULTI_CONSUMER = false;

    private QueueTableSpecImpl queueTableSpec;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private OracleConnection connection;
    @Mock
    private AQFacade aqFacade;
    @Mock
    private QueueConnection queueConnection;
    @Mock
    private AQjmsSession aqJmsSession;
    @Mock
    private OrmClient ormClient;
    @Mock
    private DataMapper<QueueTableSpec> queueTableSpecFactory;
    @Mock
    private PreparedStatement preparedStatement;

    @Before
    public void setUp() throws SQLException, JMSException {
        queueTableSpec = new QueueTableSpecImpl(NAME, PAYLOAD_TYPE, MULTI_CONSUMER);
        when(serviceLocator.getConnection()).thenReturn(connection);
        when(serviceLocator.getAQFacade()).thenReturn(aqFacade);
        when(aqFacade.createQueueConnection(connection)).thenReturn(queueConnection);
        when(queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(aqJmsSession);
        when(connection.unwrap(any(Class.class))).thenReturn(connection);
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getQueueTableSpecFactory()).thenReturn(queueTableSpecFactory);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testGetName() {
        assertThat(queueTableSpec.getName()).isEqualTo(NAME);
    }

    @Test
    public void testActivateJms() throws JMSException, AQException {
        queueTableSpec.activate();

        ArgumentCaptor<AQQueueTableProperty> propertyCaptor = ArgumentCaptor.forClass(AQQueueTableProperty.class);

        InOrder inOrder = inOrder(queueConnection, aqJmsSession);
        inOrder.verify(queueConnection).start();
        inOrder.verify(aqJmsSession).createQueueTable(eq("kore"), eq(NAME), propertyCaptor.capture());

        assertThat(propertyCaptor.getValue()).isNotNull();
        assertThat(propertyCaptor.getValue().getPayloadType()).isEqualTo(PAYLOAD_TYPE);
        assertThat(queueTableSpec.isActive()).isTrue();
    }

    @Test
    public void testActivateSecondTimeHasNoEffect() throws JMSException, AQException {
        queueTableSpec.activate();
        queueTableSpec.activate();

        verify(aqJmsSession, atMost(1)).createQueueTable(eq("kore"), eq(NAME), any(AQQueueTableProperty.class));
    }

    @Test(expected = UnderlyingJmsException.class)
    public void testActivateJmsWithJmsException() throws JMSException, AQException {
        doThrow(JMSException.class).when(aqFacade).createQueueConnection(connection);

        queueTableSpec.activate();
    }

    @Test
    public void testDeactivate() throws SQLException {
        queueTableSpec.activate();

        assertThat(queueTableSpec.isActive()).isTrue();

        Mockito.reset(preparedStatement); // clear interactions that may have occurred in activate

        queueTableSpec.deactivate();

        verify(preparedStatement, atLeast(1)).setString(anyInt(), eq(NAME));
        verify(preparedStatement).execute();

        assertThat(queueTableSpec.isActive()).isFalse();

    }

}
