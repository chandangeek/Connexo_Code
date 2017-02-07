/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.impl.DataValidationStatusImpl;
import com.elster.jupiter.validation.impl.IValidationRule;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ChannelDataUpdater;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChannelResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String BATTERY_LOW = "Battery low";
    private static final Instant NOW = Instant.ofEpochMilli(1410786205000L);
    private static final Date LAST_CHECKED = new Date(1409570229000L);
    private static final Instant LAST_READING = Instant.ofEpochMilli(1410786196000L);
    private static final long CHANNEL_ID1 = 151521354L;
    private static final long startTimeFirst = 1416403197000L;
    private static final long endTimeFirst = 1479561597000L;
    private static final long endTimeSecond = 1489561597000L;
    private static final long startTimeNew = 1469561597000L;
    private static final long endTimeNew = 1499561597000L;
    private static final long INTERVAL_START = 1410774630000L;
    private static final long INTERVAL_END = 1410828630000L;

    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileType loadProfileType;
    @Mock
    private LoadProfileSpec loadProfileSpec;
    @Mock
    private LoadProfileReading loadProfileReading, addedLoadProfileReading, editedProfileReading, removedProfileReading, confirmedProfileReading, missingReadingRecord;
    @Mock
    private ChannelSpec channelSpec;
    @Mock
    private Channel channel;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private IValidationRule validationRule;
    @Mock
    private EstimationRule estimationRule;
    @Mock
    private ReadingQualityRecord quality1, quality2, quality3, quality4;
    @Mock
    private ValidationRuleSet validationRuleSet;
    @Mock
    private ValidationRuleSetVersion validationRuleSetVersion;
    @Mock
    private EstimationRuleSet estimationRuleSet;
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private IntervalReadingRecord readingRecord, addedReadingRecord, editedReadingRecord, confirmedReadingRecord;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;

    private ReadingQualityType readingQualityTypeAdded = new ReadingQualityType("2.7.1");
    private ReadingQualityType readingQualityTypeEdited = new ReadingQualityType("2.7.0");
    private ReadingQualityType readingQualityTypeRejected = new ReadingQualityType("2.7.3");
    private ReadingQualityType readingQualityTypeConfirmedInMDC = new ReadingQualityType("2.10.1");
    private ReadingQualityType readingQualityTypeConfirmedInMDM = new ReadingQualityType("3.10.1");

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeIndex.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeIndex qualityCodeIndex = (QualityCodeIndex) invocationOnMock.getArguments()[0];
                    return qualityCodeIndex.getTranslationKey().getDefaultFormat();
                });
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeSystem.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeSystem qualityCodeSystem = (QualityCodeSystem) invocationOnMock.getArguments()[0];
                    return qualityCodeSystem.getTranslationKey().getDefaultFormat();
                });
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeCategory.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeCategory qualityCodeCategory = (QualityCodeCategory) invocationOnMock.getArguments()[0];
                    return qualityCodeCategory.getTranslationKey().getDefaultFormat();
                });
    }

    @Before
    public void setUpStubs() {
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Arrays.asList(meterActivation));
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getStart()).thenReturn(NOW);
        Interval intervalActivation = Interval.of(Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START), Instant.ofEpochMilli(INTERVAL_END)));
        when(meterActivation.getInterval()).thenReturn(intervalActivation);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("1", 1L)).thenReturn(Optional.of(device));
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(device.getVersion()).thenReturn(1L);
        when(device.getmRID()).thenReturn("1");
        when(device.getZone()).thenReturn(ZoneId.systemDefault());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);
        when(device.getChannels()).thenReturn(Arrays.asList(channel));
        when(loadProfile.getId()).thenReturn(1L);
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(channel));
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileType.getName()).thenReturn("LoadProfileTypeName");
        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START), Instant.ofEpochMilli(INTERVAL_END));
        when(channel.getChannelData(interval)).thenReturn(Arrays.asList(
                loadProfileReading, addedLoadProfileReading, editedProfileReading, removedProfileReading, confirmedProfileReading, missingReadingRecord));
        Range<Instant> interval2 = Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START - 900000), Instant.ofEpochMilli(INTERVAL_START));
        when(channel.getChannelData(interval2)).thenReturn(Arrays.asList(loadProfileReading));
        when(loadProfileReading.getRange()).thenReturn(interval);
        doReturn(BATTERY_LOW).when(thesaurus).getString(BATTERY_LOW, BATTERY_LOW);
        when(loadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, readingRecord));
        when(readingRecord.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord.getReportedDateTime()).thenReturn(LAST_READING);
        when(readingRecord.getTimeStamp()).thenReturn(NOW);
        ReadingQualityRecord readingQualityBatteryLow = mockReadingQuality(ProtocolReadingQualities.BATTERY_LOW.getCimCode());
        ReadingQualityRecord readingQualityPowerFail = mockReadingQuality("2.2.32");
        ReadingQualityRecord readingQualityWrongSystem = mockReadingQuality("112.0.0");//should be filtered out
        ReadingQualityRecord readingQualityDataValid = mockReadingQuality("2.0.0");//should be filtered out
        doReturn(Arrays.asList(readingQualityPowerFail, readingQualityWrongSystem, readingQualityDataValid)).when(readingRecord).getReadingQualities();

        List<ReadingQualityRecord> readingQualities = Arrays.asList(readingQualityBatteryLow);
        Map<Channel, List<ReadingQualityRecord>> readingQualitiesPerChannel = new HashMap<>();
        readingQualitiesPerChannel.put(channel, readingQualities);
        doReturn(readingQualitiesPerChannel).when(loadProfileReading).getReadingQualities();

        when(addedLoadProfileReading.getRange()).thenReturn(interval);
        when(addedLoadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, addedReadingRecord));
        when(addedReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(201, 0));
        when(addedReadingRecord.edited()).thenReturn(true);
        when(addedReadingRecord.getReportedDateTime()).thenReturn(LAST_READING);

        when(editedProfileReading.getRange()).thenReturn(interval);
        when(editedProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, editedReadingRecord));
        when(editedReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(202, 0));
        when(editedReadingRecord.wasAdded()).thenReturn(false);
        when(editedReadingRecord.edited()).thenReturn(true);
        when(editedReadingRecord.getReportedDateTime()).thenReturn(LAST_READING);
        when(editedReadingRecord.getTimeStamp()).thenReturn(NOW);

        when(confirmedProfileReading.getRange()).thenReturn(interval);
        when(confirmedProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, confirmedReadingRecord));
        when(confirmedReadingRecord.wasAdded()).thenReturn(false);
        when(confirmedReadingRecord.edited()).thenReturn(false);
        when(confirmedReadingRecord.confirmed()).thenReturn(true);
        when(confirmedReadingRecord.getReportedDateTime()).thenReturn(LAST_READING);
        when(confirmedReadingRecord.getTimeStamp()).thenReturn(NOW);

        when(removedProfileReading.getRange()).thenReturn(interval);
        when(removedProfileReading.getReadingTime()).thenReturn(LAST_READING);

        when(missingReadingRecord.getRange()).thenReturn(interval);

        when(clock.instant()).thenReturn(NOW);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getId()).thenReturn(CHANNEL_ID1);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channel.getOverflow()).thenReturn(Optional.empty());
        when(channelSpec.getId()).thenReturn(CHANNEL_ID1);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel, NOW)).thenReturn(true);

        DataValidationStatusImpl dataValidationStatus = new DataValidationStatusImpl(Instant.ofEpochMilli(INTERVAL_END), true);
        //add validation quality
        dataValidationStatus.addReadingQuality(quality1, asList(validationRule));
        dataValidationStatus.addBulkReadingQuality(quality1, asList(validationRule));
        when(quality1.getType()).thenReturn(readingQualityTypeAdded);
        when(validationRule.getRuleSetVersion()).thenReturn(validationRuleSetVersion);
        when(validationRule.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRuleSetVersion.getId()).thenReturn(1L);
        when(validationRuleSetVersion.getVersion()).thenReturn(1L);
        when(validationRuleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRuleSet.getName()).thenReturn("ruleSetName");
        doReturn(Arrays.asList(validationRule)).when(validationRuleSet).getRules();
        when(validationRule.isActive()).thenReturn(true);
        //add estimation quality
        dataValidationStatus.addBulkReadingQuality(quality2, Collections.emptyList());
        when(quality2.hasEstimatedCategory()).thenReturn(true);
        when(estimationRule.getId()).thenReturn(13L);
        when(estimationRule.getRuleSet()).thenReturn(estimationRuleSet);
        when(estimationRuleSet.getId()).thenReturn(15L);
        when(estimationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(estimationRule.getName()).thenReturn("EstimationRule");
        ReadingQualityType readingQualityTypeEstimatedByRule = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, (int) estimationRule.getId());
        when(quality2.getType()).thenReturn(readingQualityTypeEstimatedByRule);
        doReturn(Optional.of(estimationRule)).when(estimationService).findEstimationRuleByQualityType(readingQualityTypeEstimatedByRule);
        //add confirm quality
        dataValidationStatus.addBulkReadingQuality(quality3, Collections.emptyList());
        when(quality3.isConfirmed()).thenReturn(true);
        when(quality3.getType()).thenReturn(readingQualityTypeConfirmedInMDM);
        dataValidationStatus.addBulkReadingQuality(quality4, Collections.emptyList());
        when(quality4.isConfirmed()).thenReturn(true);
        when(quality4.getType()).thenReturn(readingQualityTypeConfirmedInMDC);


        when(loadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, dataValidationStatus));
        when(addedLoadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, dataValidationStatus));
        DataValidationStatus statusForBulkEdited = mockDataValidationStatus(true, readingQualityTypeEdited);

        when(editedProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, statusForBulkEdited));
        DataValidationStatus statusForBulkConfirmed = mockDataValidationStatus(true, readingQualityTypeConfirmedInMDC, readingQualityTypeConfirmedInMDM);
        when(confirmedProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, statusForBulkConfirmed));
        DataValidationStatus statusForValueRemoved = mockDataValidationStatus(false, readingQualityTypeRejected);
        when(removedProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, statusForValueRemoved));

        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationRule.getImplementation()).thenReturn("isPrime");
        when(validationRule.getDisplayName()).thenReturn("Primes only");
        ValidationRuleSetVersion validationRuleSetVersion = mock(ValidationRuleSetVersion.class);
        when(validationRuleSetVersion.getRuleSet()).thenReturn(mock(ValidationRuleSet.class));
        when(validationRule.getRuleSetVersion()).thenReturn(validationRuleSetVersion);
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(3);
        when(channel.getNrOfFractionDigits()).thenReturn(3);
        when(channelSpec.getOverflow()).thenReturn(Optional.empty());
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(channel.getLastDateTime()).thenReturn(Optional.of(NOW));
        ReadingType readingType = mockReadingType("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18");
        ReadingType calculatedReadingType = mockReadingType("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.of(calculatedReadingType));
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        Unit unit = Unit.get("kWh");
        when(channel.getLastReading()).thenReturn(Optional.empty());
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        when(channel.getLastDateTime()).thenReturn(Optional.of(NOW));
        when(channel.getUnit()).thenReturn(unit);
        when(channel.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(deviceValidation.getLastChecked(channel)).thenReturn(Optional.of(NOW));

        when(deviceConfiguration.getId()).thenReturn(1L);
        when(deviceConfiguration.getVersion()).thenReturn(1L);
        when(deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId())).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfiguration.getId(), deviceConfiguration.getVersion())).thenReturn(Optional.of(deviceConfiguration));

        when(loadProfile.getVersion()).thenReturn(1L);
        when(loadProfileService.findById(loadProfile.getId())).thenReturn(Optional.of(loadProfile));
        when(loadProfileService.findAndLockLoadProfileByIdAndVersion(loadProfile.getId(), loadProfile.getVersion())).thenReturn(Optional.of(loadProfile));
        when(loadProfile.getDevice()).thenReturn(device);

        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(topologyService.getSlaveChannel(any(Channel.class), any(Instant.class))).thenReturn(Optional.empty());

        when(topologyService.getDataLoggerChannelTimeLine(any(Channel.class), any(Range.class))).thenAnswer(invocationOnMock -> Collections.singletonList(Pair.of(((Channel) invocationOnMock.getArguments()[0]), ((Range<Instant>) invocationOnMock
                .getArguments()[1]))));
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        return readingQuality;
    }

    private DataValidationStatus mockDataValidationStatus(boolean isBulk, ReadingQualityType... readingQualityTypes) {
        DataValidationStatus status = mock(DataValidationStatus.class);
        List<? extends ReadingQualityRecord> readingQualities = Stream.of(readingQualityTypes).map(this::mockReadingQualityRecord).collect(Collectors.toList());
        if (isBulk) {
            doReturn(readingQualities).when(status).getBulkReadingQualities();
        } else {
            doReturn(readingQualities).when(status).getReadingQualities();
        }
        return status;
    }

    private ReadingQualityRecord mockReadingQualityRecord(ReadingQualityType readingQualityType) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.getType()).thenReturn(readingQualityType);
        return readingQualityRecord;
    }

    @Test
    public void test1IntervalOfChannelData() throws UnsupportedEncodingException {
        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/1410774630000/validation")
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.readingQualities")).hasSize(2);

        assertThat(jsonModel.<String>get("$.readingQualities[0].cimCode")).isEqualTo("2.2.32");
        assertThat(jsonModel.<String>get("$.readingQualities[0].indexName")).isEqualTo("Power fail");
        assertThat(jsonModel.<String>get("$.readingQualities[0].systemName")).isEqualTo("MDC");
        assertThat(jsonModel.<String>get("$.readingQualities[0].categoryName")).isEqualTo("Power quality");

        assertThat(jsonModel.<String>get("$.readingQualities[1].cimCode")).isEqualTo("2.0.0");
        assertThat(jsonModel.<String>get("$.readingQualities[1].indexName")).isEqualTo("Data valid");
        assertThat(jsonModel.<String>get("$.readingQualities[1].systemName")).isEqualTo("MDC");
        assertThat(jsonModel.<String>get("$.readingQualities[1].categoryName")).isEqualTo("Valid");
    }

    @Test
    public void testChannelData() throws UnsupportedEncodingException {
        String filter = ExtjsFilter.filter().property("intervalStart", INTERVAL_START).property("intervalEnd", INTERVAL_END).create();
        when(topologyService.getDataLoggerChannelTimeLine(any(Channel.class), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(channel, Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START), Instant
                .ofEpochMilli(INTERVAL_END)))));

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(6);
        assertThat(jsonModel.<Long>get("$.data[0].interval.start")).isEqualTo(INTERVAL_START);
        assertThat(jsonModel.<Long>get("$.data[0].interval.end")).isEqualTo(INTERVAL_END);
        assertThat(jsonModel.<List<?>>get("$.data[0].readingQualities")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].readingQualities[0]")).isEqualTo(BATTERY_LOW);
        assertThat(jsonModel.<String>get("$.data[0].collectedValue")).isEqualTo("200.000");
        assertThat(jsonModel.<String>get("$.data[0].mainValidationInfo.validationResult")).isEqualTo("validationStatus.suspect");

        assertThat(jsonModel.<String>get("$.data[0].bulkValidationInfo.validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<Boolean>get("$.data[0].bulkValidationInfo.estimatedByRule")).isTrue();

        assertThat(jsonModel.<String>get("$.data[0].modificationFlag")).isNull();
        assertThat(jsonModel.<Long>get("$.data[0].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());

        assertThat(jsonModel.<String>get("$.data[1].collectedValue")).isEqualTo("201.000");
        assertThat(jsonModel.<String>get("$.data[1].mainValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.ADDED.name());
        assertThat(jsonModel.<String>get("$.data[1].mainValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[1].mainValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<String>get("$.data[1].bulkValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.ADDED.name());
        assertThat(jsonModel.<String>get("$.data[1].bulkValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[1].bulkValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<Long>get("$.data[1].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<List<?>>get("$.data[1].readingQualities")).hasSize(0);

        assertThat(jsonModel.<String>get("$.data[2].collectedValue")).isEqualTo("202.000");
        assertThat(jsonModel.<String>get("$.data[2].mainValidationInfo.valueModificationFlag")).isNull();
        assertThat(jsonModel.<String>get("$.data[2].bulkValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.EDITED.name());
        assertThat(jsonModel.<String>get("$.data[2].bulkValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[2].bulkValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<Long>get("$.data[2].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<List<?>>get("$.data[2].readingQualities")).hasSize(0);

        assertThat(jsonModel.<String>get("$.data[3].value")).isNull();
        assertThat(jsonModel.<String>get("$.data[3].mainValidationInfo.valueModificationFlag")).isEqualTo("REMOVED");
        assertThat(jsonModel.<String>get("$.data[3].mainValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[3].mainValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<String>get("$.data[3].bulkValidationInfo.valueModificationFlag")).isNull();
        assertThat(jsonModel.<Long>get("$.data[3].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<List<?>>get("$.data[3].readingQualities")).hasSize(0);

        assertThat(jsonModel.<Boolean>get("$.data[4].mainValidationInfo.isConfirmed")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[4].bulkValidationInfo.isConfirmed")).isTrue();
        assertThat(jsonModel.<Long>get("$.data[4].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<List<?>>get("$.data[4].readingQualities")).hasSize(0);

        assertThat(jsonModel.<String>get("$.data[5].mainValidationInfo.validationResult")).isEqualTo("validationStatus.notValidated");
        assertThat(jsonModel.<String>get("$.data[5].bulkValidationInfo.validationResult")).isEqualTo("validationStatus.notValidated");
        assertThat(jsonModel.<List<?>>get("$.data[5].readingQualities")).hasSize(0);
    }

    @Test
    public void testPutChannelData() {
        com.elster.jupiter.metering.Channel meteringChannel = mock(com.elster.jupiter.metering.Channel.class);
        ReadingType readingType = mock(ReadingType.class);
        List list = mock(List.class);
        when(channel.getReadingType()).thenReturn(readingType);
        ChannelDataUpdater channelDataUpdater = mock(ChannelDataUpdater.class);
        when(channelDataUpdater.editChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channelDataUpdater.editBulkChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channelDataUpdater.confirmChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channelDataUpdater.removeChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channel.startEditingData()).thenReturn(channelDataUpdater);
        when(device.getId()).thenReturn(1L);
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(meteringChannel));
        doReturn(Arrays.asList(readingType)).when(meteringChannel).getReadingTypes();
        when(list.contains(readingType)).thenReturn(true);

        ChannelDataInfo channelDataInfo = new ChannelDataInfo();
        channelDataInfo.value = BigDecimal.TEN;
        channelDataInfo.interval = new IntervalInfo();
        channelDataInfo.interval.start = INTERVAL_START;
        channelDataInfo.interval.end = INTERVAL_END;

        List<ChannelDataInfo> infos = new ArrayList<>();
        infos.add(channelDataInfo);

        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/data").request().put(Entity.json(infos));
        verify(channelDataUpdater).complete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testChannelDataFiltered() throws UnsupportedEncodingException {
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String filter = ExtjsFilter.filter()
                .property("intervalStart", 1410774630000L)
                .property("intervalEnd", 1410828630000L)
                .property("suspect", "suspect")
                .create();
        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data").queryParam("filter", filter).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testChannelDataFilteredMatches() throws UnsupportedEncodingException {
        String filter = ExtjsFilter.filter().property("intervalStart", 1410774630000L).property("intervalEnd", 1410828630000L).property("suspect", "suspect").create();
        when(topologyService.getDataLoggerChannelTimeLine(any(Channel.class), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(channel, Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START), Instant
                .ofEpochMilli(INTERVAL_END)))));
        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(5);
    }

    @Test
    public void testValidate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(channel.getLastReading()).thenReturn(Optional.of(LAST_READING));

        LoadProfileTriggerValidationInfo info = new LoadProfileTriggerValidationInfo();
        info.version = device.getVersion();
        info.id = CHANNEL_ID1;
        info.version = loadProfile.getVersion();
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());

        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/validate")
                .request()
                .put(Entity.json(info));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).validateChannel(channel);
    }

    @Test
    public void testValidateWithDate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(channel.getLastReading()).thenReturn(Optional.of(LAST_READING));


        LoadProfileTriggerValidationInfo info = new LoadProfileTriggerValidationInfo();
        info.version = device.getVersion();
        info.id = CHANNEL_ID1;
        info.version = loadProfile.getVersion();
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        info.lastChecked = LAST_CHECKED.getTime();
        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/validate")
                .request()
                .put(Entity.json(info));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).setLastChecked(channel, LAST_CHECKED.toInstant());
        verify(deviceValidation).validateChannel(channel);
    }

    @Test
    public void testChannelInfo() {
        String json = target("devices/1/channels/" + CHANNEL_ID1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        // TODO add items
        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(CHANNEL_ID1);
        assertThat(jsonModel.<Number>get("$.lastValueTimestamp")).isEqualTo(NOW.toEpochMilli());
    }

    @Test
    public void testGetValidationBlocksOnIssueNoBlocks() {
        IssueDataValidation issue = mock(IssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(12L);
        List<NotEstimatedBlock> blocks = new ArrayList<>();
        when(issue.getNotEstimatedBlocks()).thenReturn(blocks);

        String response = target("devices/1/channels/" + CHANNEL_ID1 + "/datavalidationissues/12/validationblocks").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.validationBlocks")).isEmpty();
    }

    @Test
    public void testGetValidationBlocksOnIssue() {
        IssueDataValidation issue = mock(IssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(12L);
        List<NotEstimatedBlock> blocks = new ArrayList<>();
        when(issue.getNotEstimatedBlocks()).thenReturn(blocks);

        Instant now = Instant.now();
        ReadingType bulkReadingType = mockReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType deltaReadingType = mockReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(bulkReadingType.getCalculatedReadingType()).thenReturn(Optional.of(deltaReadingType));
        when(channel.getReadingType()).thenReturn(bulkReadingType);

        blocks.add(mockNotEstimatedBlock(now, now.plus(30, ChronoUnit.MINUTES), bulkReadingType));
        blocks.add(mockNotEstimatedBlock(now.plus(60, ChronoUnit.MINUTES), now.plus(90, ChronoUnit.MINUTES), bulkReadingType));
        blocks.add(mockNotEstimatedBlock(now, now.plus(15, ChronoUnit.MINUTES), deltaReadingType));
        blocks.add(mockNotEstimatedBlock(now.plus(30, ChronoUnit.MINUTES), now.plus(75, ChronoUnit.MINUTES), deltaReadingType));

        String response = target("devices/1/channels/" + CHANNEL_ID1 + "/datavalidationissues/12/validationblocks").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.validationBlocks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.validationBlocks[0].startTime")).isEqualTo(now.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.validationBlocks[0].endTime")).isEqualTo(now.plus(90, ChronoUnit.MINUTES).toEpochMilli());
    }

    private void mockChannelWithCalculatedReadingType(long channelId, String collectedReadingTypeMrid, String calculatedReadingTypeMrid, Optional<BigDecimal> multiplier) {
        ReadingType collectedReadingType = ReadingTypeMockBuilder.from(collectedReadingTypeMrid).getMock();
        ReadingType calculatedReadingType = ReadingTypeMockBuilder.from(calculatedReadingTypeMrid).getMock();
//        when(collectedReadingType.getCalculatedReadingType()).thenReturn(Optional.of(calculatedReadingType));
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getReadingType()).thenReturn(collectedReadingType);
        when(channelSpec.getOverflow()).thenReturn(Optional.empty());
        Channel channelWithBulkAndCalculatedDelta = mock(Channel.class);
        when(channelWithBulkAndCalculatedDelta.getId()).thenReturn(channelId);
        when(channelWithBulkAndCalculatedDelta.getChannelSpec()).thenReturn(channelSpec);
        when(channelWithBulkAndCalculatedDelta.getReadingType()).thenReturn(collectedReadingType);
        when(channelWithBulkAndCalculatedDelta.getCalculatedReadingType(clock.instant())).thenReturn(Optional.of(calculatedReadingType));
        when(channelWithBulkAndCalculatedDelta.getMultiplier(any(Instant.class))).thenReturn(multiplier);
        when(channelWithBulkAndCalculatedDelta.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channelWithBulkAndCalculatedDelta.getLastReading()).thenReturn(Optional.empty());
        when(channelWithBulkAndCalculatedDelta.getLastDateTime()).thenReturn(Optional.empty());
        when(channelWithBulkAndCalculatedDelta.getLoadProfile()).thenReturn(loadProfile);
        when(channelWithBulkAndCalculatedDelta.getDevice()).thenReturn(device);
        when(channelWithBulkAndCalculatedDelta.getOverflow()).thenReturn(Optional.empty());
        Unit collectedUnit = getUnit(collectedReadingType);
        when(channelWithBulkAndCalculatedDelta.getUnit()).thenReturn(collectedUnit);
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(channelWithBulkAndCalculatedDelta));
        when(deviceValidation.getLastChecked(channelWithBulkAndCalculatedDelta)).thenReturn(Optional.of(NOW));
        when(device.getChannels()).thenReturn(Arrays.asList(channelWithBulkAndCalculatedDelta));
    }

    private Unit getUnit(ReadingType rt) {
        Unit unit = Unit.get(rt.getMultiplier().getSymbol() + rt.getUnit().getSymbol());
        if (unit == null) {
            unit = Unit.get(rt.getMultiplier().getSymbol() + rt.getUnit().getUnit().getAsciiSymbol());
        }
        return unit;
    }

    @Test
    public void getWithDeltaCalculatedReadingTypeTest() {
        long channelId = 123L;
        String collectedReadingTypeMrid = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        String calculatedReadingTypeMrid = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        mockChannelWithCalculatedReadingType(channelId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.empty());
        String json = target("devices/1/channels/" + channelId).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(channelId);
        assertThat(jsonModel.<Number>get("$readingType.mRID")).isEqualTo(collectedReadingTypeMrid);
        assertThat(jsonModel.<Number>get("calculatedReadingType.mRID")).isEqualTo(calculatedReadingTypeMrid);
        assertThat(jsonModel.hasPath("multiplier")).isFalse();
    }

    @Test
    public void getWithMultiplierCalculatedReadingTypeTest() {
        long channelId = 123L;
        String collectedReadingTypeMrid = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        String calculatedReadingTypeMrid = "0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.0.72.0";
        BigDecimal multiplier = BigDecimal.TEN;
        mockChannelWithCalculatedReadingType(channelId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        String json = target("devices/1/channels/" + channelId).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(channelId);
        assertThat(jsonModel.<Number>get("$readingType.mRID")).isEqualTo(collectedReadingTypeMrid);
        assertThat(jsonModel.<Number>get("calculatedReadingType.mRID")).isEqualTo(calculatedReadingTypeMrid);
        assertThat(jsonModel.hasPath("multiplier")).isTrue();
        assertThat(jsonModel.<Number>get("multiplier")).isEqualTo(multiplier.intValue());
    }

    private NotEstimatedBlock mockNotEstimatedBlock(Instant from, Instant to, ReadingType readingType) {
        NotEstimatedBlock block = mock(NotEstimatedBlock.class);
        when(block.getStartTime()).thenReturn(from);
        when(block.getEndTime()).thenReturn(to);
        when(block.getReadingType()).thenReturn(readingType);
        return block;
    }

    private CustomPropertySet mockCustomPropertySet() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(1448191220000L));
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);
        Channel channel = mock(Channel.class);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findAndLockChannelSpecByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(channelSpec));
        when(masterDataService.findLoadProfileType(anyLong())).thenReturn(Optional.of(loadProfileType));
        when(masterDataService.findAndLockLoadProfileTypeByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(loadProfileType));
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channel.getId()).thenReturn(1L);
        when(channel.getDevice()).thenReturn(device);
        when(device.getChannels()).thenReturn(Arrays.asList(channel));
        when(channelSpec.getId()).thenReturn(1L);
        when(channelSpec.getVersion()).thenReturn(1L);
        when(channelSpec.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileType.getId()).thenReturn(1L);
        when(loadProfileType.getVersion()).thenReturn(1L);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(device.getLoadProfiles()).thenReturn(Collections.singletonList(loadProfile));
        when(loadProfile.getChannels()).thenReturn(Collections.singletonList(channel));
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("testCps");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getLoadProfileTypeCustomPropertySet(any(LoadProfileType.class))).thenReturn(Optional.of(registeredCustomPropertySet));
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        MdcPropertyUtils mdcPropertyUtils = mock(MdcPropertyUtils.class);
        PropertyInfo propertyInfo = mock(PropertyInfo.class);
        PropertyValueInfo propertyValueInfo = mock(PropertyValueInfo.class);
        when(propertyValueInfo.getValue()).thenReturn("testValue");
        when(propertyInfo.getPropertyValueInfo()).thenReturn(propertyValueInfo);
        when(mdcPropertyUtils.convertPropertySpecsToPropertyInfos(anyObject(), anyObject())).thenReturn(Arrays.asList(propertyInfo));
        CustomPropertySetValues customPropertySetValuesNoTimesliced = CustomPropertySetValues.empty();
        customPropertySetValuesNoTimesliced.setProperty("testnameNoTimesliced", "testValueNoTimesliced");
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(startTimeFirst), Instant.ofEpochMilli(endTimeFirst))));
        customPropertySetValues.setProperty("testname", "testValue1");
        CustomPropertySetValues customPropertySetValues2 = CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(endTimeFirst), Instant.ofEpochMilli(endTimeSecond))));
        customPropertySetValues2.setProperty("testname2", "testValue2");
        when(customPropertySetService.getUniqueValuesFor(eq(customPropertySet), eq(channelSpec), anyObject())).thenReturn(customPropertySetValues);
        when(customPropertySetService.getUniqueValuesFor(eq(customPropertySet), eq(channelSpec), any(Instant.class), anyObject())).thenReturn(customPropertySetValuesNoTimesliced);
        when(customPropertySetService.getAllVersionedValuesFor(eq(customPropertySet), eq(channelSpec), anyObject())).thenReturn(Arrays.asList(customPropertySetValues, customPropertySetValues2));
        ValuesRangeConflict conflict1 = mock(ValuesRangeConflict.class);
        when(conflict1.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(startTimeFirst), Instant.ofEpochMilli(endTimeFirst)));
        when(conflict1.getMessage()).thenReturn("testMessage");
        when(conflict1.getType()).thenReturn(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        when(conflict1.getValues()).thenReturn(customPropertySetValues);
        ValuesRangeConflict conflict2 = mock(ValuesRangeConflict.class);
        when(conflict2.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(startTimeNew), Instant.ofEpochMilli(endTimeNew)));
        when(conflict2.getMessage()).thenReturn("testMessage");
        when(conflict2.getType()).thenReturn(ValuesRangeConflictType.RANGE_INSERTED);
        when(conflict2.getValues()).thenReturn(CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(startTimeNew), Instant.ofEpochMilli(endTimeNew)))));
        ValuesRangeConflict conflict3 = mock(ValuesRangeConflict.class);
        when(conflict3.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(endTimeFirst), Instant.ofEpochMilli(endTimeSecond)));
        when(conflict3.getMessage()).thenReturn("testMessage");
        when(conflict3.getType()).thenReturn(ValuesRangeConflictType.RANGE_OVERLAP_DELETE);
        when(conflict3.getValues()).thenReturn(customPropertySetValues2);
        OverlapCalculatorBuilder overlapCalculatorBuilder = mock(OverlapCalculatorBuilder.class);
        when(overlapCalculatorBuilder.whenCreating(any(Range.class))).thenReturn(Arrays.asList(conflict1, conflict2, conflict3));
        when(overlapCalculatorBuilder.whenUpdating(any(Instant.class), any(Range.class))).thenReturn(Arrays.asList(conflict1, conflict2, conflict3));
        when(customPropertySetService.calculateOverlapsFor(anyObject(), anyObject(), anyObject())).thenReturn(overlapCalculatorBuilder);
        return customPropertySet;
    }

    @Test
    public void testGetChannelCustomProperties() throws Exception {
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        when(customPropertySet.isVersioned()).thenReturn(false);

        String response = target("devices/1/channels/1/customproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.customproperties")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.customproperties[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.customproperties[0].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.customproperties[0].timesliced")).isEqualTo(false);
    }

    @Test
    public void testGetChannelCustomPropertiesVersioned() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/channels/1/customproperties/1/versions").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.versions")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.versions[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.versions[0].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.versions[0].timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.versions[0].versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[0].startTime")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[0].endTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Integer>get("$.versions[1].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.versions[1].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.versions[1].timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.versions[0].versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[1].startTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[1].endTime")).isEqualTo(endTimeSecond);
    }

    @Test
    public void testGetCurrentTimeInterval() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/channels/1/customproperties/1/currentinterval").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Long>get("$.start")).isGreaterThan(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.end")).isEqualTo(endTimeFirst);
    }

    @Test
    public void testGetConflictsCreate() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/channels/1/customproperties/1/conflicts").queryParam("startTime", startTimeNew).queryParam("endTime", endTimeNew).request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.conflicts")).hasSize(3);
        assertThat(jsonModel.<Integer>get("$.conflicts[0].customPropertySet.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.conflicts[0].customPropertySet.name")).isEqualTo("testCps");
        assertThat(jsonModel.<String>get("$.conflicts[0].conflictType")).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END.name());
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].customPropertySet.timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].conflictAtStart")).isEqualTo(false);
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].conflictAtEnd")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.startTime")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.endTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[1].customPropertySet.startTime")).isEqualTo(startTimeNew);
        assertThat(jsonModel.<Long>get("$.conflicts[1].customPropertySet.endTime")).isEqualTo(endTimeNew);
        assertThat(jsonModel.<Integer>get("$.conflicts[2].customPropertySet.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.conflicts[2].customPropertySet.name")).isEqualTo("testCps");
        assertThat(jsonModel.<String>get("$.conflicts[2].conflictType")).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_DELETE.name());
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].customPropertySet.timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.versionId")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.startTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.endTime")).isEqualTo(endTimeSecond);
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].conflictAtStart")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].conflictAtEnd")).isEqualTo(true);
    }

    @Test
    public void testEditChannelCustomAttribute() throws Exception {
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        when(customPropertySet.isVersioned()).thenReturn(false);
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = 1L;
        info.isActive = true;
        info.parent = 1L;
        info.version = 5L;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        info.timesliced = false;
        info.properties = new ArrayList<>();
        Response response = target("devices/1/channels/1/customproperties/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testEditChannelCustomAttributeVersioned() throws Exception {
        mockCustomPropertySet();
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = 1L;
        info.isActive = true;
        info.startTime = endTimeFirst;
        info.endTime = startTimeFirst;
        info.parent = 1L;
        info.version = 5L;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        info.timesliced = true;
        info.versionId = info.startTime;
        info.properties = new ArrayList<>();
        Response response = target("devices/1/channels/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        info.startTime = startTimeNew;
        info.endTime = endTimeFirst;
        info.versionId = info.startTime;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        response = target("devices/1/channels/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testChannelReadingEstimationInfoAboutApplication() {
        Instant readingStart = Instant.ofEpochMilli(1410827730000L);
        Instant readingEnd = Instant.ofEpochMilli(1410828630000L);

        Range<Instant> interval = Ranges.openClosed(readingStart, readingEnd);
        when(channel.getChannelData(interval)).thenReturn(Collections.singletonList(loadProfileReading));

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/" + readingEnd.toEpochMilli() + "/validation").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Number>get("$.bulkValidationInfo.estimatedByRule.id")).isEqualTo(((Long) estimationRule.getId()).intValue());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.estimatedByRule.name")).isEqualTo(estimationRule.getName());
        assertThat(jsonModel.<Number>get("$.bulkValidationInfo.estimatedByRule.ruleSetId")).isEqualTo(((Long) estimationRule.getRuleSet().getId()).intValue());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.estimatedByRule.application.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.estimatedByRule.application.name")).isEqualTo("MultiSense");
    }

    @Test
    public void testChannelReadingConfirmedInfoAboutApplication() {
        Instant readingStart = Instant.ofEpochMilli(1410827730000L);
        Instant readingEnd = Instant.ofEpochMilli(1410828630000L);

        Range<Instant> interval = Ranges.openClosed(readingStart, readingEnd);
        when(channel.getChannelData(interval)).thenReturn(Collections.singletonList(confirmedProfileReading));

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/" + readingEnd.toEpochMilli() + "/validation").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Boolean>get("$.mainValidationInfo.isConfirmed")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.bulkValidationInfo.isConfirmed")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.bulkValidationInfo.confirmedInApps[*].id")).contains(QualityCodeSystem.MDM.name(), QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<List<String>>get("$.bulkValidationInfo.confirmedInApps[*].name")).contains("Insight", "MultiSense");
    }

    @Test
    public void testChannelReadingAddedInfoAboutApplication() {
        Instant readingStart = Instant.ofEpochMilli(1410827730000L);
        Instant readingEnd = Instant.ofEpochMilli(1410828630000L);

        Range<Instant> interval = Ranges.openClosed(readingStart, readingEnd);
        when(channel.getChannelData(interval)).thenReturn(Collections.singletonList(addedLoadProfileReading));

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/" + readingEnd.toEpochMilli() + "/validation").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<String>get("$.mainValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.ADDED.name());
        assertThat(jsonModel.<String>get("$.mainValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.mainValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.ADDED.name());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
    }

    @Test
    public void testChannelReadingEditedInfoAboutApplication() {
        Instant readingStart = Instant.ofEpochMilli(1410827730000L);
        Instant readingEnd = Instant.ofEpochMilli(1410828630000L);

        Range<Instant> interval = Ranges.openClosed(readingStart, readingEnd);
        when(channel.getChannelData(interval)).thenReturn(Collections.singletonList(editedProfileReading));

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/" + readingEnd.toEpochMilli() + "/validation").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<String>get("$.bulkValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.EDITED.name());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.bulkValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
    }

    @Test
    public void testChannelReadingRemovedInfoAboutApplication() {
        Instant readingStart = Instant.ofEpochMilli(1410827730000L);
        Instant readingEnd = Instant.ofEpochMilli(1410828630000L);

        Range<Instant> interval = Ranges.openClosed(readingStart, readingEnd);
        when(channel.getChannelData(interval)).thenReturn(Collections.singletonList(removedProfileReading));

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/" + readingEnd.toEpochMilli() + "/validation").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<String>get("$.mainValidationInfo.valueModificationFlag")).isEqualTo(ReadingModificationFlag.REMOVED.name());
        assertThat(jsonModel.<String>get("$.mainValidationInfo.editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.mainValidationInfo.editedInApp.name")).isEqualTo("MultiSense");
    }
    @Test
    public void testSaveEstimatedData() {
        MeterActivation meterActivation = mock(MeterActivation.class);
        com.elster.jupiter.metering.Channel meteringChannel = mock(com.elster.jupiter.metering.Channel.class);
        ReadingType readingType = mock(ReadingType.class);
        List list = mock(List.class);
        when(channel.getReadingType()).thenReturn(readingType);
        ChannelDataUpdater channelDataUpdater = mock(ChannelDataUpdater.class);
        when(channelDataUpdater.editBulkChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channel.startEditingData()).thenReturn(channelDataUpdater);
        when(device.getId()).thenReturn(1L);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(meteringChannel));
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        doReturn(Collections.singletonList(readingType)).when(meteringChannel).getReadingTypes();
        when(list.contains(readingType)).thenReturn(true);

        EstimateChannelDataInfo estimateChannelDataInfo = new EstimateChannelDataInfo();
        estimateChannelDataInfo.estimatorImpl = "com.elster.jupiter.estimators.impl.ValueFillEstimator";
        estimateChannelDataInfo.estimateBulk = true;
        IntervalInfo intervalInfo = new IntervalInfo();

        intervalInfo.start = 1410804630000L;
        intervalInfo.end = 1410814630000L;
        estimateChannelDataInfo.intervals = new ArrayList<>();
        estimateChannelDataInfo.intervals.add(intervalInfo);
        estimateChannelDataInfo.properties = new ArrayList<>();
        estimateChannelDataInfo.properties.add(new PropertyInfo("valuefill.maxNumberOfConsecutiveSuspects", "Max number of consecutive suspects", new PropertyValueInfo<>(123L, null, 10L, true), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));
        estimateChannelDataInfo.properties.add(new PropertyInfo("valuefill.fillValue", "Fill value", new PropertyValueInfo<>(123L, null, 10L, true), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        Estimator estimator = mock(Estimator.class);
        EstimationResult estimationResult = mock(EstimationResult.class);
        when(estimationResult.remainingToBeEstimated()).thenReturn(new ArrayList<>());

        when(estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl)).thenReturn(Optional.of(estimator));
        when(estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl, new HashMap<>())).thenReturn(Optional.of(estimator));
        when(estimationService.previewEstimate(eq(QualityCodeSystem.MDC), any(ChannelsContainer.class), any(Range.class), any(ReadingType.class), any(Estimator.class))).thenReturn(estimationResult);

        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/data/issue/estimate").request().post(Entity.json(estimateChannelDataInfo));
        verify(channelDataUpdater).complete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
