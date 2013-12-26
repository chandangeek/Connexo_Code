package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAppServerCreatorTest {

    private static final String NAME = "name";
    @Captor
    private ArgumentCaptor<AppServer> appServerCaptor;

    @Mock
    private TransactionService transactionService;
    @Mock
    private DataMapper<AppServer> appServerFactory;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private MessageService messageService;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    private DestinationSpec newDestination, allServersDestination;
    @Mock
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private JsonService jsonService;

    @Before
    public void setUp() {
        when(dataModel.mapper(AppServer.class)).thenReturn(appServerFactory);
        when(messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")).thenReturn(Optional.of(queueTableSpec));
        when(queueTableSpec.createDestinationSpec(anyString(), anyInt())).thenReturn(newDestination);
        when(messageService.getDestinationSpec(AppService.ALL_SERVERS)).thenReturn(Optional.of(allServersDestination));
        when(dataModel.getInstance(AppServerImpl.class)).thenReturn(new AppServerImpl(dataModel, cronExpressionParser, messageService, jsonService));

        setupFakeTransactionService();

    }

    @SuppressWarnings("unchecked")
	private void setupFakeTransactionService() {
        when(transactionService.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateAppServerSavesAppServerInstance() {

        new DefaultAppServerCreator(dataModel, transactionService, messageService).createAppServer(NAME, cronExpression);

        verify(appServerFactory).persist(appServerCaptor.capture());

        assertThat(appServerCaptor.getValue().getName()).isEqualTo(NAME);
        assertThat(appServerCaptor.getValue().getScheduleFrequency()).isEqualTo(cronExpression);
    }

    @Test
    public void testCreateAppServerCreatesDestinationAndSubscribes() {

        new DefaultAppServerCreator(dataModel, transactionService, messageService).createAppServer(NAME, cronExpression);

        verify(queueTableSpec).createDestinationSpec(anyString(), anyInt());
        verify(newDestination).subscribe(anyString());
    }

    @Test
    public void testCreateAppServerSubscribesToAllServersDestination() {

        new DefaultAppServerCreator(dataModel, transactionService, messageService).createAppServer(NAME, cronExpression);

        verify(allServersDestination).subscribe(anyString());
    }


}
