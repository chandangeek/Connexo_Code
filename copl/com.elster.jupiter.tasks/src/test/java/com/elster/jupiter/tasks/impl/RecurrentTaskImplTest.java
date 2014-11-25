package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecurrentTaskImplTest {

    private static final String PAYLOAD = "payload";
    private static final String NAME = "name";
    private static final Instant NOW = Instant.ofEpochMilli(5000000);
    private static final Instant NEXT = Instant.ofEpochMilli(6000000);
    private static final String SERIALIZED1 = "S1";
    private static final String SERIALIZED2 = "S2";

    private RecurrentTaskImpl recurrentTask;

    @Mock
    private CronExpression cronExpression;
    @Mock
    private DestinationSpec destination;
    @Mock
    private Clock clock;
    @Mock
    private DataMapper<TaskOccurrence> taskOccurrenceFactory;
    @Mock
    private DataMapper<RecurrentTask> recurrentTaskFactory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder builder;

    @Before
    public void setUp() {
        when(dataModel.getInstance(TaskOccurrenceImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new TaskOccurrenceImpl(dataModel, clock);
            }
        });
        when(dataModel.mapper(TaskOccurrence.class)).thenReturn(taskOccurrenceFactory);
        when(dataModel.mapper(RecurrentTask.class)).thenReturn(recurrentTaskFactory);

        recurrentTask = new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock).init(NAME, cronExpression, destination, PAYLOAD);

        when(clock.instant()).thenReturn(NOW);
        when(cronExpression.nextOccurrence(zoned(NOW))).thenReturn(Optional.of(zoned(NEXT)));
    }

    private ZonedDateTime zoned(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetDestination() {
        assertThat(recurrentTask.getDestination()).isEqualTo(destination);
    }


    @Test
    public void testUpdateNextExecution() {
        recurrentTask.updateNextExecution();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(NEXT);
    }

    @Test
    public void testCreateTaskOccurrence() {
        recurrentTask.updateNextExecution();

        TaskOccurrence taskOccurrence = recurrentTask.createScheduledTaskOccurrence();

        verify(taskOccurrenceFactory).persist(taskOccurrence);
        assertThat(taskOccurrence.getPayLoad()).isEqualTo(PAYLOAD);
        assertThat(taskOccurrence.getRecurrentTask()).isEqualTo(recurrentTask);
        assertThat(taskOccurrence.getTriggerTime()).isEqualTo(NEXT);
    }

    @Test
    public void testSave() {
        recurrentTask.save();

        verify(recurrentTaskFactory).persist(recurrentTask);
    }

    @Test
    public void testDelete() {
        recurrentTask.delete();

        verify(recurrentTaskFactory).remove(recurrentTask);
    }

    @Test
    public void testSuspend() {
        recurrentTask.updateNextExecution();
        recurrentTask.suspend();

        assertThat(recurrentTask.getNextExecution()).isNull();
        verify(recurrentTaskFactory).persist(recurrentTask);
    }

    @Test
    public void testResume() {
        recurrentTask.resume();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(NEXT);
        verify(recurrentTaskFactory).persist(recurrentTask);

    }

    @Test
    public void testLaunchOccurrence() {
        field("id").ofType(Long.TYPE).in(recurrentTask).set(1234L); // simulate being saved already

        when(jsonService.serialize(any())).thenReturn(SERIALIZED1);
        when(destination.message(SERIALIZED1)).thenReturn(builder);

        TaskOccurrenceImpl taskOccurrence = recurrentTask.launchOccurrence();

        verify(destination).message(SERIALIZED1);
        verify(builder).send();
        verify(dataModel.mapper(RecurrentTask.class)).update(recurrentTask);
        assertThat(taskOccurrence).isNotNull();

    }

}
