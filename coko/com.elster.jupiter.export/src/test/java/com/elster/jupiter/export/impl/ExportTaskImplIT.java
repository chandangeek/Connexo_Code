/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.FtpDestination;
import com.elster.jupiter.export.FtpsDestination;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.time.Never;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static com.elster.jupiter.time.RelativeField.DAY;
import static com.elster.jupiter.time.RelativeField.HOUR;
import static com.elster.jupiter.time.RelativeField.MINUTES;
import static com.elster.jupiter.time.RelativeField.MONTH;
import static com.elster.jupiter.time.RelativeField.YEAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.when;

public class ExportTaskImplIT extends PersistenceIntegrationTest {

    private static final String NAME = "NAME";
    private static final String APPLICATION = "Admin";
    private static final String FORMATTER = "formatter";

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

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();

    @Mock
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataSelectorFactory dataSelectorFactory;
    @Mock
    private PropertySpec propertySpec;

    private ReadingType readingType, anotherReadingType;
    private RelativePeriod lastYear;
    private RelativePeriod oneYearBeforeLastYear;
    private EndDeviceGroup endDeviceGroup;
    private EnumeratedEndDeviceGroup anotherEndDeviceGroup;
    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose metrologyPurpose;

    private DataExportServiceImpl dataExportService;

    @Before
    public void before() {
        when(dataFormatterFactory.getName()).thenReturn(FORMATTER);
        when(dataFormatterFactory.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(dataSelectorFactory.getName()).thenReturn(NAME);
        when(dataSelectorFactory.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(propertySpec.getName()).thenReturn("propy");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());

        readingType = getMeteringService().getReadingType("0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        anotherReadingType = getMeteringService().getReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        dataExportService = (DataExportServiceImpl) getDataExportService();
        dataExportService.addFormatter(dataFormatterFactory,
                ImmutableMap.of(DataExportService.DATA_TYPE_PROPERTY, DataExportService.STANDARD_READING_DATA_TYPE));
        dataExportService.addSelector(dataSelectorFactory,
                ImmutableMap.of(DataExportService.DATA_TYPE_PROPERTY, DataExportService.STANDARD_READING_DATA_TYPE));
        lastYear = getTimeService().createRelativePeriod("last year", startOfLastYear, startOfThisYear, getTimeService().getRelativePeriodCategories());
        oneYearBeforeLastYear = getTimeService().createRelativePeriod("the year before last year", startOfTheYearBeforeLastYear, startOfLastYear, getTimeService().getRelativePeriodCategories());
        endDeviceGroup = getMeteringGroupsService().createEnumeratedEndDeviceGroup().setName("none").create();
        usagePointGroup = getMeteringGroupsService().createEnumeratedUsagePointGroup().setName("up-group").create();
        metrologyPurpose = getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        anotherEndDeviceGroup = getMeteringGroupsService().createEnumeratedEndDeviceGroup().setName("also none").create();
    }

    private DataExportService getDataExportService() {
        return inMemoryPersistence.getDataExportService();
    }

    private MeteringService getMeteringService() {
        return inMemoryPersistence.getMeteringService();
    }

    private MeteringGroupsService getMeteringGroupsService() {
        return inMemoryPersistence.getMeteringGroupsService();
    }

    private MetrologyConfigurationService getMetrologyConfigurationService(){
        return inMemoryPersistence.getMetrologyConfigurationService();
    }

    private TimeService getTimeService() {
        return inMemoryPersistence.getTimeService();
    }

    @Test
    @Transactional
    public void testNoExportTasks() {
        // Business method
        List<? extends ExportTask> exportTasks = dataExportService.findExportTasks().find();

        // Asserts
        assertThat(exportTasks).isEmpty();
    }

    @Test
    @Transactional
    public void testAddingDestinations() {
        ExportTask exportTask = createAndSaveTask();
        exportTask.addFileDestination("tmp", "file", "csv");
        exportTask.addEmailDestination("info@elster.com", "test report", "file", "csv");
        exportTask.addFtpDestination("ftpServer", 30, "ftpUser", "ftpPassword", "ftpLocation", "ftpFile", "txt");
        exportTask.addFtpsDestination("ftpsServer", 55, "ftpsUser", "ftpsPassword", "ftpsLocation", "ftpsFile", "csv");

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTask taskFromDB = found.get();

        assertThat(taskFromDB.getDestinations()).hasSize(4);

        assertThat(taskFromDB.getDestinations().get(0)).isInstanceOf(FileDestination.class);
        FileDestination fileDestination = (FileDestination) taskFromDB.getDestinations().get(0);
        assertThat(fileDestination.getFileLocation()).isEqualTo("tmp");
        assertThat(fileDestination.getFileName()).isEqualTo("file");
        assertThat(fileDestination.getFileExtension()).isEqualTo("csv");

        assertThat(taskFromDB.getDestinations().get(1)).isInstanceOf(EmailDestination.class);
        EmailDestination emailDestination = (EmailDestination) taskFromDB.getDestinations().get(1);
        assertThat(emailDestination.getRecipients()).isEqualTo("info@elster.com");
        assertThat(emailDestination.getFileName()).isEqualTo("file");
        assertThat(emailDestination.getFileExtension()).isEqualTo("csv");
        assertThat(emailDestination.getSubject()).isEqualTo("test report");

        assertThat(taskFromDB.getDestinations().get(2)).isInstanceOf(FtpDestination.class);
        FtpDestination ftpDestination = (FtpDestination) taskFromDB.getDestinations().get(2);
        assertThat(ftpDestination.getServer()).isEqualTo("ftpServer");
        assertThat(ftpDestination.getPort()).isEqualTo(30);
        assertThat(ftpDestination.getUser()).isEqualTo("ftpUser");
        assertThat(ftpDestination.getPassword()).isEqualTo("ftpPassword");
        assertThat(ftpDestination.getFileLocation()).isEqualTo("ftpLocation");
        assertThat(ftpDestination.getFileName()).isEqualTo("ftpFile");
        assertThat(ftpDestination.getFileExtension()).isEqualTo("txt");

        assertThat(taskFromDB.getDestinations().get(3)).isInstanceOf(FtpsDestination.class);
        FtpsDestination ftpsDestination = (FtpsDestination) taskFromDB.getDestinations().get(3);
        assertThat(ftpsDestination.getServer()).isEqualTo("ftpsServer");
        assertThat(ftpsDestination.getPort()).isEqualTo(55);
        assertThat(ftpsDestination.getUser()).isEqualTo("ftpsUser");
        assertThat(ftpsDestination.getPassword()).isEqualTo("ftpsPassword");
        assertThat(ftpsDestination.getFileLocation()).isEqualTo("ftpsLocation");
        assertThat(ftpsDestination.getFileName()).isEqualTo("ftpsFile");
        assertThat(ftpsDestination.getFileExtension()).isEqualTo("csv");
    }

    @Test
    @Transactional
    public void testRemoveDestinations() {
        ExportTask exportTask = createAndSaveTask();
        exportTask.addFileDestination("tmp", "file", "csv");
        exportTask.addEmailDestination("info@elster.com", "test report", "file", "csv");

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTask taskFromDB = found.get();

        taskFromDB.removeDestination(taskFromDB.getDestinations().get(0));

        found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        taskFromDB = found.get();

        assertThat(taskFromDB.getDestinations()).hasSize(1);
    }

    @Test
    @Transactional
    public void testAddFtpDestination() {
        ExportTask exportTask = createAndSaveTask();
        exportTask.addFtpDestination("elster.com", 21, "user", "password", "testreport", "file", "csv");

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTask taskFromDB = found.get();

        assertThat(taskFromDB.getDestinations()).hasSize(1);

        assertThat(taskFromDB.getDestinations().get(0)).isInstanceOf(FtpDestination.class);
        FtpDestination ftpDestination = (FtpDestination) taskFromDB.getDestinations().get(0);

        assertThat(ftpDestination.getServer()).isEqualTo("elster.com");
        assertThat(ftpDestination.getUser()).isEqualTo("user");
        assertThat(ftpDestination.getPassword()).isEqualTo("password");
        assertThat(ftpDestination.getFileLocation()).isEqualTo("testreport");
        assertThat(ftpDestination.getFileName()).isEqualTo("file");
        assertThat(ftpDestination.getFileExtension()).isEqualTo("csv");
        assertThat(ftpDestination.getPort()).isEqualTo(21);
    }

    @Test
    @Transactional
    public void testAddFtpsDestination() {
        ExportTask exportTask = createAndSaveTask();
        exportTask.addFtpsDestination("elster.com", 20, "user", "password", "testreport", "file", "csv");

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTask taskFromDB = found.get();

        assertThat(taskFromDB.getDestinations()).hasSize(1);

        assertThat(taskFromDB.getDestinations().get(0)).isInstanceOf(FtpsDestination.class);
        FtpsDestination ftpsDestination = (FtpsDestination) taskFromDB.getDestinations().get(0);

        assertThat(ftpsDestination.getServer()).isEqualTo("elster.com");
        assertThat(ftpsDestination.getUser()).isEqualTo("user");
        assertThat(ftpsDestination.getPassword()).isEqualTo("password");
        assertThat(ftpsDestination.getFileLocation()).isEqualTo("testreport");
        assertThat(ftpsDestination.getFileName()).isEqualTo("file");
        assertThat(ftpsDestination.getFileExtension()).isEqualTo("csv");
        assertThat(ftpsDestination.getPort()).isEqualTo(20);
    }

    @Test
    @Transactional
    public void testCreateExportTaskWithMeterReadingSelector() {
        // Business method
        ExportTask exportTask = createAndSaveTask();

        // Asserts
        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTaskImpl task = (ExportTaskImpl) found.get();

        assertThat(task.getLastRun()).isEmpty();
        assertThat(task.getNextExecution()).isEqualTo(ExportInMemoryBootstrapModule.NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        assertThat(task.getProperties()).hasSize(1).contains(entry("propy", BigDecimal.valueOf(100, 0)));
        assertThat(task.getApplication()).isEqualTo(APPLICATION);
        assertThat(task.getOccurrences()).isEmpty();
        assertThat(task.getStandardDataSelectorConfig()).isPresent();

        MeterReadingSelectorConfig selectorConfig = task.getStandardDataSelectorConfig().map(MeterReadingSelectorConfig.class::cast).get();

        assertThat(selectorConfig.getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(selectorConfig.getExportPeriod().getId()).isEqualTo(lastYear.getId());
        assertThat(selectorConfig.getStrategy().getUpdatePeriod()).isPresent();
        assertThat(selectorConfig.getStrategy().getUpdatePeriod().get().getId()).isEqualTo(oneYearBeforeLastYear.getId());
        assertThat(selectorConfig.getStrategy()).isNotNull();
        assertThat(selectorConfig.getStrategy().getValidatedDataOption()).isEqualTo(ValidatedDataOption.INCLUDE_ALL);
        assertThat(selectorConfig.getStrategy().isExportContinuousData()).isTrue();
        assertThat(selectorConfig.getStrategy().isExportUpdate()).isTrue();
        assertThat(selectorConfig.getReadingTypes()).containsExactly(readingType);
    }

    @Test
    @Transactional
    public void testCreateExportTaskWithUsagePointReadingSelector() {
        // Business method
        ExportTask exportTask = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(NAME)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .selectingUsagePointReadings()
                .fromUsagePointGroup(usagePointGroup)
                .fromMetrologyPurpose(metrologyPurpose)
                .fromExportPeriod(lastYear)
                .continuousData(true)
                .exportComplete(MissingDataOption.EXCLUDE_ITEM)
                .withValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL)
                .fromReadingType(readingType)
                .endSelection()
                .create();

        // Asserts
        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTaskImpl task = (ExportTaskImpl) found.get();

        assertThat(task.getLastRun()).isEmpty();
        assertThat(task.getNextExecution()).isEqualTo(ExportInMemoryBootstrapModule.NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        assertThat(task.getProperties()).hasSize(1).contains(entry("propy", BigDecimal.valueOf(100, 0)));
        assertThat(task.getApplication()).isEqualTo(APPLICATION);
        assertThat(task.getOccurrences()).isEmpty();
        assertThat(task.getStandardDataSelectorConfig()).isPresent();

        UsagePointReadingSelectorConfig selectorConfig = task.getStandardDataSelectorConfig().map(UsagePointReadingSelectorConfig.class::cast).get();

        assertThat(selectorConfig.getUsagePointGroup().getId()).isEqualTo(usagePointGroup.getId());
        assertThat(selectorConfig.getMetrologyPurpose().get().getId()).isEqualTo(metrologyPurpose.getId());
        assertThat(selectorConfig.getExportPeriod().getId()).isEqualTo(lastYear.getId());
        assertThat(selectorConfig.getStrategy().getUpdatePeriod()).isEmpty();
        assertThat(selectorConfig.getStrategy()).isNotNull();
        assertThat(selectorConfig.getStrategy().getValidatedDataOption()).isEqualTo(ValidatedDataOption.EXCLUDE_INTERVAL);
        assertThat(selectorConfig.getStrategy().isExportContinuousData()).isTrue();
        assertThat(selectorConfig.getStrategy().isExportUpdate()).isFalse();
        assertThat(selectorConfig.getStrategy().getMissingDataOption()).isEqualTo(MissingDataOption.EXCLUDE_ITEM);
        assertThat(selectorConfig.getReadingTypes()).containsExactly(readingType);
    }

    @Test
    @Transactional
    public void testCreateExportTaskWithEventSelector() {
        ExportTask exportTask = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(NAME)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingEventTypes()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromEventType("4.*.*.*")
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .create();

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        ExportTaskImpl readingTypeDataExportTask = (ExportTaskImpl) found.get();

        assertThat(readingTypeDataExportTask.getLastRun()).isEmpty();
        assertThat(readingTypeDataExportTask.getNextExecution()).isEqualTo(ExportInMemoryBootstrapModule.NOW.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        assertThat(readingTypeDataExportTask.getOccurrences()).isEmpty();
        assertThat(readingTypeDataExportTask.getStandardDataSelectorConfig()).isPresent();

        EventSelectorConfig eventSelectorConfig = readingTypeDataExportTask.getStandardDataSelectorConfig().map(EventSelectorConfig.class::cast).get();

        assertThat(eventSelectorConfig.getEndDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(eventSelectorConfig.getExportPeriod().getId()).isEqualTo(lastYear.getId());
        assertThat(eventSelectorConfig.getStrategy().isExportContinuousData()).isFalse();
        assertThat(eventSelectorConfig.getEventTypeFilters()).hasSize(1);
        assertThat(eventSelectorConfig.getEventTypeFilters().get(0).getCode()).isEqualTo("4.*.*.*");
    }

    @Test
    @Transactional
    public void testHistory() throws InterruptedException {
        inMemoryPersistence.clock.setTicker(new Supplier<Instant>() {
            private ZonedDateTime last = ExportInMemoryBootstrapModule.NOW.plusSeconds(1);

            @Override
            public Instant get() {
                try {
                    return last.toInstant();
                } finally {
                    last = last.plusSeconds(1);
                }
            }
        });

        ExportTask exportTask = createAndSaveTask(); // version 1

        exportTask.addFileDestination("tmp", "file", "csv"); // version 2
        exportTask.addEmailDestination("info@elster.com", "test report", "file", "csv"); // version 3

        exportTask.setName("NEWNAME");
        BigDecimal value1 = new BigDecimal("101");
        exportTask.setProperty("propy", value1);

        exportTask.update(); // version 4

        BigDecimal value2 = new BigDecimal("102");
        exportTask.setProperty("propy", value2);

        exportTask.update(); // version 5

        DataExportDestination dataExportDestination = exportTask.getDestinations().get(0);
        exportTask.removeDestination(dataExportDestination);

        History<? extends ExportTask> history = exportTask.getHistory();

        Optional<? extends ExportTask> version1 = history.getVersion(1);
        assertThat(version1).isPresent();
        assertThat(version1.get().getName()).isEqualTo("NEWNAME"); // name is not journaled because taken from RecurrentTask, but it's ok
        Optional<? extends ExportTask> version2 = history.getVersion(2);
        assertThat(version2).isPresent();
        assertThat(version2.get().getDestinations(version2.get().getModTime().minusSeconds(1))).hasSize(1);
        Optional<? extends ExportTask> version3 = history.getVersion(3);
        assertThat(version3).isPresent();
        assertThat(version3.get().getDestinations(version3.get().getModTime().minusSeconds(1))).hasSize(2);
        Optional<? extends ExportTask> version4 = history.getVersion(4);
        assertThat(version4).isPresent();
        assertThat(version4.get().getName()).isEqualTo("NEWNAME");
        assertThat(version4.get().getProperties(version4.get().getModTime())).containsEntry("propy", value1);
        Optional<? extends ExportTask> version5 = history.getVersion(5);
        assertThat(version5).isPresent();
        assertThat(version5.get().getName()).isEqualTo("NEWNAME");
        assertThat(version5.get().getProperties(version5.get().getModTime())).containsEntry("propy", value2);
        Optional<? extends ExportTask> version6 = history.getVersion(6);
        assertThat(version6).isPresent();
        assertThat(version6.get().getDestinations(version6.get().getModTime().minusSeconds(1))).hasSize(1);
    }

    @Test
    @Transactional
    public void testNameUniqueness() {
        createAndSaveTask();
        try {
            createAndSaveTask();
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);
            assertThat(ex.getConstraintViolations().iterator().next().getPropertyPath().iterator().next().getName()).isEqualTo("name");
        }
        ExportTask task = createAndSaveTask("NAME2");
        task.setName(NAME);
        try {
            task.update();
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);
            assertThat(ex.getConstraintViolations().iterator().next().getPropertyPath().iterator().next().getName()).isEqualTo("name");
        }
    }

    @Test
    @Transactional
    public void testUpdate() {
        ExportTask exportTask = createAndSaveTask();

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        Instant instant = ZonedDateTime.of(2019, 4, 18, 2, 47, 14, 124000000, ZoneId.of("UTC")).toInstant();

        ExportTask task = found.get();
        task.setNextExecution(instant);
        task.setScheduleExpression(Never.NEVER);
        task.setProperty("propy", BigDecimal.valueOf(20000, 2));
        task.setName("New name!");
        MeterReadingSelectorConfig dataSelectorConfig = (MeterReadingSelectorConfig) task.getStandardDataSelectorConfig().get();
        dataSelectorConfig.startUpdate()
                .setExportPeriod(oneYearBeforeLastYear)
                .setUpdatePeriod(null)
                .setEndDeviceGroup(anotherEndDeviceGroup)
                .addReadingType(anotherReadingType)
                .removeReadingType(readingType)
                .complete();
        task.update();

        found = dataExportService.findExportTask(exportTask.getId());

        assertThat(found).isPresent();

        assertThat(found.get().getNextExecution()).isEqualTo(instant);
        assertThat(found.get().getScheduleExpression()).isEqualTo(Never.NEVER);
        assertThat(found.get().getProperties().get("propy")).isEqualTo(BigDecimal.valueOf(20000, 2));
        assertThat(found.get().getName()).isEqualTo("New name!");
        assertThat(found.get().getApplication()).isEqualTo(APPLICATION);

        assertThat(found.get().getStandardDataSelectorConfig()).isPresent();
        found.get().getStandardDataSelectorConfig().get().apply(new DataSelectorConfig.DataSelectorConfigVisitor() {
            @Override
            public void visit(MeterReadingSelectorConfig config) {
                assertThat(config.getExportPeriod().getId()).isEqualTo(oneYearBeforeLastYear.getId());
                assertThat(config.getStrategy().getUpdatePeriod()).isEmpty();
                assertThat(config.getEndDeviceGroup().getId()).isEqualTo(anotherEndDeviceGroup.getId());
                assertThat(config.getReadingTypes()).containsExactly(anotherReadingType);
            }

            @Override
            public void visit(UsagePointReadingSelectorConfig config) {
                fail("Unexpected configuration");
            }

            @Override
            public void visit(EventSelectorConfig config) {
                fail("Unexpected configuration");
            }
        });
    }

    @Test
    @Transactional
    public void testCreateOccurrence() throws Exception {
        ExportTask exportTask = createAndSaveTask();
        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());
        ExportTaskImpl task = (ExportTaskImpl) found.get();
        RecurrentTask recurrentTask = task.getRecurrentTask();
        recurrentTask.triggerNow();
        TaskOccurrence test = inMemoryPersistence.getTaskService().getOccurrences(recurrentTask, Range.all()).stream().findFirst().get();

        dataExportService.createExportOccurrence(test).persist();

        List<? extends DataExportOccurrence> occurrences = task.getOccurrences();

        assertThat(occurrences).hasSize(1);
        DataExportOccurrence occ = occurrences.get(0);
        assertThat(occ.getDefaultSelectorOccurrence()).isPresent();
        assertThat(occ.getDefaultSelectorOccurrence().get().getExportedDataInterval()).isNotNull();
    }

    @Test
    @Transactional
    public void testReadingTypeDataExportItemPersistence() throws Exception {
        ExportTask task = createAndSaveTask();

        Meter meter = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId())
                .orElseThrow(IllegalArgumentException::new)
                .newMeter("test", "myName")
                .create();
        task.getReadingDataSelectorConfig().get().addExportItem(meter, readingType);
        task.update();

        IExportTask retrievedTask = dataExportService.findExportTask(task.getId()).orElseThrow(Exception::new);

        List<? extends ReadingTypeDataExportItem> readingTypeDataExportItems = retrievedTask.getReadingDataSelectorConfig().get().getExportItems();
        assertThat(readingTypeDataExportItems).hasSize(1);
        ReadingTypeDataExportItem exportItem = readingTypeDataExportItems.get(0);
        assertThat(exportItem.getReadingContainer()).isEqualTo(meter);
        assertThat(exportItem.getSelector().getId()).isEqualTo(task.getId());
        assertThat(exportItem.getReadingType()).isEqualTo(readingType);
        assertThat(exportItem.getLastRun()).isEmpty();
        assertThat(exportItem.getLastExportedChangedData()).isEmpty();
        assertThat(exportItem.isActive()).isTrue();

        assertThat(dataExportService.findReadingTypeDataExportItem(exportItem.getId()).map(ReadingTypeDataExportItem.class::cast)).contains(exportItem);
        assertThat(dataExportService.findAndLockReadingTypeDataExportItem(exportItem.getId()).map(ReadingTypeDataExportItem.class::cast)).contains(exportItem);
    }

    @Test
    @Transactional
    public void testReadingTypeDataExportItemInactivePersistence() throws Exception {
        IExportTask task = (IExportTask) createAndSaveTask();
        Meter meter = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId())
                .orElseThrow(IllegalArgumentException::new)
                .newMeter("test", "myName")
                .create();
        ReadingTypeDataExportItem item = task.getReadingDataSelectorConfig().get().addExportItem(meter, readingType);
        item.deactivate();
        item.update();

        IExportTask retrievedTask = dataExportService.findExportTask(task.getId()).orElseThrow(Exception::new);
        List<? extends ReadingTypeDataExportItem> readingTypeDataExportItems = retrievedTask.getReadingDataSelectorConfig().get().getExportItems();
        assertThat(readingTypeDataExportItems).hasSize(1);
        ReadingTypeDataExportItem exportItem = readingTypeDataExportItems.get(0);
        assertThat(exportItem.isActive()).isFalse();
    }

    @Test
    @Transactional
    public void testGetLastOccurrence() throws Exception {
        ExportTask exportTask = createAndSaveTask();

        Optional<? extends ExportTask> found = dataExportService.findExportTask(exportTask.getId());
        ExportTaskImpl task = (ExportTaskImpl) found.get();

        RecurrentTask recurrentTask = task.getRecurrentTask();
        IDataExportOccurrence dataExportOccurrence = null;

        for (int i = 0; i < 3; i++) {
            TimeUnit.MILLISECONDS.sleep(5);
            recurrentTask.triggerNow();

            TaskOccurrence last = recurrentTask.getLastOccurrence().get();
            dataExportOccurrence = dataExportService.createExportOccurrence(last);
            dataExportOccurrence.persist();
        }

        Optional<IDataExportOccurrence> lastOccurrence = task.getLastOccurrence();
        assertThat(lastOccurrence).isPresent().contains(dataExportOccurrence);
    }

    @Test
    @Transactional
    public void testPairUnpairExportTasks() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        assertThat(exportTask1.getPairedTask()).isEmpty();

        ExportTask exportTask2 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("Export2")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .pairWith(exportTask1)
                .create();

        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();

        assertThat(exportTask1.getPairedTask()).contains(exportTask2);
        assertThat(exportTask2.getPairedTask()).contains(exportTask1);

        exportTask1.unpair();
        exportTask1.update();

        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();

        assertThat(exportTask1.getPairedTask()).isEmpty();
        assertThat(exportTask2.getPairedTask()).isEmpty();

        exportTask1.pairWith(exportTask2);
        exportTask1.update();

        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();

        assertThat(exportTask1.getPairedTask()).contains(exportTask2);
        assertThat(exportTask2.getPairedTask()).contains(exportTask1);

        exportTask2.delete();

        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        assertThat(dataExportService.findExportTask(exportTask2.getId())).isEmpty();
        assertThat(exportTask1.getPairedTask()).isEmpty();
    }

    @Test
    @Transactional
    public void testPairAndRename() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        ExportTask exportTask2 = createAndSaveTask("Export2");
        exportTask1.pairWith(exportTask2);
        exportTask1.update();

        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();
        exportTask2.pairWith(exportTask1);
        exportTask2.setName("Ex2");
        exportTask2.update();

        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();
        assertThat(exportTask1.getPairedTask()).contains(exportTask2);
        assertThat(exportTask2.getPairedTask()).contains(exportTask1);
        assertThat(exportTask2.getName()).isEqualTo("Ex2");
    }

    @Test
    @Transactional
    public void testPairExportTaskWithItself() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        assertThatThrownBy(() -> pairAndUpdate(exportTask1, exportTask1))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export task with itself.");
    }

    @Test
    @Transactional
    public void testPairExportTaskWithAlreadyPaired() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        ExportTask exportTask2 = createAndSaveTask("Export2");
        pairAndUpdate(exportTask1, exportTask2);

        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("ExportX")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .pairWith(exportTask2);
        assertThatThrownBy(builder::create)
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Data export task 'Export2' is already paired with '" + NAME + "'.");

        ExportTask exportTask3 = createAndSaveTask("Export3");
        assertThatThrownBy(() -> pairAndUpdate(exportTask3, exportTask1))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Data export task '" + NAME + "' is already paired with 'Export2'.");
    }

    @Test
    @Transactional
    public void testPairAlreadyPairedExportTaskWithAnother() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        ExportTask exportTask2 = createAndSaveTask("Export2");
        pairAndUpdate(exportTask1, exportTask2);

        ExportTask exportTask3 = createAndSaveTask("Export3");
        pairAndUpdate(exportTask1, exportTask3);
        assertThat(exportTask1.getPairedTask()).contains(exportTask3);
        assertThat(exportTask2.getPairedTask()).isEmpty();
        assertThat(exportTask3.getPairedTask()).contains(exportTask1);
        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();
        exportTask3 = dataExportService.findExportTask(exportTask3.getId()).get();
        assertThat(exportTask1.getPairedTask()).contains(exportTask3);
        assertThat(exportTask2.getPairedTask()).isEmpty();
        assertThat(exportTask3.getPairedTask()).contains(exportTask1);

        pairAndUpdate(exportTask3, exportTask2);
        exportTask1 = dataExportService.findExportTask(exportTask1.getId()).get();
        exportTask2 = dataExportService.findExportTask(exportTask2.getId()).get();
        exportTask3 = dataExportService.findExportTask(exportTask3.getId()).get();
        assertThat(exportTask1.getPairedTask()).isEmpty();
        assertThat(exportTask2.getPairedTask()).contains(exportTask3);
        assertThat(exportTask3.getPairedTask()).contains(exportTask2);
    }

    @Test
    @Transactional
    public void testPairExportTaskWithoutDataSources1() throws Exception {
        ExportTask exportTask1 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(NAME)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingEventTypes()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromEventType("4.*.*.*")
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .create();
        ExportTask exportTask2 = createAndSaveTask("Export2");
        assertThatThrownBy(() -> pairAndUpdate(exportTask1, exportTask2))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export task '" + NAME + "' that has no data sources.");
    }

    @Test
    @Transactional
    public void testPairExportTaskWithoutDataSources2() throws Exception {
        ExportTask exportTask1 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(NAME)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingEventTypes()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromEventType("4.*.*.*")
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .create();
        ExportTask exportTask2 = createAndSaveTask("Export2");
        assertThatThrownBy(() -> pairAndUpdate(exportTask2, exportTask1))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export task '" + NAME + "' that has no data sources.");
    }

    @Test
    @Transactional
    public void testCreatePairedExportTaskWithoutDataSources() throws Exception {
        ExportTask exportTask1 = createAndSaveTask("Export2");
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(NAME)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingEventTypes()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromEventType("4.*.*.*")
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .pairWith(exportTask1);
        assertThatThrownBy(builder::create)
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export task '" + NAME + "' that has no data sources.");
    }

    @Test
    @Transactional
    public void testCreateExportTaskPairedWithOneWithoutDataSources() throws Exception {
        ExportTask exportTask1 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(NAME)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingEventTypes()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromEventType("4.*.*.*")
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .create();
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("ExportX")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .pairWith(exportTask1);
        assertThatThrownBy(builder::create)
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export task '" + NAME + "' that has no data sources.");
    }

    @Test
    @Transactional
    public void testPairTasksWithDifferentSelectors() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("ExportX")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings(NAME)
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .pairWith(exportTask1);
        assertThatThrownBy(builder::create)
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export tasks with different data selector types.");

        ExportTask exportTask2 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("Export2")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings(NAME)
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .create();
        assertThatThrownBy(() -> pairAndUpdate(exportTask1, exportTask2))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export tasks with different data selector types.");
    }

    @Test
    @Transactional
    public void testPairTasksWithDifferentApplications() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("ExportX")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication("Lang")
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .pairWith(exportTask1);
        assertThatThrownBy(builder::create)
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export tasks from different applications.");

        ExportTask exportTask2 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("Export2")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication("Lang")
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .create();
        assertThatThrownBy(() -> pairAndUpdate(exportTask1, exportTask2))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export tasks from different applications.");
    }

    @Test
    @Transactional
    public void testPairTasksWithDifferentReadingTypes() throws Exception {
        ExportTask exportTask1 = createAndSaveTask();
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("ExportX")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(anotherReadingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .pairWith(exportTask1);
        assertThatThrownBy(builder::create)
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export tasks without matching reading types.");

        ExportTask exportTask2 = dataExportService.newBuilder()
                .scheduleImmediately()
                .setName("Export2")
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(anotherReadingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .create();
        assertThatThrownBy(() -> pairAndUpdate(exportTask1, exportTask2))
                .isInstanceOf(LocalizedFieldValidationException.class)
                .hasMessage("pairedTask: Can't pair data export tasks without matching reading types.");
    }

    private void pairAndUpdate(ExportTask exportTask1, ExportTask exportTask2) {
        exportTask1.pairWith(exportTask2);
        exportTask1.update();
    }

    private ExportTask createAndSaveTask() {
        return this.createAndSaveTask(NAME);
    }

    private ExportTask createAndSaveTask(String name) {
        return createExportTask(lastYear, oneYearBeforeLastYear, endDeviceGroup, name);
    }

    private ExportTask createExportTask(RelativePeriod lastYear, RelativePeriod oneYearBeforeLastYear, EndDeviceGroup endDeviceGroup, String name) {
        return dataExportService.newBuilder()
                .scheduleImmediately()
                .setName(name)
                .setLogLevel(Level.FINEST.intValue())
                .setApplication(APPLICATION)
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .selectingMeterReadings()
                .fromExportPeriod(lastYear)
                .fromEndDeviceGroup(endDeviceGroup)
                .fromReadingType(readingType)
                .fromUpdatePeriod(oneYearBeforeLastYear)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true)
                .continuousData(true)
                .endSelection()
                .setDataFormatterFactoryName(FORMATTER)
                .addProperty("propy").withValue(BigDecimal.valueOf(100, 0))
                .create();
    }
}
