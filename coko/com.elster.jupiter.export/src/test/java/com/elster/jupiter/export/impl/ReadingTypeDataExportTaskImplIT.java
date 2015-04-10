package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
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
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
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
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
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
import com.elster.jupiter.util.time.Never;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.time.RelativeField.*;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDataExportTaskImplIT {

    public static final String NAME = "NAME";
    private EnumeratedEndDeviceGroup anotherEndDeviceGroup;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LogService.class).toInstance(logService);

            bind (FileImportService.class).toInstance(fileImportService);
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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
    @Mock
    private FileImportService fileImportService;

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
                    new MeteringModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new ExportModule(),
                    new TimeModule(),
                    new TaskModule(),
                    new MeteringGroupsModule(),
                    new AppServiceModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            dataExportService = (DataExportServiceImpl) injector.getInstance(DataExportService.class);
            timeService = injector.getInstance(TimeService.class);
            meteringService = injector.getInstance(MeteringService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            return null;
        });
        readingType = meteringService.getReadingType("0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        anotherReadingType = meteringService.getReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        dataExportService.addResource(dataProcessorFactory);
        when(dataProcessorFactory.getName()).thenReturn(FORMATTER);
        when(dataProcessorFactory.getProperties()).thenReturn(Arrays.asList(propertySpec));
        when(propertySpec.getName()).thenReturn("propy");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        try (TransactionContext context = transactionService.getContext()) {
            lastYear = timeService.createRelativePeriod("last year", startOfLastYear, startOfThisYear, Collections.<RelativePeriodCategory>emptyList());
            oneYearBeforeLastYear = timeService.createRelativePeriod("the year before last year", startOfTheYearBeforeLastYear, startOfLastYear, Collections.emptyList());
            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("none");
            endDeviceGroup.save();
            anotherEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("also none");
            anotherEndDeviceGroup.save();
            context.commit();
        }
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
        assertThat(readingTypeDataExportTask.getOccurrences(/*Range.<Instant>all()*/)).isEmpty();
        assertThat(readingTypeDataExportTask.getStrategy()).isNotNull();
        assertThat(readingTypeDataExportTask.getStrategy().getValidatedDataOption()).isEqualTo(ValidatedDataOption.INCLUDE_ALL);
        assertThat(readingTypeDataExportTask.getStrategy().isExportContinuousData()).isTrue();
        assertThat(readingTypeDataExportTask.getStrategy().isExportUpdate()).isTrue();
        assertThat(readingTypeDataExportTask.getReadingTypes()).containsExactly(readingType);
        assertThat(readingTypeDataExportTask.getProperties()).hasSize(1).contains(entry("propy", BigDecimal.valueOf(100, 0)));
    }

    @Test
    public void testHistory() throws InterruptedException {

        clock.setSubsequent(
                NOW.plusSeconds(1).toInstant(),
                NOW.plusSeconds(2).toInstant(),
                NOW.plusSeconds(3).toInstant(),
                NOW.plusSeconds(4).toInstant(),
                NOW.plusSeconds(5).toInstant(),
                NOW.plusSeconds(6).toInstant(),
                NOW.plusSeconds(7).toInstant(),
                NOW.plusSeconds(8).toInstant()
        );

        ReadingTypeDataExportTask exportTask = createAndSaveTask();

        exportTask.setName("NEWNAME");
        BigDecimal value1 = new BigDecimal("101");
        exportTask.setProperty("propy", value1);

        try (TransactionContext context = transactionService.getContext()) {
            exportTask.save();
            context.commit();
        }

        BigDecimal value2 = new BigDecimal("102");
        exportTask.setProperty("propy", value2);
        try (TransactionContext context = transactionService.getContext()) {
            exportTask.save();
            context.commit();
        }

        History<? extends ReadingTypeDataExportTask> history = exportTask.getHistory();

        Optional<? extends ReadingTypeDataExportTask> version1 = history.getVersion(1);
        assertThat(version1).isPresent();
        assertThat(version1.get().getName()).isEqualTo(NAME);
        Optional<? extends ReadingTypeDataExportTask> version2 = history.getVersion(2);
        assertThat(version2).isPresent();
        assertThat(version2.get().getName()).isEqualTo("NEWNAME");
        assertThat(version2.get().getProperties(NOW.toInstant().plusSeconds(7))).containsEntry("propy", value1);
        Optional<? extends ReadingTypeDataExportTask> version3 = history.getVersion(3);
        assertThat(version3).isPresent();
        assertThat(version3.get().getName()).isEqualTo("NEWNAME");
        assertThat(version3.get().getProperties(NOW.toInstant().plusSeconds(8))).containsEntry("propy", value2);
    }

    @Test
    @Ignore
    public void testNameUniqueness() {
        createAndSaveTask();
        try {
            createAndSaveTask();
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);
            assertThat(ex.getConstraintViolations().iterator().next().getPropertyPath().iterator().next().getName()).isEqualTo("name");
        }
        ReadingTypeDataExportTask task = createAndSaveTask("NAME2");
        task.setName(NAME);
        try (TransactionContext context = transactionService.getContext()) {
            task.save();
            context.commit();
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);
            assertThat(ex.getConstraintViolations().iterator().next().getPropertyPath().iterator().next().getName()).isEqualTo("name");
        }
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
        return createAndSaveTask(NAME);
    }

    private ReadingTypeDataExportTask createAndSaveTask(String name) {
        ReadingTypeDataExportTask exportTask = null;
        try (TransactionContext context = transactionService.getContext()) {
            exportTask = createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, name);

            exportTask.save();
            context.commit();
        }
        return exportTask;
    }


    private ReadingTypeDataExportTask createExportTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup) {
        return createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, NAME);
    }

    private ReadingTypeDataExportTask createExportTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup, String name) {
        return dataExportService.newBuilder()
                .setExportPeriod(lastYear)
                .scheduleImmediately()
                .setDataProcessorName(FORMATTER)
                .setName(name)
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
        ReadingTypeDataExportTask exportTask = createAndSaveTask();

        Optional<? extends ReadingTypeDataExportTask> found = dataExportService.findExportTask(exportTask.getId());
        ReadingTypeDataExportTaskImpl task = (ReadingTypeDataExportTaskImpl) found.get();

        RecurrentTask recurrentTask = task.getRecurrentTask();
        try (TransactionContext context = transactionService.getContext()) {
            recurrentTask.triggerNow();
            TaskOccurrence test = injector.getInstance(TaskService.class).getOccurrences(recurrentTask, Range.<Instant>all()).stream().findFirst().get();

            dataExportService.createExportOccurrence(test).persist();
            context.commit();
        }
        List<? extends DataExportOccurrence> occurrences = task.getOccurrences(/*Range.atLeast(Instant.EPOCH)*/);
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
            task.addExportItem(meter, readingType);
            task.save();
            context.commit();
        }

        ReadingTypeDataExportTask retrievedTask = dataExportService.findExportTask(task.getId()).orElseThrow(Exception::new);
        List<? extends ReadingTypeDataExportItem> readingTypeDataExportItems = retrievedTask.getExportItems();
        assertThat(readingTypeDataExportItems).hasSize(1);
        ReadingTypeDataExportItem exportItem = readingTypeDataExportItems.get(0);
        assertThat(exportItem.getReadingContainer()).isNotNull();
        assertThat(exportItem.getReadingContainer()).isEqualTo(meter);
        assertThat(exportItem.getTask().getId()).isEqualTo(task.getId());
        assertThat(exportItem.getReadingType()).isEqualTo(readingType);
        assertThat(exportItem.getLastRun()).isAbsent();
        assertThat(exportItem.getLastExportedDate()).isAbsent();
        assertThat(exportItem.isActive()).isTrue();
    }

    @Test
    public void testReadingTypeDataExportItemInactivePersistence() throws Exception {
        Meter meter = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalArgumentException::new).newMeter("test");

        ReadingTypeDataExportTaskImpl task = createDataExportTask();
        try (TransactionContext context = transactionService.getContext()) {
            meter.save();
            IReadingTypeDataExportItem item = task.addExportItem(meter, readingType);
            item.deactivate();
            task.save();
            context.commit();
        }

        ReadingTypeDataExportTask retrievedTask = dataExportService.findExportTask(task.getId()).orElseThrow(Exception::new);
        List<? extends ReadingTypeDataExportItem> readingTypeDataExportItems = retrievedTask.getExportItems();
        assertThat(readingTypeDataExportItems).hasSize(1);
        ReadingTypeDataExportItem exportItem = readingTypeDataExportItems.get(0);
        assertThat(exportItem.isActive()).isFalse();
    }

    @Test
    @Ignore // generated query does not work on H2 DB : Karel will look into it
    public void testGetLastOccurrence() throws Exception {
        ReadingTypeDataExportTask exportTask = createAndSaveTask();

        Optional<? extends ReadingTypeDataExportTask> found = dataExportService.findExportTask(exportTask.getId());
        ReadingTypeDataExportTaskImpl task = (ReadingTypeDataExportTaskImpl) found.get();

        RecurrentTask recurrentTask = task.getRecurrentTask();
        IDataExportOccurrence dataExportOccurrence = null;
        try (TransactionContext context = transactionService.getContext()) {
            for (int i = 0; i < 3; i++) {
                TimeUnit.MILLISECONDS.sleep(5);
                recurrentTask.triggerNow();

                TaskOccurrence last = recurrentTask.getLastOccurrence().get();
                dataExportOccurrence = dataExportService.createExportOccurrence(last);
                dataExportOccurrence.persist();
            }
            context.commit();
        }
        Optional<IDataExportOccurrence> lastOccurrence = task.getLastOccurrence();
        assertThat(lastOccurrence).isPresent().contains(dataExportOccurrence);
    }


    private ReadingTypeDataExportTaskImpl createDataExportTask() {
        ReadingTypeDataExportTaskImpl exportTask;
        try (TransactionContext context = transactionService.getContext()) {
            exportTask = (ReadingTypeDataExportTaskImpl) createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup);
            context.commit();
        }
        return exportTask;
    }
}