/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableList;
import oracle.AQ.AQQueueTable;
import oracle.jdbc.OracleConnection;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsSession;

import javax.jms.QueueConnection;
import javax.jms.Session;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DestinationSpecImplTest {

    private static final String NAME = "name";
    private static final int RETRY_DELAY = 60;
    private static final String RAW = "RAW";
    private static final String SUBSCRIBER = "SUBSCRIBER";
    private static final String QUEUE_TABLE_NAME = "QUEUE_TABLE_NAME";
    private static final int RETRIES = 4;

    private DestinationSpecImpl destinationSpec;

    @Mock
    private QueueTableSpecImpl queueTableSpec;
    @Mock
    private DataMapper<SubscriberSpec> subscriberSpecFactory;
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
    private SubscriberSpec subscriber1, subscriber2;
    @Mock
    private SubscriberSpec subscriber;
    @Mock
    private DataModel dataModel;
    @Mock
    private Publisher publisher;
    @Mock
    private DataMapper<DestinationSpec> destinationSpecFactory;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat nlsMessageFormat;

    @Before
    public void setUp() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(queueTableSpec.getName()).thenReturn(QUEUE_TABLE_NAME);
        when(queueTableSpec.isMultiConsumer()).thenReturn(true);
        when(connection.unwrap(any())).thenReturn(connection);
        when(connection.isWrapperFor(any(Class.class))).thenReturn(true);
        when(subscriber.getName()).thenReturn(SUBSCRIBER);
        when(dataModel.getConnection(false)).thenReturn(connection);
        when(dataModel.getInstance(DestinationSpecImpl.class)).thenReturn(new DestinationSpecImpl(dataModel, aqFacade, publisher, thesaurus));
        when(dataModel.getInstance(SubscriberSpecImpl.class)).thenReturn(new SubscriberSpecImpl(dataModel, nlsService));
        when(dataModel.mapper(SubscriberSpec.class)).thenReturn(subscriberSpecFactory);
        when(dataModel.mapper(DestinationSpec.class)).thenReturn(destinationSpecFactory);
        when(aqFacade.createQueueConnection(connection)).thenReturn(queueConnection);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);

        destinationSpec = DestinationSpecImpl.from(dataModel, queueTableSpec, NAME, RETRY_DELAY, RETRIES, false);
    }

    @After
    public void tearDown() {
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
        destinationSpec.subscribe(new SimpleTranslationKey(SUBSCRIBER, SUBSCRIBER), "TST", Layer.DOMAIN);
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
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);
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

        destinationSpec.activate();
        subscriber1 = destinationSpec.subscribe(new SimpleTranslationKey("1", "One"), "TST", Layer.DOMAIN);
        subscriber2 = destinationSpec.subscribe(new SimpleTranslationKey("2", "Two"), "TST", Layer.DOMAIN);

        ImmutableList<SubscriberSpec> subscribers = ImmutableList.of(subscriber1, subscriber2);

        assertThat(destinationSpec.getSubscribers()).isEqualTo(subscribers);
    }

    @Test(expected = DuplicateSubscriberNameException.class)
    public void testSubscribeDuplicate() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        destinationSpec.subscribe(new SimpleTranslationKey(SUBSCRIBER, SUBSCRIBER), "TST", Layer.DOMAIN);
        destinationSpec.subscribe(new SimpleTranslationKey(SUBSCRIBER, SUBSCRIBER), "TST", Layer.DOMAIN);
    }

    @Test(expected = AlreadyASubscriberForQueueException.class)
    public void testSubscribeMultipleOnQueue() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        destinationSpec.subscribe(new SimpleTranslationKey("destination", "destination"), "TST", Layer.DOMAIN);
        destinationSpec.subscribe(new SimpleTranslationKey(SUBSCRIBER + "2", SUBSCRIBER + "2"), "TST", Layer.DOMAIN);
    }

    @Test
    public void testSubscribeSuccess() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);
        when(subscriberSpecFactory.find("destination", destinationSpec)).thenReturn(ImmutableList.<SubscriberSpec>of());

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        Mockito.reset(preparedStatement); // clear interactions that may have occurred in activate

        SubscriberSpec subscriberSpec = destinationSpec.subscribe(new SimpleTranslationKey(SUBSCRIBER, SUBSCRIBER), "TST", Layer.DOMAIN);

        assertThat(subscriberSpec.getName()).isEqualTo(SUBSCRIBER);
        assertThat(subscriberSpec.getDestination()).isEqualTo(destinationSpec);
    }

}