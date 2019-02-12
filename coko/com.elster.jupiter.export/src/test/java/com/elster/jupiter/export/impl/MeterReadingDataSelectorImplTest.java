/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Finder;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingDataSelectorImplTest {

    private static final ZonedDateTime UPDATE_START = ZonedDateTime.of(2014, 5, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime START = ZonedDateTime.of(2014, 6, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END = ZonedDateTime.of(2014, 7, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<Instant> EXPORTED_INTERVAL = Range.openClosed(START.toInstant(), END.toInstant());
    private static final Range<Instant> UPDATE_INTERVAL = Range.openClosed(UPDATE_START.toInstant(), START.toInstant());
    private static final ZonedDateTime SINCE = ZonedDateTime.of(2014, 6, 15, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime UPDATED_WINDOW_START = ZonedDateTime.of(2014, 5, 24, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime UPDATED_RECORD_TIME = ZonedDateTime.of(2014, 5, 24, 14, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime UPDATED_WINDOW_END = ZonedDateTime.of(2014, 5, 25, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<Instant> UPDATE_WINDOW_INTERVAL = Range.openClosed(UPDATED_WINDOW_START.toInstant(), UPDATED_WINDOW_END.toInstant());
    public static final String READING_TYPE_MRID = "1.0.0.21.12.0.0.0";
    public static final String END_DEVICE_GROUP_ID = "12" ;

    @Rule
    public TestRule useAnUncommonZoneId = Using.timeZoneOfMcMurdo();

    private Clock clock = Clock.systemDefaultZone();
    private TransactionService transactionService;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DataModel dataModel;
    @Mock(extraInterfaces = DefaultSelectorOccurrence.class)
    private IDataExportOccurrence occurrence;
    @Mock
    private IExportTask task;
    @Mock
    private RelativePeriod exportPeriod, updatePeriod, updateWindow;
    @Mock
    private EndDeviceGroup endDeviceGroup ;
    @Mock
    private Membership<EndDevice> membership1, membership2;
    @Mock
    private Meter meter1, meter2;
    @Mock
    private ReadingType readingType;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private Validator validator;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private ReadingRecord readingRecord1, readingRecord2, readingRecord3, readingRecord4, extraReadingForUpdate, extraReading;
    @Mock
    private ValidationService validationService;
    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock
    private ReadingQualityRecord suspectReadingQuality;
    @Mock
    private Logger logger;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;
    @Mock
    private DataMapper<ReadingTypeInDataSelector> readingTypeInDataSelector;
    @Mock
    Finder.JournalFinder<ReadingTypeInDataSelector> readingTypeInDataSelectorJrnl;
    @Mock
    TaskOccurrence taskOccurrence;
    @Mock
    MeteringGroupsService meteringGroupsService;

    @Before
    public void setUp() {
        transactionService = new TransactionVerifier();

        doAnswer(invocation -> new MeterReadingSelectorConfigImpl(dataModel, meteringGroupsService))
                .when(dataModel).getInstance(MeterReadingSelectorConfigImpl.class);
        doAnswer(invocation -> new ReadingTypeInDataSelector(meteringService))
                .when(dataModel).getInstance(ReadingTypeInDataSelector.class);
        doAnswer(invocation -> new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel) {
            @Override
            public long getId() {
                return this.hashCode();

            }
        })
                .when(dataModel).getInstance(ReadingTypeDataExportItemImpl.class);
        doAnswer(invocation -> new MeterReadingSelector(dataModel, transactionService, thesaurus))
                .when(dataModel).getInstance(MeterReadingSelector.class);
        doAnswer(invocation -> new MeterReadingItemDataSelector(clock, validationService, thesaurus, transactionService, threadPrincipalService))
                .when(dataModel).getInstance(MeterReadingItemDataSelector.class);
        doAnswer(invocation -> new FakeRefAny(invocation.getArguments()[0])).when(dataModel).asRefAny(any());

        endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(2L);
        when(endDeviceGroup.getName()).thenReturn("groupLOri");
        when(meteringGroupsService.findEndDeviceGroup(anyLong())).thenReturn(Optional.of(endDeviceGroup)); //dragos

        when(threadPrincipalService.getLocale()).thenReturn(Locale.US);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenAnswer(invocation1 -> {
                String defaultFormat = ((MessageSeed) invocation.getArguments()[0]).getDefaultFormat();
                return MessageFormat.format(defaultFormat, invocation1.getArguments());
            });
            return messageFormat;
        });

        doReturn(validatorFactory).when(dataModel).getValidatorFactory();
        doReturn(validator).when(validatorFactory).getValidator();

        doReturn(EXPORTED_INTERVAL).when((DefaultSelectorOccurrence) occurrence).getExportedDataInterval();
        doReturn(Arrays.asList(membership1, membership2)).when(endDeviceGroup).getMembers(EXPORTED_INTERVAL);
        doReturn(meter1).when(membership1).getMember();
        doReturn(meter2).when(membership2).getMember();
        doReturn("meter1").when(meter1).getMRID();
        doReturn("meter2").when(meter2).getMRID();
        doReturn(Optional.of(meter1)).when(meter1).getMeter(any());
        doReturn(Optional.of(meter2)).when(meter2).getMeter(any());
        doReturn(true).when(meter1).is(meter1);
        doReturn(true).when(meter2).is(meter2);
        doReturn(TimeZoneNeutral.getMcMurdo()).when(meter1).getZoneId();
        doReturn(TimeZoneNeutral.getMcMurdo()).when(meter2).getZoneId();
        doReturn(Optional.empty()).when(meter1).getUsagePoint(any());
        doReturn(Optional.empty()).when(meter2).getUsagePoint(any());
        doReturn(ImmutableSet.of(readingType)).when(meter1).getReadingTypes(EXPORTED_INTERVAL);
        doReturn(ImmutableSet.of(readingType)).when(meter2).getReadingTypes(EXPORTED_INTERVAL);
        doReturn(task).when(occurrence).getTask();
        doReturn(Arrays.asList(readingRecord1)).when(meter1).getReadings(EXPORTED_INTERVAL, readingType);
        doReturn(Arrays.asList(readingRecord2)).when(meter2).getReadings(EXPORTED_INTERVAL, readingType);
        doReturn(Arrays.asList(readingRecord3)).when(meter1).getReadingsUpdatedSince(UPDATE_INTERVAL, readingType, SINCE.toInstant());
        doReturn(Arrays.asList(readingRecord4)).when(meter2).getReadingsUpdatedSince(UPDATE_INTERVAL, readingType, SINCE.toInstant());
        doReturn(Arrays.asList(readingRecord3, extraReadingForUpdate)).when(meter1).getReadings(UPDATE_WINDOW_INTERVAL, readingType);
        doReturn(Arrays.asList(readingRecord4, extraReadingForUpdate)).when(meter2).getReadings(UPDATE_WINDOW_INTERVAL, readingType);
        doReturn(END.toInstant()).when(readingRecord1).getTimeStamp();
        doReturn(END.toInstant()).when(readingRecord2).getTimeStamp();
        doReturn(UPDATED_RECORD_TIME.toInstant()).when(readingRecord3).getTimeStamp();
        doReturn(UPDATED_RECORD_TIME.toInstant()).when(readingRecord4).getTimeStamp();
        doReturn(SINCE.toInstant()).when(occurrence).getTriggerTime();
        doReturn(UPDATE_INTERVAL).when(updatePeriod).getOpenClosedInterval(any());
        doReturn(READING_TYPE_MRID).when(readingType).getMRID();
        doReturn(UPDATE_WINDOW_INTERVAL).when(updateWindow).getOpenClosedInterval(UPDATED_RECORD_TIME);
        doReturn(validationEvaluator).when(validationService).getEvaluator();
        doReturn(Optional.of(occurrence)).when(occurrence).getDefaultSelectorOccurrence();
        doReturn(true).when(validationEvaluator).isValidationEnabled(any(), any());

        doReturn(Optional.empty()).when(occurrence).getRetryTime();
        //doReturn(Instant.now()).when(occurrence).getTriggerTime();
        when(dataModel.<ReadingTypeInDataSelector>mapper(any())).thenReturn(readingTypeInDataSelector);

        ReadingTypeInDataSelector readingTypeSelector = mock(ReadingTypeInDataSelector.class);
        JournalEntry<ReadingTypeInDataSelector> readingTypeJournal = new JournalEntry<>(Instant.ofEpochMilli(1455245L), readingTypeSelector);

        when(readingTypeInDataSelector.at(any())).thenReturn(readingTypeInDataSelectorJrnl);
        when(readingTypeInDataSelectorJrnl.find(anyMap())).thenReturn(Arrays.asList(readingTypeJournal));
        when(readingTypeSelector.getReadingType()).thenReturn(readingType);

        doReturn(taskOccurrence).when(occurrence).getTaskOccurrence();
        doReturn(Optional.empty()).when(taskOccurrence).getAdhocTime();
    }

    @Test
    public void testSelectWithUpdate() {
        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(true)
                .setUpdatePeriod(updatePeriod)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        selectorConfig.getActiveItems(occurrence).stream()
                .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                .forEach(IReadingTypeDataExportItem::update);

        collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("export"));

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("update"));

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("export"));

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("update"));
    }

    @Test
    public void testSelectWithUpdateAndWindow() {
        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(true)
                .setUpdatePeriod(updatePeriod)
                .setUpdateWindow(updateWindow)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        selectorConfig.getActiveItems(occurrence).stream()
                .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                .forEach(IReadingTypeDataExportItem::update);

        collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("export"));

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("export"));

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("update"));
        assertThat(meterReadingData4.getMeterReading().getReadings()).hasSize(2);
    }

    @Test
    public void testSelectWithComplete() {
        when(meter1.toList(eq(readingType), any())).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(eq(readingType), any())).thenReturn(Arrays.asList(START.toInstant(), END.toInstant()));

        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(false)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_ITEM)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(1);
    }

    @Test
    public void testSelectWithUpdateAndWindowAndComplete() {
        when(meter1.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter1.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant()));
        when(meter2.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant(), UPDATED_RECORD_TIME.plusMinutes(5).toInstant()));

        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(true)
                .setUpdatePeriod(updatePeriod)
                .setUpdateWindow(updateWindow)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_ITEM)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        selectorConfig.getActiveItems(occurrence).stream()
                .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                .forEach(IReadingTypeDataExportItem::update);

        collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(3);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        if (((MeterReadingData) collect.get(0)).getItem().getDomainObject().equals(meter1)) {
            assertMeterReadingData((MeterReadingData) collect.get(0), "export", 1, meter1);
            assertMeterReadingData((MeterReadingData) collect.get(1), "update", 2, meter1);
            assertMeterReadingData((MeterReadingData) collect.get(2), "export", 1, meter2);
        } else {
            assertMeterReadingData((MeterReadingData) collect.get(0), "export", 1, meter2);
            assertMeterReadingData((MeterReadingData) collect.get(1), "export", 1, meter1);
            assertMeterReadingData((MeterReadingData) collect.get(2), "update", 2, meter1);
        }
    }

    @Test
    public void testSelectWithUpdateAndWindowAndValidWithExcludeIntervalSuspectReading() {
        doReturn(Arrays.asList(extraReading, readingRecord1)).when(meter1).getReadings(EXPORTED_INTERVAL, readingType);
        when(extraReading.getTimeStamp()).thenReturn(END.minusMinutes(5).toInstant());

        when(meter1.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter1.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant()));
        when(meter2.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant(), UPDATED_RECORD_TIME.plusMinutes(5).toInstant()));
        when(meter1.getReadingQualities(ImmutableSet.of(QualityCodeSystem.MDC), QualityCodeIndex.SUSPECT, readingType, EXPORTED_INTERVAL)).thenReturn(Collections.singletonList(suspectReadingQuality));
        when(suspectReadingQuality.getReadingTimestamp()).thenReturn(END.toInstant());
        when(occurrence.getRetryTime()).thenReturn(Optional.empty());
        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.of(END.plusMonths(1).toInstant()));

        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        //when(selectorConfig.getEndDeviceGroup().getId()).thenReturn(END_DEVICE_GROUP_ID);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(true)
                .setUpdatePeriod(updatePeriod)
                .setUpdateWindow(updateWindow)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .setValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL);

        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());
            assertThat(collect).hasSize(2);

            selectorConfig.getActiveItems(occurrence).stream()
                    .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                    .forEach(IReadingTypeDataExportItem::update);

            collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

            assertThat(collect).hasSize(4);

            assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
            MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
            assertThat(meterReadingData1.getStructureMarker()
                    .getStructurePath()).isEqualTo(Collections.singletonList("export"));
            assertThat(meterReadingData1.getMeterReading().getReadings()).hasSize(1);

            assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
            MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
            assertThat(meterReadingData2.getStructureMarker()
                    .getStructurePath()).isEqualTo(Collections.singletonList("update"));
            assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

            assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
            MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
            assertThat(meterReadingData3.getStructureMarker()
                    .getStructurePath()).isEqualTo(Collections.singletonList("export"));
            assertThat(meterReadingData3.getMeterReading().getReadings()).hasSize(1);

            assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
            MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
            assertThat(meterReadingData4.getStructureMarker()
                    .getStructurePath()).isEqualTo(Collections.singletonList("update"));
            assertThat(meterReadingData4.getMeterReading().getReadings()).hasSize(2);
    }

    @Test
    public void testSelectWithUpdateAndWindowAndValidWithExcludeIntervalReadingNotValidatedYet() {
        doReturn(Arrays.asList(extraReading, readingRecord1)).when(meter1).getReadings(EXPORTED_INTERVAL, readingType);
        when(extraReading.getTimeStamp()).thenReturn(END.minusMinutes(5).toInstant());

        when(meter1.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter1.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant()));
        when(meter2.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant(), UPDATED_RECORD_TIME.plusMinutes(5).toInstant()));
        when(meter1.getReadingQualities(Collections.emptySet(), QualityCodeIndex.SUSPECT, readingType, EXPORTED_INTERVAL)).thenReturn(Collections.emptyList());

        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.of(END.plusMonths(1).toInstant()));
        when(validationEvaluator.getLastChecked(meter1, readingType)).thenReturn(Optional.of(END.minusMinutes(5).toInstant()));

        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(true)
                .setUpdatePeriod(updatePeriod)
                .setUpdateWindow(updateWindow)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .setValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));


        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        selectorConfig.getActiveItems(occurrence).stream()
                .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                .forEach(IReadingTypeDataExportItem::update);

        collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("export"));
        assertThat(meterReadingData1.getMeterReading().getReadings()).hasSize(1);

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("export"));
        assertThat(meterReadingData3.getMeterReading().getReadings()).hasSize(1);

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList("update"));
        assertThat(meterReadingData4.getMeterReading().getReadings()).hasSize(2);
    }

    @Test
    public void testSelectWithUpdateAndWindowAndValidWithExcludeItemSuspectReading() {
        doReturn(Arrays.asList(extraReading, readingRecord1)).when(meter1).getReadings(EXPORTED_INTERVAL, readingType);
        when(extraReading.getTimeStamp()).thenReturn(END.minusMinutes(5).toInstant());

        when(meter1.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter1.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant()));
        when(meter2.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant(), UPDATED_RECORD_TIME.plusMinutes(5).toInstant()));
        when(meter1.getReadingQualities(ImmutableSet.of(QualityCodeSystem.MDC), QualityCodeIndex.SUSPECT, readingType, EXPORTED_INTERVAL)).thenReturn(Collections.singletonList(suspectReadingQuality));
        when(suspectReadingQuality.getReadingTimestamp()).thenReturn(END.toInstant());

        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.of(END.plusMonths(1).toInstant()));

        MeterReadingSelectorConfigImpl selectorConfig = MeterReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setEndDeviceGroup(endDeviceGroup)
                .addReadingType(readingType)
                .setExportUpdate(true)
                .setUpdatePeriod(updatePeriod)
                .setUpdateWindow(updateWindow)
                .setExportOnlyIfComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .setValidatedDataOption(ValidatedDataOption.EXCLUDE_ITEM);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        List<ExportData> collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

            assertThat(collect).hasSize(1);

            Set<IReadingTypeDataExportItem> activeItems = selectorConfig.getActiveItems(occurrence);
            activeItems.stream()
                    .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                    .forEach(IReadingTypeDataExportItem::update);

            collect = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

            assertThat(collect).hasSize(3);

            assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
            if (((MeterReadingData) collect.get(0)).getItem().getDomainObject().equals(meter2)) {
                assertMeterReadingData((MeterReadingData) collect.get(0), "export", 1, meter2);
                assertMeterReadingData((MeterReadingData) collect.get(1), "update", 2, meter2);
                assertMeterReadingData((MeterReadingData) collect.get(2), "update", 2, meter1);
            } else {
                assertMeterReadingData((MeterReadingData) collect.get(0), "update", 2, meter1);
                assertMeterReadingData((MeterReadingData) collect.get(1), "export", 1, meter2);
                assertMeterReadingData((MeterReadingData) collect.get(2), "update", 2, meter2);
            }
    }

    private void assertMeterReadingData(MeterReadingData data, String expectedStructureMarker, int expectedReadingSize, Meter expectedMeter) {
        assertThat(data.getStructureMarker().getStructurePath()).isEqualTo(Collections.singletonList(expectedStructureMarker));
        assertThat(data.getMeterReading().getReadings()).hasSize(expectedReadingSize);
        assertThat(data.getItem().getDomainObject()).isEqualTo(expectedMeter);
    }

    @Test
    public void testItemDescription() {
        ReadingTypeDataExportItem item = ReadingTypeDataExportItemImpl.from(
                dataModel, dataModel.getInstance(MeterReadingSelectorConfigImpl.class), meter1, readingType);
        when(meter1.getName()).thenReturn("PeriMeter");
        when(readingType.getFullAliasName()).thenReturn("Odium humani generis");

        assertThat(item.getDescription()).isEqualTo("PeriMeter:Odium humani generis");
    }
}
