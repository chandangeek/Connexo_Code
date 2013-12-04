package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import oracle.jdbc.OracleConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceImplTest {

    private static final String QTS = "QTS";
    private static final String RAW = "RAW";
    private static final boolean MULTI_CONSUMER = true;
    private static final String DESTINATION = "DESTINATION";
    private static final String SUBSCRIBER = "subscriber";

    private MessageService messageService;
    private TransientMessageService service;

    @Mock
    private OracleConnection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    private DestinationSpec destination;
    @Mock
    private SubscriberSpec subscriberSpec;

    @Before
    public void setUp() throws SQLException {


        service = new TransientMessageService();

        messageService = service;

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateQueueTableSpec() {
        QueueTableSpec queueTableSpec = messageService.createQueueTableSpec(QTS, RAW, MULTI_CONSUMER);

        assertThat(queueTableSpec).isNotNull();
        assertThat(queueTableSpec.getName()).isEqualTo(QTS);
        assertThat(queueTableSpec.getPayloadType()).isEqualTo(RAW);
        assertThat(queueTableSpec.isMultiConsumer()).isTrue();
    }

    @Test
    public void testGetQueueTableSpec() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);

        assertThat(messageService.getQueueTableSpec(QTS)).contains(queueTableSpec);
    }

    @Test
    public void testGetQueueTableSpecNotExists() {
        assertThat(messageService.getQueueTableSpec(QTS)).isAbsent();
    }

    @Test
    public void testGetDestinationSpec() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);
        destination = queueTableSpec.createDestinationSpec(DESTINATION, 0);

        assertThat(messageService.getDestinationSpec(DESTINATION)).contains(destination);
    }

    @Test
    public void testGetDestinationSpecNotExists() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);

        assertThat(messageService.getDestinationSpec(DESTINATION)).isAbsent();
    }

    @Test
    public void testGetSubscriberSpec() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);
        destination = queueTableSpec.createDestinationSpec(DESTINATION, 0);
        destination.activate();
        subscriberSpec = destination.subscribe(SUBSCRIBER);

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER)).contains(subscriberSpec);
    }

    @Test
    public void testGetSubscriberSpecNotExists() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);
        destination = queueTableSpec.createDestinationSpec(DESTINATION, 0);

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER)).isAbsent();
    }

}
