/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecurrentTaskIT {

    @Rule
    public TimeZoneNeutral murdo = Using.timeZoneOfMcMurdo();

    private final ZonedDateTime zonedDateTime = ZonedDateTime.of(2017, 8, 14, 18, 0, 0, 0, ZoneId.of(murdo.getSubstitute()));
    private final Instant now = zonedDateTime.toInstant();
    private final Instant nextExecution = ZonedDateTime.of(2017, 8, 15, 18, 0, 0, 0, ZoneId.of(murdo.getSubstitute())).toInstant();

    public static final String NAME = "NAME";
    public static final String PAY_LOAD = "PLD";
    public static final TemporalExpression TEMPORAL_EXPRESSION = new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(18));
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    private TaskService taskService;
    private MessageService messageService;
    private TransactionService transactionService;
    private NlsService nlsService;

    @Mock
    private UserService userService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LogService logService;

    private Clock clock;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() {
        clock = new ProgrammableClock(TimeZoneNeutral.getMcMurdo(), now);

        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new TaskModule(),
                    new DataVaultModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {

            @Override
            public Void perform() {
                taskService = injector.getInstance(TaskService.class);
                messageService = injector.getInstance(MessageService.class);
                nlsService = injector.getInstance(NlsService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateRecurrentTask() {
        long id;
        id = createRecurrentTask(PeriodicalScheduleExpression.every(1).days().at(18, 0, 0).build());
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();
        assertThat(recurrentTask).isNotNull();
        recurrentTask.updateNextExecution();
        assertThat(recurrentTask.getNextExecution()).isEqualTo(nextExecution);
    }

    @Test
    public void testHistory() throws InterruptedException {
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(
                now,
                zonedDateTime.plusSeconds(1).toInstant(),
                zonedDateTime.plusSeconds(2).toInstant(),
                zonedDateTime.plusSeconds(3).toInstant(),
                zonedDateTime.plusSeconds(4).toInstant(),
                zonedDateTime.plusSeconds(5).toInstant(),
                zonedDateTime.plusSeconds(6).toInstant(),
                zonedDateTime.plusSeconds(7).toInstant(),
                zonedDateTime.plusSeconds(8).toInstant()
        );

        long id;
        PeriodicalScheduleExpression originalSchedule = PeriodicalScheduleExpression.every(1).days().at(18, 0, 0).build();
        id = createRecurrentTask(originalSchedule);
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();

        PeriodicalScheduleExpression newSchedule = PeriodicalScheduleExpression.every(1).days().at(19, 0, 0).build();
        recurrentTask.setScheduleExpression(newSchedule);

        try (TransactionContext context = transactionService.getContext()) {
            recurrentTask.save();
            context.commit();
        }

        History<? extends RecurrentTask> history = recurrentTask.getHistory();

        Optional<? extends RecurrentTask> version1 = history.getVersion(1);
        JupiterAssertions.assertThat(version1).isPresent();
        assertThat(version1.get().getScheduleExpression()).isEqualTo(originalSchedule);
        Optional<? extends RecurrentTask> version2 = history.getVersion(2);
        JupiterAssertions.assertThat(version2).isPresent();
        assertThat(version2.get().getScheduleExpression()).isEqualTo(newSchedule);
    }


    @Test
    public void testCreateRecurrentTaskWithTemporalExpression() {
        long id;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destination = queueTableSpec.createDestinationSpec("Destiny", 60);
            RecurrentTask recurrentTask = taskService.newBuilder()
                    .setApplication("Pulse")
                    .setName(NAME)
                    .setScheduleExpression(TEMPORAL_EXPRESSION)
                    .setDestination(destination)
                    .setPayLoad(PAY_LOAD)
                    .scheduleImmediately(true)
                    .build();
            id = recurrentTask.getId();
            context.commit();
        }
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();
        assertThat(recurrentTask).isNotNull();
        recurrentTask.updateNextExecution();
        assertThat(recurrentTask.getNextExecution()).isEqualTo(nextExecution);
    }

    @Test
    public void testUpdateScheduleOfRecurrentTask() {
        long id;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destination = queueTableSpec.createDestinationSpec("Destiny", 60);
            RecurrentTask recurrentTask = taskService.newBuilder()
                    .setApplication("Pulse")
                    .setName(NAME)
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .setDestination(destination)
                    .setPayLoad(PAY_LOAD)
                    .scheduleImmediately(true)
                    .build();
            id = recurrentTask.getId();
            context.commit();
        }
        try (TransactionContext context = transactionService.getContext()) {
            RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();
            recurrentTask.setScheduleExpression(Never.NEVER);
            recurrentTask.save();
            context.commit();
        }
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();
        assertThat(recurrentTask.getScheduleExpression()).isEqualTo(Never.NEVER);
    }

    @Test
    public void testDuplicateRecurrentTask() {
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destination = queueTableSpec.createDestinationSpec("Destiny", 60);
            RecurrentTask recurrentTask = taskService.newBuilder()
                    .setApplication("Pulse")
                    .setName("DUPLICATED")
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .setDestination(destination)
                    .setPayLoad(PAY_LOAD)
                    .scheduleImmediately(true)
                    .build();
            RecurrentTask recurrentTask2 = taskService.newBuilder()
                    .setApplication("Pulse")
                    .setName("DUPLICATED")
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .setDestination(destination)
                    .setPayLoad(PAY_LOAD)
                    .scheduleImmediately(true)
                    .build();
            fail("violation expected");
        } catch (ConstraintViolationException cve) {
            ConstraintViolation<?> constraintViolation = cve.getConstraintViolations().iterator().next();
            assertThat(constraintViolation.getMessage()).isEqualTo("NotUnique");
            assertThat(constraintViolation.getPropertyPath().iterator().next().getName()).isEqualTo("name");
        }
    }

    @Test
    public void testUpdateNextExecutionOfRecurrentTask() {
        long id;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destination = queueTableSpec.createDestinationSpec("Destiny", 60);
            RecurrentTask recurrentTask = taskService.newBuilder()
                    .setApplication("Pulse")
                    .setName(NAME)
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .setDestination(destination)
                    .setPayLoad(PAY_LOAD)
                    .scheduleImmediately(true)
                    .build();
            id = recurrentTask.getId();
            context.commit();
        }
        Instant instant = ZonedDateTime.of(2017, 9, 15, 15, 21, 12, 888000000, ZoneId.of("UTC")).toInstant();
        try (TransactionContext context = transactionService.getContext()) {
            RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();
            recurrentTask.setNextExecution(instant);
            recurrentTask.save();
            context.commit();
        }
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).get();
        assertThat(recurrentTask.getNextExecution()).isEqualTo(instant);
    }

    @Test
    public void testLastRunNextOccurrenceUpdateNoJournal() {
        long recurrentTaskId = createRecurrentTask(PeriodicalScheduleExpression.every(1).minutes().at(0).build());
        RecurrentTaskImpl recurrentTask = (RecurrentTaskImpl) taskService.getRecurrentTask(recurrentTaskId).get();
        OrmService instance = injector.getInstance(OrmService.class);
        DataMapper<RecurrentTask> dataModel = instance.getDataModel(TaskService.COMPONENTNAME).get().mapper(RecurrentTask.class);
        assertThat(dataModel.getJournal(recurrentTaskId)).isEmpty();

        transactionService.builder().principal(() -> "RecurrentTaskIT").run(recurrentTask::launchOccurrence);
        assertThat(dataModel.getJournal(recurrentTaskId)).isEmpty();
        transactionService.builder().principal(() -> "RecurrentTaskIT").run(() -> {
            recurrentTask.updateLastRun(now);
        });
        assertThat(dataModel.getJournal(recurrentTaskId)).isEmpty();
        transactionService.builder().principal(() -> "RecurrentTaskIT").run(recurrentTask::suspend);
        assertThat(dataModel.getJournal(recurrentTaskId)).hasSize(1);
        transactionService.builder().principal(() -> "RecurrentTaskIT").run(recurrentTask::resume);
        assertThat(dataModel.getJournal(recurrentTaskId)).hasSize(2);
    }

    private long createOccurenceAndLogOnDifferentLevels(RecurrentTaskImpl recurrentTask) {
        long taskOccurrenceId;
        try (TransactionContext context = transactionService.getContext()) {
            TaskOccurrence taskOccurrence = recurrentTask.createScheduledTaskOccurrence();
            Logger logger = Logger.getAnonymousLogger();
            // In test root logger is not really configured. So I want all logs to appear in these tests
            logger.setLevel(Level.ALL);
            logger.addHandler(taskOccurrence.createTaskLogHandler().asHandler());
            logger.log(Level.FINE, "   Coucou Fine  ");
            logger.log(Level.INFO, "   Coucou Info  ");
            logger.log(Level.WARNING, "   Coucou Warning  ");

            taskOccurrenceId = taskOccurrence.getId();
            context.commit();
        }
        return taskOccurrenceId;
    }

    @Test
    public void testTaskOccurrenceLogDefaultLogLevel() {
        long id = createRecurrentTask(PeriodicalScheduleExpression.every(1).days().at(18, 0, 0).build());
        RecurrentTaskImpl recurrentTask = (RecurrentTaskImpl) taskService.getRecurrentTask(id).get();
        DataModel dataModel = injector.getInstance(OrmService.class).getDataModel(TaskService.COMPONENTNAME).get();

        long taskOccurrenceId = createOccurenceAndLogOnDifferentLevels(recurrentTask);

        TaskOccurrence occurrence = dataModel.mapper(TaskOccurrence.class).getExisting(taskOccurrenceId);
        List<TaskLogEntry> logs = occurrence.getLogs();
        assertThat(logs).hasSize(1);
        TaskLogEntry entry = logs.get(0);
        assertThat(entry.getTaskOccurrence()).isEqualTo(occurrence);
        assertThat(entry.getLogLevel()).isEqualTo(Level.WARNING);
        assertThat(entry.getMessage()).isEqualTo("Coucou Warning");

    }


    @Test
    public void testTaskOccurrenceLogFineLevel() {
        long id = createRecurrentTask(PeriodicalScheduleExpression.every(1).days().at(18, 0, 0).build(), Level.FINE);
        RecurrentTaskImpl recurrentTask = (RecurrentTaskImpl) taskService.getRecurrentTask(id).get();
        DataModel dataModel = injector.getInstance(OrmService.class).getDataModel(TaskService.COMPONENTNAME).get();

        long taskOccurrenceId = createOccurenceAndLogOnDifferentLevels(recurrentTask);

        TaskOccurrence occurrence = dataModel.mapper(TaskOccurrence.class).getExisting(taskOccurrenceId);
        List<TaskLogEntry> logs = occurrence.getLogs();
        assertThat(logs).hasSize(3);
        TaskLogEntry entry = logs.get(0);
        assertThat(entry.getTaskOccurrence()).isEqualTo(occurrence);
        assertThat(entry.getLogLevel()).isEqualTo(Level.FINE);
        assertThat(entry.getMessage()).isEqualTo("Coucou Fine");
        entry = logs.get(1);
        assertThat(entry.getTaskOccurrence()).isEqualTo(occurrence);
        assertThat(entry.getLogLevel()).isEqualTo(Level.INFO);
        assertThat(entry.getMessage()).isEqualTo("Coucou Info");
        entry = logs.get(2);
        assertThat(entry.getTaskOccurrence()).isEqualTo(occurrence);
        assertThat(entry.getLogLevel()).isEqualTo(Level.WARNING);
        assertThat(entry.getMessage()).isEqualTo("Coucou Warning");
    }

    @Test
    public void testGetVersionAt() {
        long recurrentTaskId = createRecurrentTask(PeriodicalScheduleExpression.every(1).days().at(18, 0, 0).build());
        RecurrentTaskImpl recurrentTask = (RecurrentTaskImpl) taskService.getRecurrentTask(recurrentTaskId).get();
        Optional<RecurrentTask> recurrentTaskVersion = recurrentTask.getVersionAt(now.minus(Duration.ofMinutes(1)));
        assertThat(recurrentTaskVersion).isEmpty();
        recurrentTaskVersion = recurrentTask.getVersionAt(now);
        assertThat(recurrentTaskVersion).isPresent();
        assertThat(recurrentTaskVersion.get().getVersion()).isEqualTo(1);
        recurrentTaskVersion = recurrentTask.getVersionAt(now.plus(Duration.ofMinutes(1)));
        assertThat(recurrentTaskVersion).isPresent();

        ((ProgrammableClock) clock).setTicker(() -> zonedDateTime.plusDays(1).toInstant());
        transactionService.builder().principal(() -> "RecurrentTaskIt").run(() -> {
            recurrentTask.updateNextExecution();
            recurrentTask.save();
        });
        recurrentTaskVersion = recurrentTask.getVersionAt(zonedDateTime.plusDays(1).toInstant());
        assertThat(recurrentTaskVersion).isPresent();
        assertThat(recurrentTaskVersion.get().getVersion()).isEqualTo(2);
        recurrentTaskVersion = recurrentTask.getVersionAt(now);
        assertThat(recurrentTaskVersion).isPresent();
        assertThat(recurrentTaskVersion.get().getVersion()).isEqualTo(1);

        ((ProgrammableClock) clock).setTicker(() -> zonedDateTime.plusDays(2).toInstant());
        transactionService.builder().principal(() -> "RecurrentTaskIt").run(() -> {
            recurrentTask.setScheduleExpression(PeriodicalScheduleExpression.every(1).months().at(1, 20, 0, 0).build());
            recurrentTask.save();
        });
        recurrentTaskVersion = recurrentTask.getVersionAt(zonedDateTime.plusDays(2).toInstant());
        assertThat(recurrentTaskVersion).isPresent();
        assertThat(recurrentTaskVersion.get().getVersion()).isEqualTo(3);
        recurrentTaskVersion = recurrentTask.getVersionAt(now);
        assertThat(recurrentTaskVersion).isPresent();
        assertThat(recurrentTaskVersion.get().getVersion()).isEqualTo(1);

    }

    private long createRecurrentTask(ScheduleExpression scheduleExpression) {
        return createRecurrentTask(scheduleExpression, null);
    }

    private long createRecurrentTask(ScheduleExpression scheduleExpression, Level level) {
        long id;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destination = queueTableSpec.createDestinationSpec("Destiny", 60);
            RecurrentTaskBuilder.RecurrentTaskBuilderFinisher builder = taskService.newBuilder()
                    .setApplication("Pulse")
                    .setName(NAME)
                    .setScheduleExpression(scheduleExpression)
                    .setDestination(destination)
                    .setPayLoad(PAY_LOAD)
                    .scheduleImmediately(true);
            if (level != null) {
                builder.setLogLevel(level.intValue());
            }
            RecurrentTask recurrentTask = builder.build();
            id = recurrentTask.getId();
            context.commit();
        }
        return id;
    }

}