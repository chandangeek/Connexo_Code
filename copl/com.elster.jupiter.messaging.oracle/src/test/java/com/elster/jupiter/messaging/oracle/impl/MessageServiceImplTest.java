package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.google.common.base.Optional;
import oracle.jdbc.OracleConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
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

    @Before
    public void setUp() throws SQLException {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.mapper(QueueTableSpec.class)).thenReturn(queueTableSpecFactory);
        when(dataModel.mapper(DestinationSpec.class)).thenReturn(destinationSpecFactory);
        when(dataModel.mapper(SubscriberSpec.class)).thenReturn(subscriberSpecFactory);
        when(dataModel.addTable(anyString(),any(Class.class))).thenReturn(table);
        when(dataModel.getConnection(anyBoolean())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        service = new MessageServiceImpl();
        service.setOrmService(ormService);
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
        when(queueTableSpecFactory.get(QTS)).thenReturn(Optional.of(queueTableSpec));

        assertThat(messageService.getQueueTableSpec(QTS)).contains(queueTableSpec);
    }

    @Test
    public void testGetQueueTableSpecNotExists() {
        when(queueTableSpecFactory.get(QTS)).thenReturn(Optional.<QueueTableSpec>absent());

        assertThat(messageService.getQueueTableSpec(QTS)).isAbsent();
    }

    @Test
    public void testGetDestinationSpec() {
        when(destinationSpecFactory.get(DESTINATION)).thenReturn(Optional.of(destination));

        assertThat(messageService.getDestinationSpec(DESTINATION)).contains(destination);
    }

    @Test
    public void testGetDestinationSpecNotExists() {
        when(destinationSpecFactory.get(DESTINATION)).thenReturn(Optional.<DestinationSpec>absent());

        assertThat(messageService.getDestinationSpec(DESTINATION)).isAbsent();
    }

    @Test
    public void testGetSubscriberSpec() {
        when(subscriberSpecFactory.get(DESTINATION, SUBSCRIBER)).thenReturn(Optional.of(subscriberSpec));

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER)).contains(subscriberSpec);
    }

    @Test
    public void testGetSubscriberSpecNotExists() {
        when(subscriberSpecFactory.get(DESTINATION, SUBSCRIBER)).thenReturn(Optional.<SubscriberSpec>absent());

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER)).isAbsent();
    }

}
