package com.elster.jupiter.messaging.impl;

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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
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
    @Mock
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
        when(dataModel.getDataMapper(QueueTableSpec.class, QueueTableSpecImpl.class, TableSpecs.MSG_QUEUETABLESPEC.name())).thenReturn(queueTableSpecFactory);
        when(dataModel.getDataMapper(DestinationSpec.class, DestinationSpecImpl.class, TableSpecs.MSG_DESTINATIONSPEC.name())).thenReturn(destinationSpecFactory);
        when(dataModel.getDataMapper(SubscriberSpec.class, SubscriberSpecImpl.class, TableSpecs.MSG_CONSUMERSPEC.name())).thenReturn(subscriberSpecFactory);
        when(dataModel.addTable(anyString())).thenReturn(table);
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
        Bus.setServiceLocator(null);
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

        assertThat(messageService.getQueueTableSpec(QTS).isPresent()).isTrue();
        assertThat(messageService.getQueueTableSpec(QTS).get()).isEqualTo(queueTableSpec);
    }

    @Test
    public void testGetQueueTableSpecNotExists() {
        when(queueTableSpecFactory.get(QTS)).thenReturn(Optional.<QueueTableSpec>absent());

        assertThat(messageService.getQueueTableSpec(QTS).isPresent()).isFalse();
    }

    @Test
    public void testGetDestinationSpec() {
        when(destinationSpecFactory.get(DESTINATION)).thenReturn(Optional.of(destination));

        assertThat(messageService.getDestinationSpec(DESTINATION).isPresent()).isTrue();
        assertThat(messageService.getDestinationSpec(DESTINATION).get()).isEqualTo(destination);
    }

    @Test
    public void testGetDestinationSpecNotExists() {
        when(destinationSpecFactory.get(DESTINATION)).thenReturn(Optional.<DestinationSpec>absent());

        assertThat(messageService.getDestinationSpec(DESTINATION).isPresent()).isFalse();
    }

    @Test
    public void testGetSubscriberSpec() {
        when(subscriberSpecFactory.get(DESTINATION, SUBSCRIBER)).thenReturn(Optional.of(subscriberSpec));

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).isPresent()).isTrue();
        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).get()).isEqualTo(subscriberSpec);
    }

    @Test
    public void testGetSubscriberSpecNotExists() {
        when(subscriberSpecFactory.get(DESTINATION, SUBSCRIBER)).thenReturn(Optional.<SubscriberSpec>absent());

        assertThat(messageService.getSubscriberSpec(DESTINATION, SUBSCRIBER).isPresent()).isFalse();
    }

}
