package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
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
import static com.elster.jupiter.time.RelativeField.DAY;
import static com.elster.jupiter.time.RelativeField.HOUR;
import static com.elster.jupiter.time.RelativeField.MINUTES;
import static com.elster.jupiter.time.RelativeField.MONTH;
import static com.elster.jupiter.time.RelativeField.YEAR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationTaskImplIT {

    public static final String NAME = "NAME";
    private EnumeratedEndDeviceGroup anotherEndDeviceGroup;
    private EnumeratedUsagePointGroup anotherUsagePointGroup;
    private IEstimationService estimationService;
    private TaskService taskService;
    private ThreadPrincipalService threadPrincipalService;

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
    private MeteringService meteringService;
    private MeteringGroupsService meteringGroupsService;
    private ReadingType readingType, anotherReadingType;
    private TimeService timeService;
    private final RelativeDate startOfTheYearBeforeLastYear = new RelativeDate(
            YEAR.minus(2),
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private final RelativeDate startOfLastYear = new RelativeDate(
            YEAR.minus(1),
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private final RelativeDate startOfThisYear = new RelativeDate(
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private RelativePeriod lastYear;
    private RelativePeriod oneYearBeforeLastYear;
    private EndDeviceGroup endDeviceGroup;
    private UsagePointGroup usagePointGroup;

    @Before
    public void setUp() throws SQLException {
//        when(userService.findUser(anyString())).thenReturn(Optional.of(user));
        when(user.getName()).thenReturn("estimation");

        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule(
                            "0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                            "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"
                    ),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new EstimationModule(),
                    new TaskModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new UserModule(),
                    new WebServicesModule(),
                    new AppServiceModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule(),
                    new FileImportModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            estimationService = (IEstimationService) injector.getInstance(EstimationService.class);
            timeService = injector.getInstance(TimeService.class);
            meteringService = injector.getInstance(MeteringService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            return null;
        });
        readingType = meteringService.getReadingType("0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        anotherReadingType = meteringService.getReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        try (TransactionContext context = transactionService.getContext()) {
            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup()
                    .setName("none")
                    .create();
            anotherEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup()
                    .setName("also none")
                    .create();
            usagePointGroup = meteringGroupsService.createEnumeratedUsagePointGroup()
                    .setName("none")
                    .create();
            anotherUsagePointGroup = meteringGroupsService.createEnumeratedUsagePointGroup()
                    .setName("also none")
                    .create();
            context.commit();
        }
        taskService = injector.getInstance(TaskService.class);
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }


    @Test
    public void testCreationTaskWithEndDeviceGroup() {

        EstimationTask estimationTask = createAndSaveTaskWithEndDeviceGroup();

        Optional<? extends EstimationTask> found = estimationService.findEstimationTask(estimationTask.getId());

        assertThat(found).isPresent();

        EstimationTask readingTypeDataExportTask = found.get();
        Assertions.assertThat(readingTypeDataExportTask.getEndDeviceGroup().isPresent()).isTrue();
        Assertions.assertThat(readingTypeDataExportTask.getEndDeviceGroup().get().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(readingTypeDataExportTask.getLastRun()).isEmpty();
        Assertions.assertThat(readingTypeDataExportTask.getNextExecution()).isEqualTo(NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
    }

    @Test
    public void testUpdateTaskWithEndDeviceGroup() {

        EstimationTask estimationTask = createAndSaveTaskWithEndDeviceGroup();

        Optional<? extends EstimationTask> found = estimationService.findEstimationTask(estimationTask.getId());

        assertThat(found).isPresent();

        Instant instant = ZonedDateTime.of(2019, 4, 18, 2, 47, 14, 124000000, ZoneId.of("UTC")).toInstant();

        try (TransactionContext context = transactionService.getContext()) {
            EstimationTask task = found.get();
            task.setNextExecution(instant);
            task.setScheduleExpression(Never.NEVER);
            task.setEndDeviceGroup(anotherEndDeviceGroup);
            task.setName("New name!");
            task.update();
            context.commit();
        }

        found = estimationService.findEstimationTask(estimationTask.getId());

        assertThat(found).isPresent();

        Assertions.assertThat(found.get().getNextExecution()).isEqualTo(instant);
        Assertions.assertThat(found.get().getScheduleExpression()).isEqualTo(Never.NEVER);
        Assertions.assertThat(found.get().getEndDeviceGroup().isPresent()).isTrue();
        Assertions.assertThat(found.get().getEndDeviceGroup().get().getId()).isEqualTo(anotherEndDeviceGroup.getId());
        Assertions.assertThat(found.get().getName()).isEqualTo("New name!");
    }

    @Test
    public void testCreationTaskWithUsagePointGroup() {

        EstimationTask estimationTask = createAndSaveTaskWithUsagePointGroup();

        Optional<? extends EstimationTask> found = estimationService.findEstimationTask(estimationTask.getId());

        assertThat(found).isPresent();

        EstimationTask readingTypeDataExportTask = found.get();
        Assertions.assertThat(readingTypeDataExportTask.getUsagePointGroup().isPresent()).isTrue();
        Assertions.assertThat(readingTypeDataExportTask.getUsagePointGroup().get().getId()).isEqualTo(usagePointGroup.getId());
        assertThat(readingTypeDataExportTask.getLastRun()).isEmpty();
        Assertions.assertThat(readingTypeDataExportTask.getNextExecution()).isEqualTo(NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
    }

    @Test
    public void testUpdateTaskWithUsagePointGroup() {

        EstimationTask estimationTask = createAndSaveTaskWithUsagePointGroup();

        Optional<? extends EstimationTask> found = estimationService.findEstimationTask(estimationTask.getId());

        assertThat(found).isPresent();

        Instant instant = ZonedDateTime.of(2019, 4, 18, 2, 47, 14, 124000000, ZoneId.of("UTC")).toInstant();

        try (TransactionContext context = transactionService.getContext()) {
            EstimationTask task = found.get();
            task.setNextExecution(instant);
            task.setScheduleExpression(Never.NEVER);
            task.setUsagePointGroup(anotherUsagePointGroup);
            task.setName("New name!");
            task.update();
            context.commit();
        }

        found = estimationService.findEstimationTask(estimationTask.getId());

        assertThat(found).isPresent();

        Assertions.assertThat(found.get().getNextExecution()).isEqualTo(instant);
        Assertions.assertThat(found.get().getScheduleExpression()).isEqualTo(Never.NEVER);
        Assertions.assertThat(found.get().getUsagePointGroup().isPresent()).isTrue();
        Assertions.assertThat(found.get().getUsagePointGroup().get().getId()).isEqualTo(anotherUsagePointGroup.getId());
        Assertions.assertThat(found.get().getName()).isEqualTo("New name!");
    }

    private EstimationTask createAndSaveTaskWithEndDeviceGroup() {
        return createAndSaveTaskWithEndDeviceGroup(NAME);
    }

    private EstimationTask createAndSaveTaskWithEndDeviceGroup(String name) {
        EstimationTask estimationTask = null;
        try (TransactionContext context = transactionService.getContext()) {
            estimationTask = createEstimationTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, name);
            context.commit();
        }
        return estimationTask;
    }

    private EstimationTask createAndSaveTaskWithUsagePointGroup() {
        return createAndSaveTaskWithUsagePointGroup(NAME);
    }

    private EstimationTask createAndSaveTaskWithUsagePointGroup(String name) {
        EstimationTask estimationTask = null;
        try (TransactionContext context = transactionService.getContext()) {
            estimationTask = createEstimationTask(lastYear, oneYearBeforeLastYear, usagePointGroup, name);
            context.commit();
        }
        return estimationTask;
    }

    private EstimationTask createEstimationTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup, String name) {
        return estimationService.newBuilder()
                .scheduleImmediately()
                .setName(name)
                .setQualityCodeSystem(QualityCodeSystem.MDC)
                .setEndDeviceGroup(endDeviceGroup)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .create();
    }

    private EstimationTask createEstimationTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, UsagePointGroup usagePointGroup, String name) {
        return estimationService.newBuilder()
                .scheduleImmediately()
                .setName(name)
                .setQualityCodeSystem(QualityCodeSystem.MDM)
                .setUsagePointGroup(usagePointGroup)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .create();
    }

}