package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
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
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
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
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
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
    @Mock
    private Clock clock;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(now);

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
                new TaskModule()
        );
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

        when (clock.instant()).thenReturn(
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
                    .setScheduleExpression(TEMPORAL_EXPRESSION)
                    .scheduleImmediately()
                    .setName(NAME)
                    .setPayLoad(PAY_LOAD)
                    .setDestination(destination)
                    .build();
            recurrentTask.save();
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
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .scheduleImmediately()
                    .setName(NAME)
                    .setPayLoad(PAY_LOAD)
                    .setDestination(destination)
                    .build();
            recurrentTask.save();
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
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .scheduleImmediately()
                    .setName("DUPLICATED")
                    .setPayLoad(PAY_LOAD)
                    .setDestination(destination)
                    .build();
            recurrentTask.save();
            RecurrentTask recurrentTask2 = taskService.newBuilder()
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .scheduleImmediately()
                    .setName("DUPLICATED")
                    .setPayLoad(PAY_LOAD)
                    .setDestination(destination)
                    .build();
            recurrentTask2.save();
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
                    .setScheduleExpressionString("0 0 18 * * ? *")
                    .scheduleImmediately()
                    .setName(NAME)
                    .setPayLoad(PAY_LOAD)
                    .setDestination(destination)
                    .build();
            recurrentTask.save();
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
    public void testTaskOccurrenceLog() {
        long id = createRecurrentTask(PeriodicalScheduleExpression.every(1).days().at(18, 0, 0).build());
        RecurrentTaskImpl recurrentTask = (RecurrentTaskImpl) taskService.getRecurrentTask(id).get();
        OrmService instance = injector.getInstance(OrmService.class);
        DataModel dataModel = instance.getDataModel(TaskService.COMPONENTNAME).get();

        long taskOccurrenceId;
        try (TransactionContext context = transactionService.getContext()) {
            TaskOccurrence taskOccurrence = recurrentTask.createScheduledTaskOccurrence();
            Logger logger = Logger.getAnonymousLogger();
            logger.addHandler(taskOccurrence.createTaskLogHandler().asHandler());
            logger.log(Level.INFO, "   Coucou   ");

            taskOccurrenceId = taskOccurrence.getId();
            context.commit();
        }

        TaskOccurrence occurrence = dataModel.mapper(TaskOccurrence.class).getExisting(taskOccurrenceId);
        List<TaskLogEntry> logs = occurrence.getLogs();
        assertThat(logs).hasSize(1);
        TaskLogEntry entry = logs.get(0);
        assertThat(entry.getTaskOccurrence()).isEqualTo(occurrence);
        assertThat(entry.getLogLevel()).isEqualTo(Level.INFO);
        assertThat(entry.getMessage()).isEqualTo("Coucou");

    }

    private long createRecurrentTask(ScheduleExpression scheduleExpression) {
        long id;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destination = queueTableSpec.createDestinationSpec("Destiny", 60);
            RecurrentTask recurrentTask = taskService.newBuilder()
                    .setScheduleExpression(scheduleExpression)
                    .scheduleImmediately()
                    .setName(NAME)
                    .setPayLoad(PAY_LOAD)
                    .setDestination(destination)
                    .build();
            recurrentTask.save();
            id = recurrentTask.getId();
            context.commit();
        }
        return id;
    }

}