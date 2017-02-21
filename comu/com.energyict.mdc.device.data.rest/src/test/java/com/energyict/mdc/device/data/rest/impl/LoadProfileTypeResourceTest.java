/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.impl.DataValidationStatusImpl;
import com.elster.jupiter.validation.impl.IValidationRule;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadProfileTypeResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String BATTERY_LOW = "Battery low";
    private static final String BAD_TIME = "Clock error";
    private static final Instant NOW = Instant.ofEpochMilli(1410786205000L);
    private static final Instant LAST_READING = Instant.ofEpochMilli(1410786196000L);
    private static final Date LAST_CHECKED = new Date(1409570229000L);
    private static final long CHANNEL_ID1 = 151521354L;
    private static final long CHANNEL_ID2 = 7487921005L;
    private static final long INTERVAL_START = 1410774630000L;
    private static final long INTERVAL_END = 1410828630000L;

    @Mock
    private Device device;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileReading loadProfileReading;
    @Mock
    private ChannelSpec channelSpec;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private IntervalReadingRecord readingRecord1, readingRecord2;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private IValidationRule rule1;
    @Mock
    private EstimationRule estimationRule;
    @Mock
    private ReadingQualityRecord quality1;
    @Mock
    private ReadingQualityRecord quality2;
    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private EstimationRuleSet estimationRuleSet;
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private LoadProfile.LoadProfileUpdater loadProfileUpdater;

    private ReadingQualityType readingQualityType = new ReadingQualityType("2.0.1");

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
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("1", 1L)).thenReturn(Optional.of(device));
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(device.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(loadProfile.getId()).thenReturn(1L);
        when(loadProfile.getVersion()).thenReturn(1L);
        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START), Instant.ofEpochMilli(INTERVAL_END));
        when(loadProfile.getChannelData(interval)).thenReturn(asList(loadProfileReading));
        Range<Instant> firstInterval = Ranges.openClosed(Instant.ofEpochMilli(INTERVAL_START - 900000), Instant.ofEpochMilli(INTERVAL_START));
        when(loadProfile.getChannelData(firstInterval)).thenReturn(Arrays.asList(loadProfileReading));
        when(loadProfileReading.getRange()).thenReturn(interval);

        when(channel1.getChannelData(firstInterval)).thenReturn(Arrays.asList(loadProfileReading));
        when(channel2.getChannelData(firstInterval)).thenReturn(Arrays.asList(loadProfileReading));

        ReadingQualityRecord readingQualityBatteryLow = mockReadingQuality(ProtocolReadingQualities.BATTERY_LOW.getCimCode());
        ReadingQualityRecord readingQualityBadTime = mockReadingQuality(ProtocolReadingQualities.BADTIME.getCimCode());
        List<ReadingQualityRecord> readingQualities = Arrays.asList(readingQualityBatteryLow, readingQualityBadTime);
        Map<Channel, List<ReadingQualityRecord>> readingQualitiesPerChannel = new HashMap<>();
        readingQualitiesPerChannel.put(channel1, readingQualities);
        readingQualitiesPerChannel.put(channel2, readingQualities);

        doReturn(readingQualitiesPerChannel).when(loadProfileReading).getReadingQualities();
        doReturn(readingQualities).when(readingRecord1).getReadingQualities();
        doReturn(readingQualities).when(readingRecord2).getReadingQualities();

        doReturn(BATTERY_LOW).when(thesaurus).getString(BATTERY_LOW, BATTERY_LOW);

        ReadingType readingType = mockReadingType("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18");
        ReadingType calculatedReadingType = mockReadingType("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.of(calculatedReadingType));

        when(loadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel1, readingRecord1, channel2, readingRecord2));
        when(clock.instant()).thenReturn(NOW);

        when(readingRecord1.getValue()).thenReturn(BigDecimal.valueOf(200001, 0));
        Quantity quantity = Quantity.create(BigDecimal.valueOf(200, 0), "Wh");
        when(readingRecord1.getQuantity(calculatedReadingType)).thenReturn(quantity);

        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(250001, 0));
        quantity = Quantity.create(BigDecimal.valueOf(250, 0), "Wh");
        when(readingRecord2.getQuantity(calculatedReadingType)).thenReturn(quantity);

        when(channel1.getDevice()).thenReturn(device);
        when(channel1.getReadingType()).thenReturn(readingType);
        when(channel1.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));
        when(channel1.getId()).thenReturn(CHANNEL_ID1);
        when(channel1.getChannelSpec()).thenReturn(channelSpec);
        when(channel1.getInterval()).thenReturn(new TimeDuration(900));
        when(channel1.getNrOfFractionDigits()).thenReturn(3);
        when(channel2.getDevice()).thenReturn(device);
        when(channel2.getReadingType()).thenReturn(readingType);
        when(channel2.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));
        when(channel2.getId()).thenReturn(CHANNEL_ID2);
        when(channel2.getChannelSpec()).thenReturn(channelSpec);
        when(channel2.getInterval()).thenReturn(new TimeDuration(900));
        when(channel2.getNrOfFractionDigits()).thenReturn(3);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(device.getZone()).thenReturn(ZoneId.systemDefault());
        when(deviceValidation.isValidationActive(channel1, NOW)).thenReturn(true);
        when(deviceValidation.isValidationActive(channel2, NOW)).thenReturn(true);

        DataValidationStatusImpl state1 = new DataValidationStatusImpl(Instant.ofEpochMilli(INTERVAL_END), true);
        state1.addReadingQuality(quality1, asList(rule1));
        when(quality1.getType()).thenReturn(readingQualityType);
        when(rule1.getRuleSet()).thenReturn(ruleSet);
        when(ruleSet.getName()).thenReturn("ruleSetName");
        doReturn(Arrays.asList(rule1)).when(ruleSet).getRules();
        when(rule1.isActive()).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(rule1.getImplementation()).thenReturn("isPrime");
        when(rule1.getDisplayName()).thenReturn("Primes only");
        ValidationRuleSetVersion validationRuleSetVersion = mock(ValidationRuleSetVersion.class);
        when(validationRuleSetVersion.getRuleSet()).thenReturn(mock(ValidationRuleSet.class));
        when(rule1.getRuleSetVersion()).thenReturn(validationRuleSetVersion);
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(3);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);

        DataValidationStatusImpl state2 = new DataValidationStatusImpl(Instant.ofEpochMilli(INTERVAL_END), true);
        state2.addReadingQuality(quality2, Collections.emptyList());
        when(quality2.hasEstimatedCategory()).thenReturn(true);
        when(estimationRule.getId()).thenReturn(13L);
        when(estimationRule.getRuleSet()).thenReturn(estimationRuleSet);
        when(estimationRuleSet.getId()).thenReturn(15L);
        when(estimationRule.getName()).thenReturn("EstimationRule");
        ReadingQualityType readingQualityType = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, (int) estimationRule.getId());
        when(quality2.getType()).thenReturn(readingQualityType);
        doReturn(Optional.of(estimationRule)).when(estimationService).findEstimationRuleByQualityType(readingQualityType);

        when(loadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel1, state1, channel2, state2));
        when(loadProfileService.findById(loadProfile.getId())).thenReturn(Optional.of(loadProfile));
        when(loadProfileService.findAndLockLoadProfileByIdAndVersion(loadProfile.getId(), loadProfile.getVersion())).thenReturn(Optional.of(loadProfile));
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        return readingQuality;
    }

    @Test
    public void test1IntervalOfLPData() throws UnsupportedEncodingException {

        when(topologyService.getDataLoggerChannelTimeLine(any(Channel.class), any(Range.class))).thenAnswer(invocationOnMock -> Collections.singletonList(Pair.of(((Channel) invocationOnMock.getArguments()[0]), ((Range<Instant>) invocationOnMock
                .getArguments()[1]))));
        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data/1410774630000/validation")
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.readingQualities")).hasSize(2);

        assertThat(jsonModel.<String>get("$.readingQualities[0].cimCode")).isEqualTo("1.1.1");
        assertThat(jsonModel.<String>get("$.readingQualities[0].indexName")).isEqualTo("Battery low");
        assertThat(jsonModel.<String>get("$.readingQualities[0].systemName")).isEqualTo("Device");
        assertThat(jsonModel.<String>get("$.readingQualities[0].categoryName")).isEqualTo("Diagnostics");

        assertThat(jsonModel.<String>get("$.readingQualities[1].cimCode")).isEqualTo("1.1.9");
        assertThat(jsonModel.<String>get("$.readingQualities[1].indexName")).isEqualTo("Clock error");
        assertThat(jsonModel.<String>get("$.readingQualities[1].systemName")).isEqualTo("Device");
        assertThat(jsonModel.<String>get("$.readingQualities[1].categoryName")).isEqualTo("Diagnostics");


        String json2 = target("devices/1/channels/" + CHANNEL_ID2 + "/data/1410774630000/validation")
                .request().get(String.class);

        JsonModel jsonModel2 = JsonModel.create(json2);

        assertThat(jsonModel2.<List<?>>get("$.readingQualities")).hasSize(2);

        assertThat(jsonModel2.<String>get("$.readingQualities[0].cimCode")).isEqualTo("1.1.1");
        assertThat(jsonModel2.<String>get("$.readingQualities[0].indexName")).isEqualTo("Battery low");
        assertThat(jsonModel2.<String>get("$.readingQualities[0].systemName")).isEqualTo("Device");
        assertThat(jsonModel2.<String>get("$.readingQualities[0].categoryName")).isEqualTo("Diagnostics");

        assertThat(jsonModel2.<String>get("$.readingQualities[1].cimCode")).isEqualTo("1.1.9");
        assertThat(jsonModel2.<String>get("$.readingQualities[1].indexName")).isEqualTo("Clock error");
        assertThat(jsonModel2.<String>get("$.readingQualities[1].systemName")).isEqualTo("Device");
        assertThat(jsonModel2.<String>get("$.readingQualities[1].categoryName")).isEqualTo("Diagnostics");
    }

    @Test
    public void testLoadProfileData() {
        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
        assertThat(jsonModel.<Long>get("$.data[0].interval.start")).isEqualTo(1410774630000L);
        assertThat(jsonModel.<Long>get("$.data[0].interval.end")).isEqualTo(1410828630000L);
        Map<String, List<String>> map = jsonModel.<Map<String, List<String>>>get("$.data[0].readingQualities");

        List<String> readingQualitiesChannel1 = map.get(String.valueOf(CHANNEL_ID1));
        assertThat(readingQualitiesChannel1).hasSize(2);
        assertThat(readingQualitiesChannel1.get(0)).isEqualTo(BATTERY_LOW);
        assertThat(readingQualitiesChannel1.get(1)).isEqualTo(BAD_TIME);

        List<String> readingQualitiesChannel2 = map.get(String.valueOf(CHANNEL_ID2));
        assertThat(readingQualitiesChannel2).hasSize(2);
        assertThat(readingQualitiesChannel2.get(0)).isEqualTo(BATTERY_LOW);
        assertThat(readingQualitiesChannel2.get(1)).isEqualTo(BAD_TIME);

        Map collectedValues = jsonModel.<Map>get("$.data[0].channelData");
        assertThat(collectedValues).contains(entry(String.valueOf(CHANNEL_ID1), "200.000"));
        assertThat(collectedValues).contains(entry(String.valueOf(CHANNEL_ID2), "250.000"));
        Map deltaValues = jsonModel.<Map>get("$.data[0].channelCollectedData");
        assertThat(deltaValues).contains(entry(String.valueOf(CHANNEL_ID1), "200001.000"));
        assertThat(deltaValues).contains(entry(String.valueOf(CHANNEL_ID2), "250001.000"));
        Map validations = jsonModel.<Map>get("$.data[0].channelValidationData");
        assertThat(validations).hasSize(2).containsKeys(String.valueOf(CHANNEL_ID1), String.valueOf(CHANNEL_ID2));
        assertThat(jsonModel.<Boolean>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".mainValidationInfo.validationResult")).isEqualTo("validationStatus.suspect");


        assertThat(jsonModel.<Boolean>get("$.data[0].channelValidationData." + CHANNEL_ID2 + ".dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID2 + ".mainValidationInfo.validationResult")).isEqualTo("validationStatus.suspect");
    }

    @Test
    public void testLoadProfileDataFiltered() {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.VALID);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000},{\"property\":\"suspect\",\"value\":\"suspect\"}]");
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testLoadProfileDataFilteredMatches() {
        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("filter", filter)
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
    }

    @Test
    public void testValidate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING));

        LoadProfileTriggerValidationInfo entity = new LoadProfileTriggerValidationInfo();
        entity.id = 1L;
        entity.version = 1L;
        entity.parent = new VersionInfo<>("1", 1L);
        Response response = target("devices/1/loadprofiles/1/validate")
                .request()
                .put(Entity.json(entity));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).validateLoadProfile(loadProfile);
    }

    @Test
    public void testValidateWithDate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING));

        LoadProfileTriggerValidationInfo triggerValidationInfo = new LoadProfileTriggerValidationInfo();
        triggerValidationInfo.lastChecked = LAST_CHECKED.getTime();
        triggerValidationInfo.id = 1L;
        triggerValidationInfo.version = 1L;
        triggerValidationInfo.parent = new VersionInfo<>("1", 1L);
        Response response = target("devices/1/loadprofiles/1/validate")
                .request()
                .put(Entity.json(triggerValidationInfo));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).validateLoadProfile(loadProfile);
    }

}