/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;

import oracle.jdbc.OracleConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(messageService.getQueueTableSpec(QTS).get()).isEqualTo(queueTableSpec);
    }

    @Test
    public void testGetQueueTableSpecNotExists() {
        assertThat(messageService.getQueueTableSpec(QTS).isPresent()).isFalse();
    }

    @Test
    public void testGetDestinationSpec() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);
        destination = queueTableSpec.createDestinationSpec(DESTINATION, 0);

        assertThat(messageService.getDestinationSpec(DESTINATION).get()).isEqualTo(destination);
    }

    @Test
    public void testGetDestinationSpecNotExists() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);

        assertThat(messageService.getDestinationSpec(DESTINATION).isPresent()).isFalse();
    }

    @Test
    public void testGetSubscriberSpec() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);
        destination = queueTableSpec.createDestinationSpec(DESTINATION, 0);
        destination.activate();
        subscriberSpec = destination.subscribe(new SimpleTranslationKey(SUBSCRIBER, SUBSCRIBER), "TST", Layer.DOMAIN);

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).get()).isEqualTo(subscriberSpec);
    }

    @Test
    public void testGetSubscriberSpecNotExists() {
        queueTableSpec = messageService.createQueueTableSpec(QTS, "raw", true);
        destination = queueTableSpec.createDestinationSpec(DESTINATION, 0);

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).isPresent()).isFalse();
    }

}