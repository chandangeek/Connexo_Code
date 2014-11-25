package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskOccurrenceImplTest {

    private static final String NAME = "sfsdqfsqdfqsdfsdqf";
    @Mock
    private DataModel dataModel;
    private RecurrentTask recurrentTask;
    @Mock
    private Clock clock;
    private static final Instant now = Instant.ofEpochMilli(123456);
    private static final Instant instant2 = Instant.ofEpochMilli(124000);
    @Mock
    private DataMapper<RecurrentTask> mapper;

    @Before
    public void setUp() {
        when(dataModel.getInstance(RecurrentTaskImpl.class)).thenReturn(new RecurrentTaskImpl(dataModel, mock(ScheduleExpressionParser.class), mock(MessageService.class), mock(JsonService.class), clock));
        when(dataModel.getInstance(TaskOccurrenceImpl.class)).thenAnswer(invocation -> new TaskOccurrenceImpl(dataModel, clock));
        when(dataModel.getInstance(TaskLogEntryImpl.class)).thenAnswer(invocation -> new TaskLogEntryImpl());
        when(dataModel.mapper(RecurrentTask.class)).thenReturn(mapper);

        when(clock.instant()).thenReturn(now, instant2);
        recurrentTask = RecurrentTaskImpl.from(dataModel, NAME, mock(ScheduleExpression.class), mock(DestinationSpec.class), "payload");
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testStart() {
        TaskOccurrenceImpl taskOccurrence = TaskOccurrenceImpl.createScheduled(dataModel, recurrentTask, Instant.EPOCH);
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.NOT_EXECUTED_YET);
        assertThat(taskOccurrence.getStartDate()).isAbsent();
        assertThat(taskOccurrence.getEndDate()).isAbsent();

        taskOccurrence.start();
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.BUSY);
        assertThat(taskOccurrence.getStartDate()).contains(now);
        assertThat(taskOccurrence.getEndDate()).isAbsent();

        //next is noop
        taskOccurrence.start();
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.BUSY);
        assertThat(taskOccurrence.getStartDate()).contains(now);
        assertThat(taskOccurrence.getEndDate()).isAbsent();

        // goto final state
        taskOccurrence.hasRun(true);
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(taskOccurrence.getStartDate()).contains(now);
        assertThat(taskOccurrence.getEndDate()).contains(instant2);

        //next is noop
        taskOccurrence.start();
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(taskOccurrence.getStartDate()).contains(now);
        assertThat(taskOccurrence.getEndDate()).contains(instant2);
    }

    @Test
    public void testHasRun() {
        TaskOccurrenceImpl taskOccurrence = TaskOccurrenceImpl.createScheduled(dataModel, recurrentTask, Instant.EPOCH);
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.NOT_EXECUTED_YET);
        assertThat(taskOccurrence.getStartDate()).isAbsent();
        assertThat(taskOccurrence.getEndDate()).isAbsent();

        taskOccurrence.hasRun(true);
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(taskOccurrence.getStartDate()).isAbsent();
        assertThat(taskOccurrence.getEndDate()).contains(now);
        taskOccurrence.hasRun(false);
        assertThat(taskOccurrence.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(taskOccurrence.getStartDate()).isAbsent();
        assertThat(taskOccurrence.getEndDate()).contains(now);
    }

    @Test
    public void testCreatedLogHandlerTriggersLogEntriesBeingAdded() {
        TaskOccurrenceImpl taskOccurrence = TaskOccurrenceImpl.createScheduled(dataModel, recurrentTask, Instant.EPOCH);

        Handler handler = taskOccurrence.createTaskLogHandler().asHandler();

        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(handler);

        logger.log(Level.WARNING, "Whoops");

// TODO get log entries        assertThat(recurrentTask.get)
    }

}