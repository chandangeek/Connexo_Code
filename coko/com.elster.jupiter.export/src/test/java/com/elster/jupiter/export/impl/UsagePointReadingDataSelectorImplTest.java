/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;
import jersey.repackaged.com.google.common.collect.ImmutableSet;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointReadingDataSelectorImplTest {

    private static final ZonedDateTime START = ZonedDateTime.of(2014, 6, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END = ZonedDateTime.of(2014, 7, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<Instant> EXPORT_INTERVAL = Range.openClosed(START.toInstant(), END.toInstant());
    public static final String USAGE_POINT_GROUP_NAME = "Group";

    private Clock clock = Clock.systemDefaultZone();

    private TransactionService transactionService;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private IExportTask task;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private UsagePoint usagePoint1, usagePoint2;
    @Mock
    private ReadingType readingType;
    @Mock
    private Logger logger;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock(extraInterfaces = DefaultSelectorOccurrence.class)
    private IDataExportOccurrence occurrence;
    @Mock
    private RelativePeriod exportPeriod;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ChannelsContainer channelContainer1, channelContainer2;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private ReadingRecord readingRecord1, readingRecord2;

    @Before
    public void setUp() {
        transactionService = new TransactionVerifier();

        doAnswer(invocation -> new UsagePointReadingSelectorConfigImpl(dataModel))
                .when(dataModel).getInstance(UsagePointReadingSelectorConfigImpl.class);
        doAnswer(invocation -> new ReadingTypeInDataSelector(meteringService))
                .when(dataModel).getInstance(ReadingTypeInDataSelector.class);
        doAnswer(invocation -> new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel))
                .when(dataModel).getInstance(ReadingTypeDataExportItemImpl.class);
        doAnswer(invocation -> new UsagePointReadingSelector(dataModel, transactionService, thesaurus))
                .when(dataModel).getInstance(UsagePointReadingSelector.class);
        doAnswer(invocation -> new UsagePointReadingItemDataSelector(clock, validationService, thesaurus, transactionService, threadPrincipalService))
                .when(dataModel).getInstance(UsagePointReadingItemDataSelector.class);
        doAnswer(invocation -> new FakeRefAny(invocation.getArguments()[0])).when(dataModel).asRefAny(any());
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);

        mockThesaurus();

        when(threadPrincipalService.getLocale()).thenReturn(Locale.US);
        doReturn(Optional.of(occurrence)).when(occurrence).getDefaultSelectorOccurrence();
        when(occurrence.getTask()).thenReturn(task);
        doReturn(EXPORT_INTERVAL).when((DefaultSelectorOccurrence) occurrence).getExportedDataInterval();

        List<Membership<UsagePoint>> memberships = Arrays.asList(mockUsagePointMember(usagePoint1), mockUsagePointMember(usagePoint2));
        when(usagePointGroup.getMembers(EXPORT_INTERVAL)).thenReturn(memberships);
        when(usagePointGroup.getName()).thenReturn(USAGE_POINT_GROUP_NAME);

        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(readingRecord1.getTimeStamp()).thenReturn(START.toInstant());
        when(readingRecord2.getTimeStamp()).thenReturn(END.toInstant());

        mockUsagePointChannel(usagePoint1, channelContainer1, channel1, readingRecord1, readingRecord2);
        mockUsagePointChannel(usagePoint2, channelContainer2, channel2, readingRecord2);
    }

    private void mockThesaurus() {
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenAnswer(invocation1 -> {
                String defaultFormat = ((MessageSeed) invocation.getArguments()[0]).getDefaultFormat();
                return MessageFormat.format(defaultFormat, invocation1.getArguments());
            });
            return messageFormat;
        });
    }

    private Membership<UsagePoint> mockUsagePointMember(UsagePoint usagePoint) {
        Membership<UsagePoint> membership = mock(Membership.class);
        when(membership.getMember()).thenReturn(usagePoint);
        return membership;
    }

    private void mockUsagePointChannel(UsagePoint usagePoint, ChannelsContainer channelContainer, Channel channel, ReadingRecord... readings) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint1 = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint1.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfigurationOnUsagePoint1.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelContainer));
        when(usagePoint.getEffectiveMetrologyConfigurations(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint1));
        when(channelContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(channelContainer.getChannelsContainers()).thenReturn(Collections.singletonList(channelContainer));
        when(channelContainer.overlaps(EXPORT_INTERVAL)).thenReturn(true);
        when(channelContainer.getRange()).thenReturn(EXPORT_INTERVAL);
        when(channelContainer.toList(readingType, EXPORT_INTERVAL)).thenReturn(Arrays.asList(START.toInstant(), END.toInstant()));
        when(channelContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        when(channelContainer.getReadingTypes(EXPORT_INTERVAL)).thenReturn(ImmutableSet.of(readingType));
        when(channel.getChannelsContainer()).thenReturn(channelContainer);
        doReturn(Arrays.asList(readings)).when(channelContainer).getReadings(EXPORT_INTERVAL, readingType);
        when(validationEvaluator.isValidationEnabled(channelContainer, readingType)).thenReturn(true);
    }

    @Test
    public void testSelectOnlyIfComplete() {
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(true)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(1);
        MeterReadingData exportDataItem = (MeterReadingData) exportData.get(0);
        assertThat(exportDataItem.getItem().getDomainObject()).isEqualTo(usagePoint1);
        assertThat(exportDataItem.getItem().getReadingType()).isEqualTo(readingType);
        assertThat(exportDataItem.getValidationData().getValidationStatus(START.toInstant())).isNull();
        assertThat(exportDataItem.getValidationData().getValidationStatus(END.toInstant())).isNull();
        assertThat(exportDataItem.getMeterReading().getReadings()).hasSize(2);
    }

    @Test
    public void testSelectIncomplete() {
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(false)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(2);

        MeterReadingData exportDataItem;
        exportDataItem = (MeterReadingData) exportData.get(0);
        assertThat(exportDataItem.getItem().getDomainObject()).isEqualTo(usagePoint1);
        assertThat(exportDataItem.getItem().getReadingType()).isEqualTo(readingType);
        assertThat(exportDataItem.getValidationData().getValidationStatus(START.toInstant())).isNull();
        assertThat(exportDataItem.getValidationData().getValidationStatus(END.toInstant())).isNull();
        assertThat(exportDataItem.getMeterReading().getReadings()).hasSize(2);

        exportDataItem = (MeterReadingData) exportData.get(1);
        assertThat(exportDataItem.getItem().getDomainObject()).isEqualTo(usagePoint2);
        assertThat(exportDataItem.getItem().getReadingType()).isEqualTo(readingType);
        assertThat(exportDataItem.getValidationData().getValidationStatus(START.toInstant())).isNull();
        assertThat(exportDataItem.getValidationData().getValidationStatus(END.toInstant())).isNull();
        assertThat(exportDataItem.getMeterReading().getReadings()).hasSize(1);
    }

    @Test
    public void testSelectValidItem() {
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(false)
                .setValidatedDataOption(ValidatedDataOption.EXCLUDE_ITEM)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(validationEvaluator.getLastChecked(channelContainer1, readingType)).thenReturn(Optional.empty());
        when(validationEvaluator.getLastChecked(channelContainer2, readingType)).thenReturn(Optional.of(END.toInstant()));

        DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
        List<DataValidationStatus> dataValidationStatuses = Collections.singletonList(dataValidationStatus);
        when(validationEvaluator.getValidationStatus(eq(ImmutableSet.of(QualityCodeSystem.MDM)), eq(channel2), eq(Collections.singletonList(readingRecord2))))
                .thenReturn(dataValidationStatuses);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(END.toInstant());
        when(dataValidationStatus.getValidationResult()).thenReturn(ValidationResult.VALID);

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(1);
        MeterReadingData exportDataItem = (MeterReadingData) exportData.get(0);
        assertThat(exportDataItem.getItem().getDomainObject()).isEqualTo(usagePoint2);
        assertThat(exportDataItem.getItem().getReadingType()).isEqualTo(readingType);
        assertThat(exportDataItem.getValidationData().getValidationStatus(END.toInstant()).getValidationResult()).isEqualTo(ValidationResult.VALID);
        assertThat(exportDataItem.getMeterReading().getReadings()).hasSize(1);
    }

    @Test
    public void testSelectValidReadings() {
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(false)
                .setValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(validationEvaluator.getLastChecked(channelContainer1, readingType)).thenReturn(Optional.empty());
        when(validationEvaluator.getLastChecked(channelContainer2, readingType)).thenReturn(Optional.of(END.toInstant()));

        DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
        List<DataValidationStatus> dataValidationStatuses = Collections.singletonList(dataValidationStatus);
        when(validationEvaluator.getValidationStatus(eq(ImmutableSet.of(QualityCodeSystem.MDM)), eq(channel2), eq(Collections.singletonList(readingRecord2))))
                .thenReturn(dataValidationStatuses);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(END.toInstant());
        when(dataValidationStatus.getValidationResult()).thenReturn(ValidationResult.VALID);
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(END.toInstant());

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(1);
        MeterReadingData exportDataItem = (MeterReadingData) exportData.get(0);
        assertThat(exportDataItem.getItem().getDomainObject()).isEqualTo(usagePoint2);
        assertThat(exportDataItem.getItem().getReadingType()).isEqualTo(readingType);
        assertThat(exportDataItem.getValidationData().getValidationStatus(END.toInstant()).getValidationResult()).isEqualTo(ValidationResult.VALID);
        assertThat(exportDataItem.getMeterReading().getReadings()).hasSize(1);
    }

    @Test
    public void testSelectExcludeSuspectReadings() {
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(false)
                .setValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(validationEvaluator.getLastChecked(channelContainer1, readingType)).thenReturn(Optional.empty());
        when(validationEvaluator.getLastChecked(channelContainer2, readingType)).thenReturn(Optional.of(END.toInstant()));

        DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
        List<DataValidationStatus> dataValidationStatuses = Collections.singletonList(dataValidationStatus);
        when(validationEvaluator.getValidationStatus(eq(ImmutableSet.of(QualityCodeSystem.MDM)), eq(channel2), eq(Collections.singletonList(readingRecord2))))
                .thenReturn(dataValidationStatuses);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(END.toInstant());
        when(dataValidationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(END.toInstant());
        when(channelContainer2.getReadingQualities(eq(ImmutableSet.of(QualityCodeSystem.MDM)), eq(QualityCodeIndex.SUSPECT), eq(readingType), eq(EXPORT_INTERVAL)))
                .thenReturn(Collections.singletonList(readingQuality));

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(0);
    }

    @Test
    public void testUsagePointsHaveNoneOfTheReadingTypes() {
        ReadingType missingReadingType = mock(ReadingType.class);
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(missingReadingType)
                .setExportOnlyIfComplete(false)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(0);

        String expectedLogMessage = thesaurus.getFormat(MessageSeeds.SOME_USAGEPOINTS_HAVE_NONE_OF_THE_SELECTED_READINGTYPES).format(USAGE_POINT_GROUP_NAME);
        Level expectedLogLevel = MessageSeeds.SOME_USAGEPOINTS_HAVE_NONE_OF_THE_SELECTED_READINGTYPES.getLevel();
        verify(logger).log(expectedLogLevel, expectedLogMessage);
    }

    @Test
    public void testSelectDataExcludingAggregatedReadingsInTheMiddleOfInterval() {
        List<Membership<UsagePoint>> memberships = Collections.singletonList(mockUsagePointMember(usagePoint1));
        when(usagePointGroup.getMembers(EXPORT_INTERVAL)).thenReturn(memberships);
        ZonedDateTime MIDDLE = ZonedDateTime.of(2014, 6, 19, 12, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ReadingRecord middleRecord = mock(ReadingRecord.class);
        when(middleRecord.getTimeStamp()).thenReturn(MIDDLE.toInstant());
        doReturn(Arrays.asList(readingRecord1, middleRecord, readingRecord2)).when(channelContainer1).getReadings(EXPORT_INTERVAL, readingType);

        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(false)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(1);
        MeterReadingData data = (MeterReadingData) exportData.get(0);
        List<Reading> exportedReadings = data.getMeterReading().getReadings();
        assertThat(exportedReadings).hasSize(2);
        assertThat(exportedReadings.get(0).getTimeStamp()).isEqualTo(START.toInstant());
        assertThat(exportedReadings.get(1).getTimeStamp()).isEqualTo(END.toInstant());
    }
}
