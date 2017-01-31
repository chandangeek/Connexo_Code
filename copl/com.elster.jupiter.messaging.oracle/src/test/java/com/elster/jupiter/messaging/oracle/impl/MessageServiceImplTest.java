/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.upgrade.UpgradeService;

import oracle.jdbc.OracleConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceImplTest {

    private static final String QTS = "QTS";
    private static final String RAW = "RAW";
    private static final boolean MULTI_CONSUMER = true;
    private static final String DESTINATION = "DESTINATION";
    private static final String SUBSCRIBER = "subscriber";

    private MessageService messageService;

    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<QueueTableSpec> queueTableSpecFactory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private OracleConnection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private QueueTableSpec queueTableSpec;
    private MessageServiceImpl service;
    @Mock
    private DataMapper<DestinationSpec> destinationSpecFactory;
    @Mock
    private DestinationSpec destination;
    @Mock
    private DataMapper<SubscriberSpec> subscriberSpecFactory;
    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private UpgradeService upgradeService;
    @Mock
    private AQFacade aqFacade;

    @Before
    public void setUp() throws SQLException {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.mapper(QueueTableSpec.class)).thenReturn(queueTableSpecFactory);
        when(dataModel.mapper(DestinationSpec.class)).thenReturn(destinationSpecFactory);
        when(dataModel.mapper(SubscriberSpec.class)).thenReturn(subscriberSpecFactory);
        when(dataModel.addTable(anyString(),any())).thenReturn(table);
        when(dataModel.getConnection(anyBoolean())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(dataModel.getInstance(QueueTableSpecImpl.class)).thenReturn(new QueueTableSpecImpl(dataModel, aqFacade, thesaurus));

        service = new MessageServiceImpl();
        service.setOrmService(ormService);
        service.setUpgradeService(upgradeService);
        service.setAqFacade(aqFacade);
        service.activate();

        messageService = service;

    }

    @After
    public void tearDown() {
        service.deactivate();
    }

    @Test
    public void testCreateQueueTableSpec() {
        QueueTableSpec queueTableSpec = messageService.createQueueTableSpec(QTS, RAW, MULTI_CONSUMER);

        assertThat(queueTableSpec).isNotNull();
        assertThat(queueTableSpec.getName()).isEqualTo(QTS);
        assertThat(queueTableSpec.getPayloadType()).isEqualTo(RAW);
        assertThat(queueTableSpec.isMultiConsumer()).isTrue();
        verify(queueTableSpecFactory).persist(queueTableSpec);
    }

    @Test
    public void testGetQueueTableSpec() {
        when(queueTableSpecFactory.getOptional(QTS)).thenReturn(Optional.of(queueTableSpec));

        assertThat(messageService.getQueueTableSpec(QTS).get()).isEqualTo(queueTableSpec);
    }

    @Test
    public void testGetQueueTableSpecNotExists() {
        when(queueTableSpecFactory.getOptional(QTS)).thenReturn(Optional.empty());

        assertThat(messageService.getQueueTableSpec(QTS).isPresent()).isFalse();
    }

    @Test
    public void testGetDestinationSpec() {
        when(destinationSpecFactory.getOptional(DESTINATION)).thenReturn(Optional.of(destination));

        assertThat(messageService.getDestinationSpec(DESTINATION).get()).isEqualTo(destination);
    }

    @Test
    public void testGetDestinationSpecNotExists() {
        when(destinationSpecFactory.getOptional(DESTINATION)).thenReturn(Optional.empty());

        assertThat(messageService.getDestinationSpec(DESTINATION).isPresent()).isFalse();
    }

    @Test
    public void testGetSubscriberSpec() {
        when(subscriberSpecFactory.getOptional(DESTINATION, SUBSCRIBER)).thenReturn(Optional.of(subscriberSpec));

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).get()).isEqualTo(subscriberSpec);
    }

    @Test
    public void testGetSubscriberSpecNotExists() {
        when(subscriberSpecFactory.getOptional(DESTINATION, SUBSCRIBER)).thenReturn(Optional.empty());

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).isPresent()).isFalse();
    }

}
