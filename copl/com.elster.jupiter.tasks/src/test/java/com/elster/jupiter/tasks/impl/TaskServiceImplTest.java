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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskServiceImplTest {

    private static final long ID = 15;
    private static final Instant NOW = ZonedDateTime.of(2013, 9, 10, 14, 47, 24, 0, ZoneId.systemDefault()).toInstant();
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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private TransactionService transactionService;
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
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.<RecurrentTask>mapper(any())).thenReturn(recurrentTaskFactory);
        when(transactionService.execute(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });
        when(clock.instant()).thenReturn(NOW);

        taskService = new TaskServiceImpl();
        taskService.setDueTaskFetcher(dueTaskFetcher);
        taskService.setCronExpressionParser(cronExpressionParser);
        taskService.setOrmService(ormService);
        taskService.setTransactionService(transactionService);
        taskService.setJsonService(jsonService);
    }

    @After
    public void tearDown() {
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
        when(recurrentTaskFactory.getOptional(ID)).thenReturn(Optional.of(recurrentTask));

        assertThat(taskService.getRecurrentTask(ID).get()).isEqualTo(recurrentTask);
    }

    @Test
    public void testGetRecurrentTaskByIdNotFound() {
        when(recurrentTaskFactory.getOptional(ID)).thenReturn(Optional.empty());

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

            assertThat(jobEndedLatch.await(10, TimeUnit.MINUTES)).isTrue(); // ensure jobs get executed
        } finally {
            taskService.deactivate();
        }


    }


}
