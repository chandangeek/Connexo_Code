package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecurrentTaskImplTest extends EqualsContractTest {

    private static final String PAYLOAD = "payload";
    private static final String NAME = "name";
    private static final Instant NOW = Instant.ofEpochMilli(5000000);
    private static final Instant NEXT = Instant.ofEpochMilli(6000000);
    private static final String SERIALIZED1 = "S1";
    private static final long INSTANCEA_ID = 45;
    private static final String APPLICATION = "Pulse";

    private RecurrentTaskImpl instanceA;
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
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.getInstance(TaskOccurrenceImpl.class)).thenAnswer(invocationOnMock -> new TaskOccurrenceImpl(dataModel, clock, thesaurus));
        when(dataModel.mapper(TaskOccurrence.class)).thenReturn(taskOccurrenceFactory);
        when(dataModel.mapper(RecurrentTask.class)).thenReturn(recurrentTaskFactory);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(anyObject())).thenReturn(new HashSet<>());

        recurrentTask = new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock).init(APPLICATION, NAME, cronExpression, destination, PAYLOAD, Level.INFO.intValue());

        when(clock.instant()).thenReturn(NOW);
        when(cronExpression.nextOccurrence(zoned(NOW))).thenReturn(Optional.of(zoned(NEXT)));
    }

    private ZonedDateTime zoned(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock).init(APPLICATION, NAME, cronExpression, destination, PAYLOAD, Level.INFO.intValue());
            field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCEA_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        RecurrentTaskImpl entity = new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock).init(APPLICATION, NAME, cronExpression, destination, PAYLOAD, Level.INFO.intValue());
        field("id").ofType(Long.TYPE).in(entity).set(INSTANCEA_ID);
        return entity;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        RecurrentTaskImpl entity = new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock).init(APPLICATION, NAME, cronExpression, destination, PAYLOAD, Level.SEVERE.intValue());
        field("id").ofType(Long.TYPE).in(entity).set(INSTANCEA_ID + 1);
        return Collections.singletonList(entity);
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        RecurrentTaskImpl recurrentTask = new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock) {
        }.init(APPLICATION, NAME, cronExpression, destination, PAYLOAD, Level.INFO.intValue());
        field("id").ofType(Long.TYPE).in(recurrentTask).set(INSTANCEA_ID);
        return recurrentTask;
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

        verify(dataModel).persist(recurrentTask);
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
        verify(dataModel).persist(recurrentTask);
    }

    @Test
    public void testResume() {
        recurrentTask.resume();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(NEXT);
        verify(dataModel).persist(recurrentTask);

    }

    @Test
    public void testLaunchOccurrence() {
        field("id").ofType(Long.TYPE).in(recurrentTask).set(1234L); // simulate being saved already

        when(jsonService.serialize(any())).thenReturn(SERIALIZED1);
        when(destination.message(SERIALIZED1)).thenReturn(builder);

        TaskOccurrenceImpl taskOccurrence = recurrentTask.launchOccurrence();

        verify(destination).message(SERIALIZED1);
        verify(builder).send();
        verify(recurrentTaskFactory).update(recurrentTask, "nextExecution");
        assertThat(taskOccurrence).isNotNull();

    }

}
