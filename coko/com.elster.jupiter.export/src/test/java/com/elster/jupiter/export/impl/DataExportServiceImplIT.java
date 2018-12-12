/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ftpclient.impl.FtpModule;
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
import com.elster.jupiter.orm.impl.OrmModule;
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
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import static org.assertj.core.api.Fail.fail;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceImplIT {

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

    private static final String NAME = "NAME";
    private static final String APPLICATION = "Admin";
    private static final String FORMATTER = "formatter";
    private static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 12, 9, 46, 12, 241615214, TimeZoneNeutral.getMcMurdo());
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(), ZoneId.systemDefault());

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();
    private EnumeratedEndDeviceGroup anotherEndDeviceGroup;
    private Injector injector;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
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
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataFormatter dataFormatter;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private HttpService httpService;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private DataExportServiceImpl dataExportService;
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

    @Before
    public void setUp() throws SQLException {
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
                    new OrmModule(),
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
        when(dataFormatterFactory.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
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

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateExportTaskWithoutReadingTypes() throws Exception {
        ExportTask exportTask1;
        try (TransactionContext context = transactionService.getContext()) {
            exportTask1 = dataExportService.newBuilder()
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
                    .endSelection()
                    .create();

            exportTask1.update();
            context.commit();
            fail("expected constraint violation");
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo(MessageSeeds.MUST_SELECT_READING_TYPE.getDefaultFormat());
        }
    }

    @Test
    public void testCreation() {

        IExportTask exportTask = (IExportTask) createAndSaveTask();

        TaskOccurrence occurrence;

        try (TransactionContext context = transactionService.getContext()) {
            exportTask.triggerNow();
            RecurrentTask recurrentTask = extractOccurrence(exportTask);
            occurrence = injector.getInstance(TaskService.class).getOccurrences(recurrentTask, Range.<Instant>all()).stream().findFirst().get();
            new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus, CLOCK, threadPrincipalService).execute(occurrence);

            context.commit();
        }

        Optional<IDataExportOccurrence> dataExportOccurrence = dataExportService.findDataExportOccurrence(exportTask, occurrence.getTriggerTime());

        assertThat(dataExportOccurrence).isPresent();

        assertThat(dataExportOccurrence.get().getTask().getId()).isEqualTo(exportTask.getId());
        assertThat(dataExportOccurrence.get().getTriggerTime()).isEqualTo(occurrence.getTriggerTime());

    }

    private RecurrentTask extractOccurrence(IExportTask exportTask) {
        return (RecurrentTask) field("recurrentTask").ofType(Reference.class).in(exportTask).get().get();
    }

    private ExportTask createExportTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup) {
        return createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, NAME, APPLICATION);
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
        ExportTask exportTask;
        try (TransactionContext context = transactionService.getContext()) {
            exportTask = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, name, APPLICATION);

            exportTask.update();
            context.commit();
        }
        return exportTask;
    }

    @Test
    public void testExportTaskFinder() {
        try (TransactionContext context = transactionService.getContext()) {
            ExportTask t1 = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, "T1", "MultiSense");
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
}
