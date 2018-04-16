/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Never;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.ValidatorFactory;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomTaskImplIT {

    public static final String NAME = "NAME";
    private CustomTaskService customTaskService;
    private TaskService taskService;
    private ThreadPrincipalService threadPrincipalService;
    private static final String MULTISENSE_KEY = "MDC";

    @Mock
    private HttpService httpService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LogService.class).toInstance(logService);
            bind(HttpService.class).toInstance(httpService);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    public static final String FORMATTER = "formatter";

    private static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 12, 9, 46, 12, 241615214, TimeZoneNeutral.getMcMurdo());
    private final ProgrammableClock clock = new ProgrammableClock(ZoneId.systemDefault(), NOW.toInstant());

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();
    private Injector injector;


    private QueryService queryService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private PropertySpec min, max, consZero;
    @Mock
    private LogService logService;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private User user;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private TransactionService transactionService;
    //private MeteringService meteringService;
    //private MeteringGroupsService meteringGroupsService;
    // private ReadingType readingType, anotherReadingType;
    private TimeService timeService;


    @Before
    public void setUp() throws SQLException {
        when(user.getName()).thenReturn("customTask");

        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new TransactionModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new OrmModule(),
                    new AppServiceModule(),
                    new NlsModule(),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new EventsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        queryService = injector.getInstance(QueryService.class);
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            //  injector.getInstance(FiniteStateMachineService.class);
            customTaskService = (CustomTaskServiceImpl) injector.getInstance(CustomTaskServiceImpl.class);
            //timeService = injector.getInstance(TimeService.class);

            //threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            return null;
        });
        taskService = injector.getInstance(TaskService.class);
       /*
        setMessageService(messageService);

        setClock(clock);



        setUpgradeService(upgradeService);

        setQueryService(queryService);*/
/*
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new OrmModule(),
                    new TimeModule(),
                    new TaskModule(),
                    new NlsModule(),
                    new AppServiceModule(),
                    new TransactionModule(),
                    new BasicPropertiesModule(),
                    new UserModule(),

                    new PartyModule(),
                    new DomainUtilModule(),

                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),



                    new SearchModule(),

                    new WebServicesModule()


            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
          //  injector.getInstance(FiniteStateMachineService.class);
            customTaskService = (CustomTaskService) injector.getInstance(CustomTaskService.class);
            timeService = injector.getInstance(TimeService.class);

            threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            return null;
        });

        taskService = injector.getInstance(TaskService.class);*/
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }


    @Test
    public void testCreationTaskWithEndDeviceGroup() {
        CustomTask customTaskTask = createAndSaveTask(NAME);
        Optional<? extends CustomTask> found = customTaskService.findCustomTask(customTaskTask.getId());

        assertThat(found).isPresent();
        CustomTask foundCustomTaskTask = found.get();
        assertThat(foundCustomTaskTask.getLastRun()).isEmpty();
        Assertions.assertThat(foundCustomTaskTask.getNextExecution()).isEqualTo(NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
    }

    @Test
    public void testUpdateTask() {

        CustomTask customTaskTask = createAndSaveTask(NAME);
        Optional<? extends CustomTask> found = customTaskService.findCustomTask(customTaskTask.getId());

        assertThat(found).isPresent();
        Instant instant = ZonedDateTime.of(2019, 4, 18, 2, 47, 14, 124000000, ZoneId.of("UTC")).toInstant();

        try (TransactionContext context = transactionService.getContext()) {
            CustomTask task = found.get();
            task.setNextExecution(instant);
            task.setScheduleExpression(Never.NEVER);
            task.setName("New name!");
            task.update();
            context.commit();
        }

        found = customTaskService.findCustomTask(customTaskTask.getId());

        assertThat(found).isPresent();

        Assertions.assertThat(found.get().getNextExecution()).isEqualTo(instant);
        Assertions.assertThat(found.get().getScheduleExpression()).isEqualTo(Never.NEVER);
        Assertions.assertThat(found.get().getName()).isEqualTo("New name!");
    }

    private CustomTask createAndSaveTask(String name) {
        CustomTask customTaskTask = null;
        try (TransactionContext context = transactionService.getContext()) {
            customTaskTask = createCustomTask(name);
            context.commit();
        }
        return customTaskTask;
    }

    private CustomTask createCustomTask(String name) {
        return customTaskService.newBuilder()
                .setName(name)
                .setApplication(MULTISENSE_KEY)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .create();
    }
}