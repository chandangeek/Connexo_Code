package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDataSelectorImplTest {

    private static final ZonedDateTime UPDATE_START = ZonedDateTime.of(2014, 5, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime START = ZonedDateTime.of(2014, 6, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END = ZonedDateTime.of(2014, 7, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<Instant> EXPORTED_INTERVAL = Range.openClosed(START.toInstant(), END.toInstant());
    private static final Range<Instant> UPDATE_INTERVAL = Range.openClosed(UPDATE_START.toInstant(), START.toInstant());
    private static final Range<ZonedDateTime> UPDATE_PERIOD = Range.openClosed(UPDATE_START, START);
    private static final ZonedDateTime SINCE = ZonedDateTime.of(2014, 6, 15, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime UPDATED_WINDOW_START = ZonedDateTime.of(2014, 5, 24, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime UPDATED_RECORD_TIME = ZonedDateTime.of(2014, 5, 24, 14, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime UPDATED_WINDOW_END = ZonedDateTime.of(2014, 5, 25, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<ZonedDateTime> UPDATE_WINDOW_PERIOD = Range.openClosed(UPDATED_WINDOW_START, UPDATED_WINDOW_END);
    private static final Range<Instant> UPDATE_WINDOW_INTERVAL = Range.openClosed(UPDATED_WINDOW_START.toInstant(), UPDATED_WINDOW_END.toInstant());
    public static final String READING_TYPE_MRID = "1.0.0.21.12.0.0.0";

    @Rule
    public TestRule useAnUncommonZoneId = Using.timeZoneOfMcMurdo();

    private Clock clock = Clock.systemDefaultZone();
    private TransactionService transactionService;

    @Mock
    private MeteringService meteringService;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataExportOccurrence occurrence;
    @Mock
    private IExportTask task;
    @Mock
    private RelativePeriod exportPeriod, updatePeriod, updateWindow;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private EndDeviceMembership membership1, membership2;
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

    @Before
    public void setUp() {
        transactionService = new TransactionVerifier();

        doAnswer(invocation -> new ReadingTypeDataSelectorImpl(dataModel, transactionService, meteringService, validationService, clock))
                .when(dataModel).getInstance(ReadingTypeDataSelectorImpl.class);
        doAnswer(invocation -> new ReadingTypeInDataSelector(meteringService))
                .when(dataModel).getInstance(ReadingTypeInDataSelector.class);
        doAnswer(invocation -> new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel))
                .when(dataModel).getInstance(ReadingTypeDataExportItemImpl.class);
        doAnswer(invocation -> new FakeRefAny(invocation.getArguments()[0])).when(dataModel).asRefAny(any());
        doReturn(validatorFactory).when(dataModel).getValidatorFactory();
        doReturn(validator).when(validatorFactory).getValidator();

        doReturn(EXPORTED_INTERVAL).when(occurrence).getExportedDataInterval();
        doReturn(Arrays.asList(membership1, membership2)).when(endDeviceGroup).getMembers(EXPORTED_INTERVAL);
        doReturn(meter1).when(membership1).getEndDevice();
        doReturn(meter2).when(membership2).getEndDevice();
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

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSelectWithUpdate() {

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(true);
        selector.setUpdatePeriod(updatePeriod);
        selector.setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "export"));

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "update"));

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "export"));

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "update"));
    }

    @Test
    public void testSelectWithUpdateAndWindow() {

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(true);
        selector.setUpdatePeriod(updatePeriod);
        selector.setUpdateWindow(updateWindow);
        selector.setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "export"));

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "export"));

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData4.getMeterReading().getReadings()).hasSize(2);
    }

    @Test
    public void testSelectWithComplete() {

        when(meter1.toList(eq(readingType), any())).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(eq(readingType), any())).thenReturn(Arrays.asList(START.toInstant(), END.toInstant()));

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(false);
        selector.setExportOnlyIfComplete(true);
        selector.setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(1);

    }

    @Test
    public void testSelectWithUpdateAndWindowAndComplete() {

        when(meter1.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter1.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant()));
        when(meter2.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant(), UPDATED_RECORD_TIME.plusMinutes(5).toInstant()));

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(true);
        selector.setUpdatePeriod(updatePeriod);
        selector.setUpdateWindow(updateWindow);
        selector.setExportOnlyIfComplete(true);
        selector.setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(3);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "export"));

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "export"));

    }

    @Test
    public void testSelectWithUpdateAndWindowAndValidWithExcludeIntervalSuspectReading() {
        doReturn(Arrays.asList(extraReading, readingRecord1)).when(meter1).getReadings(EXPORTED_INTERVAL, readingType);
        when(extraReading.getTimeStamp()).thenReturn(END.minusMinutes(5).toInstant());

        when(meter1.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter2.toList(readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(END.toInstant()));
        when(meter1.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant()));
        when(meter2.toList(readingType, UPDATE_WINDOW_INTERVAL)).thenReturn(Arrays.asList(UPDATED_RECORD_TIME.toInstant(), UPDATED_RECORD_TIME.plusMinutes(5).toInstant()));
        when(meter1.getReadingQualities(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(suspectReadingQuality));
        when(suspectReadingQuality.getReadingTimestamp()).thenReturn(END.toInstant());

        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.of(END.plusMonths(1).toInstant()));

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(true);
        selector.setUpdatePeriod(updatePeriod);
        selector.setUpdateWindow(updateWindow);
        selector.setExportOnlyIfComplete(false);
        selector.setValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "export"));
        assertThat(meterReadingData1.getMeterReading().getReadings()).hasSize(1);

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "export"));
        assertThat(meterReadingData3.getMeterReading().getReadings()).hasSize(1);

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "update"));
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
        when(meter1.getReadingQualities(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, EXPORTED_INTERVAL)).thenReturn(Collections.emptyList());

        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.of(END.plusMonths(1).toInstant()));
        when(validationEvaluator.getLastChecked(meter1, readingType)).thenReturn(Optional.of(END.minusMinutes(5).toInstant()));

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(true);
        selector.setUpdatePeriod(updatePeriod);
        selector.setUpdateWindow(updateWindow);
        selector.setExportOnlyIfComplete(false);
        selector.setValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(4);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData1 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData1.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "export"));
        assertThat(meterReadingData1.getMeterReading().getReadings()).hasSize(1);

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "export"));
        assertThat(meterReadingData3.getMeterReading().getReadings()).hasSize(1);

        assertThat(collect.get(3)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(3);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "update"));
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
        when(meter1.getReadingQualities(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, EXPORTED_INTERVAL)).thenReturn(Arrays.asList(suspectReadingQuality));
        when(suspectReadingQuality.getReadingTimestamp()).thenReturn(END.toInstant());

        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.of(END.plusMonths(1).toInstant()));

        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportPeriod, endDeviceGroup);
        selector.addReadingType(readingType);
        selector.setExportUpdate(true);
        selector.setUpdatePeriod(updatePeriod);
        selector.setUpdateWindow(updateWindow);
        selector.setExportOnlyIfComplete(false);
        selector.setValidatedDataOption(ValidatedDataOption.EXCLUDE_ITEM);

        doReturn(Optional.of(selector)).when(task).getReadingTypeDataSelector();

        List<ExportData> collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(1);

        collect = selector.selectData(occurrence).collect(Collectors.toList());

        assertThat(collect).hasSize(3);

        assertThat(collect.get(0)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData2 = (MeterReadingData) collect.get(0);
        assertThat(meterReadingData2.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter1", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData2.getMeterReading().getReadings()).hasSize(2);

        assertThat(collect.get(1)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData3 = (MeterReadingData) collect.get(1);
        assertThat(meterReadingData3.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "export"));

        assertThat(collect.get(2)).isInstanceOf(MeterReadingData.class);
        MeterReadingData meterReadingData4 = (MeterReadingData) collect.get(2);
        assertThat(meterReadingData4.getStructureMarker().getStructurePath()).isEqualTo(Arrays.asList("meter2", "", READING_TYPE_MRID, "update"));
        assertThat(meterReadingData4.getMeterReading().getReadings()).hasSize(2);
    }



    private static class FakeRefAny implements RefAny {
        private final Object value;

        public FakeRefAny(Object value) {
            this.value = value;
        }

        @Override
        public boolean isPresent() {
            return value != null;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public String getComponent() {
            return "";
        }

        @Override
        public String getTableName() {
            return "";
        }

        @Override
        public Object[] getPrimaryKey() {
            return new Object[0];
        }
    }
}