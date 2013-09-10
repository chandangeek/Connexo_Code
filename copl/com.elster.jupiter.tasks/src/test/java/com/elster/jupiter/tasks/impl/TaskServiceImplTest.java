package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskServiceImplTest {

    private static final long ID = 15;
    private static final Date NOW = new DateTime(2013, 9, 10, 14, 47, 24).toDate();
    private TaskServiceImpl taskService;

    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<RecurrentTask> recurrentTaskFactory;
    @Mock
    private Table table;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private TransactionService transactionService;
    @Mock
    private OrmClient ormClient;
    @Mock
    private Connection connection;
    @Mock
    private Clock clock;
    @Mock
    private DueTaskFetcher dueTaskFetcher;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private DestinationSpec destination;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder messageBuilder;

    @Before
    public void setUp() throws SQLException {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString())).thenReturn(table);
        when(dataModel.getDataMapper(any(Class.class), any(Class.class), anyString())).thenReturn(recurrentTaskFactory);
        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
        when(transactionService.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getConnection()).thenReturn(connection);
        when(serviceLocator.getClock()).thenReturn(clock);
        when(clock.now()).thenReturn(NOW);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);

        taskService = new TaskServiceImpl();
        taskService.setDueTaskFetcher(dueTaskFetcher);

        taskService.setCronExpressionParser(cronExpressionParser);
        taskService.setOrmService(ormService);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testNewBuilder() {
        // ok we test an implementation detail, but since the specific type of RecurrentTaskBuilder is already tested, we save testing its behavior again...
        // so if at some point the implementation class changes, simply change this test to check for the new implementation type (making sure that new type is well tested itself)

        assertThat(taskService.newBuilder()).isInstanceOf(DefaultRecurrentTaskBuilder.class);
    }

    @Test
    public void testCreateMessageHandler() {
        // ok we test an implementation detail, but since the specific type of MessageHandler is already tested, we save testing its behavior again...
        // so if at some point the implementation class changes, simply change this test to check for the new implementation type (making sure that new type is well tested itself)

        assertThat(taskService.createMessageHandler(taskExecutor)).isInstanceOf(TaskExecutionMessageHandler.class);
    }

    @Test
    public void testGetRecurrentTaskById() {
        when(recurrentTaskFactory.get(ID)).thenReturn(Optional.of(recurrentTask));

        assertThat(taskService.getRecurrentTask(ID).isPresent()).isTrue();
        assertThat(taskService.getRecurrentTask(ID).get()).isEqualTo(recurrentTask);
    }

    @Test
    public void testGetRecurrentTaskByIdNotFound() {
        when(recurrentTaskFactory.get(ID)).thenReturn(Optional.<RecurrentTask>absent());

        assertThat(taskService.getRecurrentTask(ID).isPresent()).isFalse();
    }

    @Test
    public void testLaunchStartsScanningForDueTasks() throws InterruptedException {
        final CountDownLatch jobEndedLatch = new CountDownLatch(1);

        when(dueTaskFetcher.dueTasks()).thenReturn(Arrays.asList(recurrentTask));
        when(recurrentTask.createTaskOccurrence()).thenReturn(taskOccurrence);
        when(recurrentTask.getDestination()).thenReturn(destination);
        when(destination.message(anyString())).thenReturn(messageBuilder);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                jobEndedLatch.countDown();
                return null;
            }
        }).when(recurrentTask).save();

        try {
            taskService.launch();

            assertThat(jobEndedLatch.await(1, TimeUnit.MINUTES)).isTrue(); // ensure jobs get executed
        } finally {
            taskService.deactivate(null);
        }


    }


}
