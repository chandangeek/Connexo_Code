/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Finder;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardDataSelectorTest {

    @Rule
    public TestRule timeZone = Using.timeZoneOfMcMurdo();

    private ZonedDateTime exportPeriodStart;
    private ZonedDateTime exportPeriodEnd;
    private ZonedDateTime triggerTime;
    private ZonedDateTime lastExported;
    private Range<Instant> exportPeriod;
    private LogRecorder logRecorder;
    private TransactionVerifier transactionService;
    private Clock clock = Clock.system(ZoneId.systemDefault());

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private TaskOccurrence occurrence;
    @Mock(extraInterfaces = DefaultSelectorOccurrence.class)
    private IDataExportOccurrence dataExportOccurrence;
    @Mock
    private IExportTask task;
    @Mock
    private EndDeviceGroup group;
    @Mock
    private Membership<EndDevice> endDeviceMembership1, endDeviceMembership2;
    @Mock
    private Meter meter1, meter2, meter3;
    @Mock
    private ReadingTypeDataExportItem existingItem, newItem, obsoleteItem;
    @Mock
    private ReadingType readingType1;
    @Mock
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataExportProperty dataExportProperty;
    @Mock
    private DataFormatter dataFormatter;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private TaskLogHandler taskLogHandler;
    @Mock
    private DataExportStrategy strategy;
    @Mock(extraInterfaces = {IntervalReadingRecord.class})
    private ReadingRecord reading1, reading2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DataModel dataModel;
    @Mock
    private RelativePeriod exportRelativePeriod;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private FormattedData formattedData;
    @Mock
    private ValidationService validationService;
    @Mock
    private EventService eventService;
    @Mock
    private Logger logger;
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private DataMapper<ReadingTypeInDataSelector> readingTypeInDataSelector;
    @Mock
    private Finder.JournalFinder<ReadingTypeInDataSelector> readingTypeInDataSelectorJrnl;
    private DataExportTaskExecutor dataExportTaskExecutor;

    @Before
    public void setUp() {
        exportPeriodStart = ZonedDateTime.of(2012, 11, 10, 6, 0, 0, 0, ZoneId.systemDefault());
        lastExported = ZonedDateTime.of(2012, 11, 5, 6, 0, 0, 0, ZoneId.systemDefault());
        exportPeriodEnd = ZonedDateTime.of(2012, 11, 11, 6, 0, 0, 0, ZoneId.systemDefault());
        triggerTime = ZonedDateTime.of(2012, 11, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        exportPeriod = Range.openClosed(exportPeriodStart.toInstant(), exportPeriodEnd.toInstant());
        logRecorder = new LogRecorder(Level.ALL);

        transactionService = new TransactionVerifier(dataFormatter, newItem, existingItem);

        when(dataModel.getInstance(MeterReadingSelectorConfigImpl.class)).thenAnswer(invocation -> spy(new MeterReadingSelectorConfigImpl(dataModel, meteringGroupsService)));
        when(dataModel.getInstance(ReadingTypeInDataSelector.class)).thenAnswer(invocation -> spy(new ReadingTypeInDataSelector(meteringService)));
        when(dataModel.getInstance(ReadingTypeDataExportItemImpl.class)).thenAnswer(invocation -> {
            ReadingTypeDataExportItem item = spy(new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel) {
                @Override
                public long getId() {
                    return this.hashCode();
                }
            });
            long id = item.getId();
            doReturn(Optional.of(item)).when(dataExportService).findAndLockReadingTypeDataExportItem(id);
            return item;
        });
        when(dataModel.getInstance(MeterReadingSelector.class)).thenAnswer(invocation -> new MeterReadingSelector(dataModel, transactionService, thesaurus));
        when(dataModel.getInstance(MeterReadingItemDataSelector.class)).thenAnswer(invocation -> new MeterReadingItemDataSelector(clock, validationService, thesaurus, transactionService, threadPrincipalService));
        when(dataModel.asRefAny(any())).thenAnswer(invocation -> new FakeRefAny(invocation.getArguments()[0]));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.emptySet());
        when(occurrence.createTaskLogHandler(any())).thenReturn(taskLogHandler);
        when(occurrence.getRetryTime()).thenReturn(Optional.empty());
        when(taskLogHandler.asHandler()).thenReturn(logRecorder);
        when(dataExportService.createExportOccurrence(occurrence)).thenReturn(dataExportOccurrence);
        when(dataExportService.findDataExportOccurrence(occurrence)).thenReturn(Optional.of(dataExportOccurrence));
        StandardDataSelectorFactory dataSelectorFactory = new StandardDataSelectorFactory(thesaurus);
        when(dataExportService.getDataSelectorFactory(DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR)).thenReturn(Optional.of(dataSelectorFactory));
        when(dataExportOccurrence.getRetryTime()).thenReturn(Optional.empty());
        when(dataExportOccurrence.getTask()).thenReturn(task);
        when(dataExportOccurrence.getDefaultSelectorOccurrence()).thenReturn(Optional.of((DefaultSelectorOccurrence) dataExportOccurrence));
        when(((DefaultSelectorOccurrence) dataExportOccurrence).getExportedDataInterval()).thenReturn(exportPeriod);
        when(dataExportOccurrence.getTriggerTime()).thenReturn(triggerTime.toInstant());
        when(task.getDataFormatterFactory()).thenReturn(dataFormatterFactory);
        when(task.getDataSelectorFactory()).thenReturn(dataSelectorFactory);
        when(task.hasDefaultSelector()).thenReturn(true);
        when(task.getDataExportProperties()).thenReturn(Collections.singletonList(dataExportProperty));
        when(task.getDataExportProperties(triggerTime.toInstant())).thenReturn(Collections.singletonList(dataExportProperty));
        when(task.getCompositeDestination()).thenReturn(new CompositeDataExportDestination());
        when(task.getPairedTask()).thenReturn(Optional.empty());
        when(dataExportProperty.getName()).thenReturn("name");
        when(dataExportProperty.getValue()).thenReturn("CSV");
        when(meter1.is(meter1)).thenReturn(true);
        when(meter2.is(meter2)).thenReturn(true);
        when(meter3.is(meter3)).thenReturn(true);
        when(meter1.getReadingTypes(any())).thenReturn(Collections.singleton(readingType1));
        when(meter2.getReadingTypes(any())).thenReturn(Collections.singleton(readingType1));
        when(meter3.getReadingTypes(any())).thenReturn(Collections.singleton(readingType1));
        when(meter1.getMeter(any())).thenReturn(Optional.of(meter1));
        when(meter2.getMeter(any())).thenReturn(Optional.of(meter2));
        when(meter3.getMeter(any())).thenReturn(Optional.of(meter3));
        when(existingItem.getId()).thenReturn(351L);
        doReturn((Optional.of(existingItem))).when(dataExportService).findAndLockReadingTypeDataExportItem(351L);
        when(existingItem.getReadingType()).thenReturn(readingType1);
        when(existingItem.getReadingContainer()).thenReturn(meter2);
        when(meter2.getMeter(any())).thenReturn(Optional.of(meter2));
        when(meter2.getUsagePoint(any())).thenReturn(Optional.empty());
        when(existingItem.getLastExportedChangedData()).thenReturn(Optional.of(lastExported.toInstant()));
        when(newItem.getId()).thenReturn(395L);
        doReturn((Optional.of(newItem))).when(dataExportService).findAndLockReadingTypeDataExportItem(395L);
        when(newItem.getLastExportedChangedData()).thenReturn(Optional.empty());
        when(newItem.getReadingContainer()).thenReturn(meter1);
        when(meter1.getMeter(any())).thenReturn(Optional.of(meter1));
        when(meter1.getUsagePoint(any())).thenReturn(Optional.empty());
        when(newItem.getReadingType()).thenReturn(readingType1);
        when(obsoleteItem.getId()).thenReturn(135L);
        doReturn((Optional.of(obsoleteItem))).when(dataExportService).findAndLockReadingTypeDataExportItem(135L);
        when(obsoleteItem.getReadingType()).thenReturn(readingType1);
        when(obsoleteItem.getReadingContainer()).thenReturn(meter3);
        when(meter3.getMeter(any())).thenReturn(Optional.of(meter3));
        when(meter3.getUsagePoint(any())).thenReturn(Optional.empty());
        when(meteringGroupsService.findEndDeviceGroup(group.getId())).thenReturn(Optional.of(group));
        when(group.getMembers(exportPeriod)).thenReturn(Arrays.asList(endDeviceMembership1, endDeviceMembership2));
        when(endDeviceMembership1.getMember()).thenReturn(meter1);
        when(endDeviceMembership2.getMember()).thenReturn(meter2);
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(dataExportProperty.getName(), dataExportProperty.getValue());
        when(dataFormatterFactory.createDataFormatter(propertyMap)).thenReturn(dataFormatter);
        when(dataFormatterFactory.getPropertySpec("name")).thenReturn(Optional.of(propertySpec));
        when(strategy.isExportContinuousData()).thenReturn(false);
        doReturn(Collections.singletonList(reading1)).when(meter1).getReadings(exportPeriod, readingType1);
        doReturn(Collections.singletonList(reading2)).when(meter2).getReadings(exportPeriod, readingType1);
        when(dataFormatter.processData(any())).thenReturn(formattedData);
        when(reading1.getSource()).thenReturn("reading1");
        when(reading2.getSource()).thenReturn("reading2");
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(threadPrincipalService.getLocale()).thenReturn(Locale.US);

        when(dataModel.<ReadingTypeInDataSelector>mapper(any())).thenReturn(readingTypeInDataSelector);
        ReadingTypeInDataSelector readingTypeSelector = mock(ReadingTypeInDataSelector.class);
        JournalEntry<ReadingTypeInDataSelector> readingTypeJournal = new JournalEntry<>(Instant.ofEpochMilli(1455245L), readingTypeSelector);
        when(readingTypeInDataSelector.at(any())).thenReturn(readingTypeInDataSelectorJrnl);
        when(readingTypeInDataSelectorJrnl.find(anyMapOf(String.class, Object.class))).thenReturn(Collections.singletonList(readingTypeJournal));
        when(readingTypeSelector.getReadingType()).thenReturn(readingType1);
        when(meteringGroupsService.findEndDeviceGroup(anyLong())).thenReturn(Optional.of(group));

        dataExportTaskExecutor = new DataExportTaskExecutor(dataExportService, transactionService, null, thesaurus, clock, threadPrincipalService, eventService);
    }

    @Test
    public void testExecuteObsoleteItemIsDeactivated() {
        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportRelativePeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(group)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .addReadingType(readingType1);
        existingItem = selectorConfig.addExportItem(meter2, readingType1);
        obsoleteItem = selectorConfig.addExportItem(meter3, readingType1);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(task.getStandardDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        dataExportTaskExecutor.postExecute(occurrence);

        InOrder inOrder = inOrder(obsoleteItem);
        inOrder.verify(obsoleteItem).deactivate();
        inOrder.verify(obsoleteItem).update();
    }

    @Test
    public void testExecuteExistingItemIsUpdated() {
        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportRelativePeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(group)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .addReadingType(readingType1);
        existingItem = selectorConfig.addExportItem(meter2, readingType1);
        obsoleteItem = selectorConfig.addExportItem(meter3, readingType1);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(task.getStandardDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        dataExportTaskExecutor.postExecute(occurrence);

        InOrder inOrder = inOrder(existingItem);
        inOrder.verify(existingItem).activate();
    }

    @Test
    public void testNewItemIsUpdated() {
        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportRelativePeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(group)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .addReadingType(readingType1);
        existingItem = selectorConfig.addExportItem(meter2, readingType1);
        obsoleteItem = selectorConfig.addExportItem(meter3, readingType1);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(task.getStandardDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        dataExportTaskExecutor.postExecute(occurrence);

        assertThat(selectorConfig.getExportItems())
                .hasSize(3)
                .contains(existingItem)
                .contains(obsoleteItem);

        assertThat(obsoleteItem.isActive()).isFalse();

        assertThat(selectorConfig.getExportItems().stream()
                .filter(ReadingTypeDataExportItem::isActive)
                .count()).isEqualTo(2);
    }
}
