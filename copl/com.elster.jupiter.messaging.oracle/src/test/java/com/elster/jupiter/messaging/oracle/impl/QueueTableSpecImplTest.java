/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.UnderlyingJmsException;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.MessageSeed;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    private OracleConnection connection;
    @Mock
    private AQFacade aqFacade;
    @Mock
    private QueueConnection queueConnection;
    @Mock
    private AQjmsSession aqJmsSession;
    @Mock
    private DataMapper<QueueTableSpec> queueTableSpecFactory;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat nlsMessageFormat;

    @Before
    public void setUp() throws SQLException, JMSException {
        when(dataModel.getInstance(QueueTableSpecImpl.class)).thenReturn(new QueueTableSpecImpl(dataModel, aqFacade, thesaurus));
        when(dataModel.getConnection(false)).thenReturn(connection);
        when(dataModel.mapper(QueueTableSpec.class)).thenReturn(queueTableSpecFactory);
        queueTableSpec = QueueTableSpecImpl.from(dataModel, NAME, PAYLOAD_TYPE, MULTI_CONSUMER);
        when(aqFacade.createQueueConnection(connection)).thenReturn(queueConnection);
        when(queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(aqJmsSession);
        when(connection.unwrap(any())).thenReturn(connection);
        when(connection.isWrapperFor(OracleConnection.class)).thenReturn(true);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
    }

    @After
    public void tearDown() {
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
