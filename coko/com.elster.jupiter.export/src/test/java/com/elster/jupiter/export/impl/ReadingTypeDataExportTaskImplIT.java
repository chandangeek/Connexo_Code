package com.elster.jupiter.export.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.time.Never;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.time.RelativeField.*;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDataExportTaskImplIT {

    private EnumeratedEndDeviceGroup anotherEndDeviceGroup;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(CronExpressionParser.class).toInstance(new DefaultCronExpressionParser());
            bind(LogService.class).toInstance(logService);
        }
    }

    public static final String FORMATTER = "formatter";

    private static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 12, 9, 46, 12, 241615214, TimeZoneNeutral.getMcMurdo());

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private PropertySpec min, max, consZero;
    @Mock
    private LogService logService;
    @Mock
    private DataProcessorFactory dataProcessorFactory;
    @Mock
    private DataProcessor dataProcessor;
    @Mock
    private PropertySpec propertySpec;

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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(Clock.fixed(NOW.toInstant(), ZoneId.systemDefault())),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new ExportModule(),
                new TimeModule(),
                new TaskModule(),
                new MeteringGroupsModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            dataExportService = (DataExportServiceImpl) injector.getInstance(DataExportService.class);
            timeService = injector.getInstance(TimeService.class);
            meteringService = injector.getInstance(MeteringService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            return null;
        });
        readingType = meteringService.getReadingType("0.0.3.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        anotherReadingType = meteringService.getReadingType("0.0.14.4.19.1.12.0.0.0.0.2.0.0.0.0.72.0").get();
        dataExportService.addResource(dataProcessorFactory);
        when(dataProcessorFactory.getName()).thenReturn(FORMATTER);
        when(dataProcessorFactory.getProperties()).thenReturn(Arrays.asList(propertySpec));
        when(propertySpec.getName()).thenReturn("propy");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }


    @Test
    public void testCreation() {

        ReadingTypeDataExportTask exportTask = createAndSaveTask();

        Optional<? extends ReadingTypeDataExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ReadingTypeDataExportTask readingTypeDataExportTask = found.get();

        assertThat(readingTypeDataExportTask.getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(readingTypeDataExportTask.getExportPeriod().getId()).isEqualTo(lastYear.getId());
        assertThat(readingTypeDataExportTask.getUpdatePeriod()).isPresent();
        assertThat(readingTypeDataExportTask.getUpdatePeriod().get().getId()).isEqualTo(oneYearBeforeLastYear.getId());
        assertThat(readingTypeDataExportTask.getLastRun()).isAbsent();
        assertThat(readingTypeDataExportTask.getNextExecution()).isEqualTo(NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        assertThat(readingTypeDataExportTask.getOccurrences(Range.<Instant>all())).isEmpty();
        assertThat(readingTypeDataExportTask.getStrategy()).isNotNull();
        assertThat(readingTypeDataExportTask.getStrategy().getValidatedDataOption()).isEqualTo(ValidatedDataOption.INCLUDE_ALL);
        assertThat(readingTypeDataExportTask.getStrategy().isExportContinuousData()).isTrue();
        assertThat(readingTypeDataExportTask.getStrategy().isExportUpdate()).isTrue();
        assertThat(readingTypeDataExportTask.getReadingTypes()).containsExactly(readingType);
        assertThat(readingTypeDataExportTask.getProperties()).hasSize(1).contains(entry("propy", BigDecimal.valueOf(100, 0)));
    }

    @Test
    public void testUpdate() {

        ReadingTypeDataExportTask exportTask = createAndSaveTask();

        Optional<? extends ReadingTypeDataExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        Instant instant = ZonedDateTime.of(2019, 4, 18, 2, 47, 14, 124000000, ZoneId.of("UTC")).toInstant();

        try (TransactionContext context = transactionService.getContext()) {
            ReadingTypeDataExportTask readingTypeDataExportTask = found.get();
            readingTypeDataExportTask.setNextExecution(instant);
            readingTypeDataExportTask.setScheduleExpression(Never.NEVER);
            readingTypeDataExportTask.setExportPeriod(oneYearBeforeLastYear);
            readingTypeDataExportTask.setUpdatePeriod(null);
            readingTypeDataExportTask.setEndDeviceGroup(anotherEndDeviceGroup);
            readingTypeDataExportTask.setProperty("propy", BigDecimal.valueOf(20000, 2));
            readingTypeDataExportTask.setName("New name!");
            readingTypeDataExportTask.addReadingType(anotherReadingType);
            readingTypeDataExportTask.removeReadingType(readingType);
            readingTypeDataExportTask.save();
            context.commit();
        }

        found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        assertThat(found.get().getNextExecution()).isEqualTo(instant);
        assertThat(found.get().getScheduleExpression()).isEqualTo(Never.NEVER);
        assertThat(found.get().getExportPeriod().getId()).isEqualTo(oneYearBeforeLastYear.getId());
        assertThat(found.get().getUpdatePeriod()).isAbsent();
        assertThat(found.get().getEndDeviceGroup().getId()).isEqualTo(anotherEndDeviceGroup.getId());
        assertThat(found.get().getProperties().get("propy")).isEqualTo(BigDecimal.valueOf(20000, 2));
        assertThat(found.get().getReadingTypes()).containsExactly(anotherReadingType);
        assertThat(found.get().getName()).isEqualTo("New name!");
    }

    private ReadingTypeDataExportTask createAndSaveTask() {
        lastYear = null;
        oneYearBeforeLastYear = null;
        endDeviceGroup = null;

        try (TransactionContext context = transactionService.getContext()) {
            lastYear = timeService.createRelativePeriod("last year", startOfLastYear, startOfThisYear, Collections.<RelativePeriodCategory>emptyList());
            oneYearBeforeLastYear = timeService.createRelativePeriod("the year before last year", startOfTheYearBeforeLastYear, startOfLastYear, Collections.emptyList());

            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("none");
            endDeviceGroup.save();
            anotherEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("also none");
            anotherEndDeviceGroup.save();

            context.commit();
        }
        ReadingTypeDataExportTask exportTask = null;
        try (TransactionContext context = transactionService.getContext()) {
            exportTask = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup);

            exportTask.save();
            context.commit();
        }
        return exportTask;
    }


    private ReadingTypeDataExportTask createExportTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup) {
        return dataExportService.newBuilder()
                .setExportPeriod(lastYear)
                .scheduleImmediately()
                .setDataProcessorName(FORMATTER)
                .setName("NAME")
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .setUpdatePeriod(oneYearBeforeLastYear)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .exportUpdate(true)
                .exportContinuousData(true)
                .build();
    }

    @Test
    public void testCreateOccurrence() throws Exception {
        ReadingTypeDataExportTask exportTask = null;
        try (TransactionContext context = transactionService.getContext()) {
            RelativePeriod lastYear = timeService.createRelativePeriod("last year", startOfLastYear, startOfThisYear, Collections.<RelativePeriodCategory>emptyList());
            RelativePeriod oneYearBeforeLastYear = timeService.createRelativePeriod("the year before last year", startOfTheYearBeforeLastYear, startOfLastYear, Collections.emptyList());

            EndDeviceGroup endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("none");
            endDeviceGroup.save();

            exportTask = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup);
            exportTask.save();
            context.commit();
        }

        Optional<? extends ReadingTypeDataExportTask> found = dataExportService.findExportTask(exportTask.getId());
        ReadingTypeDataExportTaskImpl task = (ReadingTypeDataExportTaskImpl) found.get();

        RecurrentTask recurrentTask = task.getRecurrentTask();
        try (TransactionContext context = transactionService.getContext()) {
            TaskOccurrence test = recurrentTask.createTaskOccurrence();

            dataExportService.createExportOccurrence(test).persist();
            context.commit();
        }
        List<? extends DataExportOccurrence> occurrences = task.getOccurrences(Range.atLeast(Instant.EPOCH));
        assertThat(occurrences).hasSize(1);
        DataExportOccurrence occ = occurrences.get(0);
        assertThat(occ.getExportedDataInterval()).isNotNull();
    }

    @Test
    public void testReadingTypeDataExportItemPersistence() throws Exception {
        Meter meter = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalArgumentException::new).newMeter("test");

        ReadingTypeDataExportTaskImpl task = createDataExportTask();
        try (TransactionContext context = transactionService.getContext()) {
            meter.save();
            task.addExportItem(meter, "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            task.save();
            context.commit();
        }

        ReadingTypeDataExportTask retrievedTask = dataExportService.findExportTask(task.getId()).orElseThrow(Exception::new);
        List<ReadingTypeDataExportItem> readingTypeDataExportItems = retrievedTask.getExportItems();
        assertThat(readingTypeDataExportItems).hasSize(1);
        ReadingTypeDataExportItem exportItem = readingTypeDataExportItems.get(0);
        assertThat(exportItem.getReadingContainer()).isNotNull();
        assertThat(exportItem.getReadingContainer()).isEqualTo(meter);
        assertThat(exportItem.getTask()).isEqualTo(task);
        assertThat(exportItem.getReadingTypeMRId()).isEqualTo("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(exportItem.getLastRun()).isAbsent();
        assertThat(exportItem.getLastExportedDate()).isAbsent();
    }

    private ReadingTypeDataExportTaskImpl createDataExportTask() {
        ReadingTypeDataExportTaskImpl exportTask;
        try (TransactionContext context = transactionService.getContext()) {
            RelativePeriod lastYear = timeService.createRelativePeriod("last year", startOfLastYear, startOfThisYear, Collections.<RelativePeriodCategory>emptyList());
            RelativePeriod oneYearBeforeLastYear = timeService.createRelativePeriod("the year before last year", startOfTheYearBeforeLastYear, startOfLastYear, Collections.emptyList());

            EnumeratedEndDeviceGroup endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("none");
            endDeviceGroup.save();

            exportTask = (ReadingTypeDataExportTaskImpl) createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup);
            context.commit();
        }
        return exportTask;
    }
}