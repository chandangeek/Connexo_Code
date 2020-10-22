/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ftpclient.impl.FtpModule;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mail.impl.MailModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.nls.impl.NlsServiceImpl;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
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
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceImplIT {
    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }

    private static final String NAME = "NAME";
    private static final String APPLICATION = "Admin";
    private static final String FORMATTER = "formatter";
    private static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 12, 9, 46, 12, 241615214, TimeZoneNeutral.getMcMurdo());
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(), ZoneId.systemDefault());
    private static final RelativeDate startOfTheYearBeforeLastYear = new RelativeDate(
            YEAR.minus(2),
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private static final RelativeDate startOfLastYear = new RelativeDate(
            YEAR.minus(1),
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private static final RelativeDate startOfThisYear = new RelativeDate(
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );

    private static Injector injector;
    private static DataFormatterFactory dataFormatterFactory = mock(DataFormatterFactory.class);
    private static PropertySpec propertySpec = mock(PropertySpec.class);
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static DataExportServiceImpl dataExportService;
    private static TransactionService transactionService;
    private static MeteringService meteringService;
    private static MeteringGroupsService meteringGroupsService;
    private static TimeService timeService;

    private static RelativePeriod lastYear;
    private static RelativePeriod oneYearBeforeLastYear;
    private static EndDeviceGroup endDeviceGroup;
    private static EnumeratedEndDeviceGroup anotherEndDeviceGroup;
    private static ReadingType readingType, anotherReadingType;

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();
    @Rule
    public TestRule transactional = new TransactionalRule(transactionService);

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private EventService eventService;

    @BeforeClass
    public static void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CalendarModule(),
                    new MeteringModule("0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"),
                    new PartyModule(),
                    new DataVaultModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new H2OrmModule(),
                    new UtilModule(CLOCK),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new ExportModule(),
                    new TimeModule(),
                    new TaskModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new WebServicesModule(),
                    new AuditServiceModule(),
                    new AppServiceModule(),
                    new BasicPropertiesModule(),
                    new MailModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new BpmModule(),
                    new DataVaultModule(),
                    new FtpModule(),
                    new UserModule(),
                    new CustomPropertySetsModule(),
                    new FileImportModule(),
                    new ServiceCallModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            meteringService = injector.getInstance(MeteringService.class);
            dataExportService = (DataExportServiceImpl) injector.getInstance(DataExportService.class);
            timeService = injector.getInstance(TimeService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            return null;
        });
        NlsServiceImpl nlsService = (NlsServiceImpl) injector.getInstance(NlsService.class);
        nlsService.addTranslationKeyProvider(dataExportService);
        nlsService.addMessageSeedProvider(dataExportService);
        readingType = meteringService.getReadingType("0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        anotherReadingType = meteringService.getReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        dataExportService.addFormatter(dataFormatterFactory, ImmutableMap.of(DataExportService.DATA_TYPE_PROPERTY, DataExportService.STANDARD_READING_DATA_TYPE));
        when(dataFormatterFactory.getName()).thenReturn(FORMATTER);
        when(dataFormatterFactory.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(propertySpec.getName()).thenReturn("propy");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        injector.getInstance(ThreadPrincipalService.class).set(() -> "test");
        try (TransactionContext context = transactionService.getContext()) {
            lastYear = timeService.createRelativePeriod("last year", startOfLastYear, startOfThisYear, timeService.getRelativePeriodCategories());
            oneYearBeforeLastYear = timeService.createRelativePeriod("the year before last year", startOfTheYearBeforeLastYear, startOfLastYear, timeService.getRelativePeriodCategories());
            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup()
                    .setName("none")
                    .create();
            anotherEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup()
                    .setName("also none")
                    .create();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testCreateExportTaskWithoutReadingTypes() throws Exception {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setDataFormatterFactoryName(FORMATTER)
                .setName(NAME)
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection();

        assertThatThrownBy(builder::create)
                .isInstanceOf(ConstraintViolationException.class)
                .has(new Condition<>(e -> ((ConstraintViolationException) e).getConstraintViolations().iterator().next().getMessage().equals(MessageSeeds.MUST_SELECT_READING_TYPE.getDefaultFormat()),
                        "wrong message"));
    }

    @Test
    @Transactional
    public void testCreateFindAndRun() {
        IExportTask exportTask = (IExportTask) createAndSaveTask();

        assertThat(dataExportService.findExportTask(exportTask.getId()).map(IExportTask.class::cast)).contains(exportTask);
        assertThat(dataExportService.findAndLockExportTask(exportTask.getId()).map(IExportTask.class::cast)).contains(exportTask);
        assertThat(dataExportService.findAndLockExportTask(exportTask.getId(), exportTask.getVersion()).map(IExportTask.class::cast)).contains(exportTask);

        exportTask.triggerNow();
        RecurrentTask recurrentTask = extractOccurrence(exportTask);
        TaskOccurrence occurrence = injector.getInstance(TaskService.class).getOccurrences(recurrentTask, Range.all()).stream().findFirst().get();
        new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus, CLOCK, threadPrincipalService, eventService).execute(occurrence);

        Optional<IDataExportOccurrence> dataExportOccurrence = dataExportService.findDataExportOccurrence(exportTask, occurrence.getTriggerTime());

        assertThat(dataExportOccurrence).isPresent();

        assertThat(dataExportOccurrence.get().getTask().getId()).isEqualTo(exportTask.getId());
        assertThat(dataExportOccurrence.get().getTriggerTime()).isEqualTo(occurrence.getTriggerTime());
    }

    private RecurrentTask extractOccurrence(IExportTask exportTask) {
        return (RecurrentTask) field("recurrentTask").ofType(Reference.class).in(exportTask).get().get();
    }

    private ExportTask createExportTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup, String name, String application) {
        return dataExportService.newBuilder()
                .scheduleImmediately()
                .setDataFormatterFactoryName(FORMATTER)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .setName(name)
                .setApplication(application)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .create();
    }

    private ExportTask createAndSaveTask() {
        return createAndSaveTask(NAME);
    }

    private ExportTask createAndSaveTask(String name) {
        ExportTask exportTask = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, name, APPLICATION);
        exportTask.update();
        return exportTask;
    }

    @Test
    @Transactional
    public void testExportTaskFinder() {
        createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, "T1", "MultiSense");
        ExportTask t2 = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, "T2", "MultiSense");
        ExportTask t3 = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, "T3", "Insight");

        List<? extends ExportTask> exportTasks;
        exportTasks = dataExportService.findExportTasks().ofApplication("MultiSense").setStart(1).setLimit(1).find();
        assertThat(exportTasks).hasSize(1);
        assertThat(exportTasks.get(0).getId()).isEqualTo(t2.getId());

        exportTasks = dataExportService.findExportTasks().ofApplication("Insight").find();
        assertThat(exportTasks).hasSize(1);
        assertThat(exportTasks.get(0).getId()).isEqualTo(t3.getId());
    }
}
