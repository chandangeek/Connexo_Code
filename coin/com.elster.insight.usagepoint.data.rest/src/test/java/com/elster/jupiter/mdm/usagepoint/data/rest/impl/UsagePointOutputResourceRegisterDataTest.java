package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceRegisterDataTest extends UsagePointDataRestApplicationJerseyTest {
    private static final String USAGE_POINT_NAME = "Obychnoe imya";
    private static final Instant readingTimeStamp1 = Instant.ofEpochMilli(1410774620100L);
    private static final Instant readingTimeStamp2 = readingTimeStamp1.plus(5, ChronoUnit.MINUTES);
    private static final Instant readingTimeStamp3 = readingTimeStamp2.plus(10, ChronoUnit.MINUTES);

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingRecord readingRecord1, readingRecord2, readingRecord3;
    @Mock
    ValidationEvaluator evaluator;

    @Captor
    private ArgumentCaptor<List<ReadingImpl>> readingsCaptor;

    @Before
    public void before() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(meteringService.findUsagePointByName(any())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfigurationWithContract(1, "mc");
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer));

        when(readingRecord1.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord1.getTimeStamp()).thenReturn(readingTimeStamp1);
        when(readingRecord1.getReportedDateTime()).thenReturn(readingTimeStamp1);
        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(206, 0));
        when(readingRecord2.getTimeStamp()).thenReturn(readingTimeStamp2);
        when(readingRecord2.getReportedDateTime()).thenReturn(readingTimeStamp2);
        when(readingRecord3.getValue()).thenReturn(BigDecimal.valueOf(250, 0));
        when(readingRecord3.getTimeStamp()).thenReturn(readingTimeStamp3);
        when(readingRecord3.getReportedDateTime()).thenReturn(readingTimeStamp3);

        evaluator = mock(ValidationEvaluator.class);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationStatus(eq(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC)),
                any(Channel.class), any(), eq(Range.openClosed(readingTimeStamp1, readingTimeStamp3))))
                .thenReturn(Collections.emptyList());

        when(validationService.getLastChecked(any(Channel.class))).thenReturn(Optional.of(readingTimeStamp3));
    }

    private String buildFilter() throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", readingTimeStamp1.toEpochMilli())
                .property("intervalEnd", readingTimeStamp3.toEpochMilli())
                .create();
    }

    @Test
    public void testGetRegisterDataNoSuchUsagePoint() throws Exception {
        // Business method
        Response response = target("/usagepoints/xxx/purposes/1/outputs/2/registerData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataNoMetrologyConfigurationOnUsagePoint() throws Exception {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/1/outputs/2/registerData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataNoSuchContract() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/90030004443343/outputs/2/registerData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataMissingIntervalStart() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalEnd", readingTimeStamp3.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.registerData")).isEmpty();
    }

    @Test
    public void testGetRegisterDataMissingIntervalEnd() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalStart", readingTimeStamp1.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.registerData")).isEmpty();
    }

    @Test
    public void testGetRegisterDataOnRegularReadingTypeDeliverable() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/1/outputs/1/registerData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataRequestedIntervalDoesNotContainData() throws Exception {
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(readingTimeStamp1));
        String filter = ExtjsFilter.filter()
                .property("intervalStart", readingTimeStamp1.minus(100, ChronoUnit.MINUTES).toEpochMilli())
                .property("intervalEnd", readingTimeStamp1.minus(50, ChronoUnit.MINUTES).toEpochMilli())
                .create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.channelData")).isEmpty();
    }

    @Test
    public void testGetRegisterOutputData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(readingTimeStamp1));
        when(channel.getRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3)))
                .thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getCalculatedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getPersistedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Collections.emptyList());
        when(channel.toList(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingTimeStamp1, readingTimeStamp2, readingTimeStamp3));
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", buildFilter()).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$registerData[*].timeStamp")).containsExactly(
                readingTimeStamp3.toEpochMilli(), readingTimeStamp2.toEpochMilli(), readingTimeStamp1.toEpochMilli());
        assertThat(jsonModel.<List<Number>>get("$registerData[*].reportedDateTime")).containsExactly(
                readingTimeStamp3.toEpochMilli(), readingTimeStamp2.toEpochMilli(), readingTimeStamp1.toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$registerData[*].value")).containsExactly("250", "206", "200");
    }

    @Test
    public void testGetRegisterOutputDataValidated() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        mockReadingsWithValidationResult(channel);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(readingTimeStamp1));
        when(channel.getCalculatedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getPersistedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Collections.emptyList());
        when(channel.toList(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingTimeStamp1, readingTimeStamp2, readingTimeStamp3));
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));

        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData").queryParam("filter", buildFilter())
                .request()
                .get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$registerData[*].timeStamp")).containsExactly(
                readingTimeStamp3.toEpochMilli(), readingTimeStamp2.toEpochMilli(), readingTimeStamp1.toEpochMilli());
        assertThat(jsonModel.<List<Number>>get("$registerData[*].reportedDateTime")).containsExactly(
                readingTimeStamp3.toEpochMilli(), readingTimeStamp2.toEpochMilli(), readingTimeStamp1.toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$registerData[*].value")).containsExactly("250", "206", "200");
        assertThat(jsonModel.<String>get("$registerData[0].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Boolean>get("$registerData[0].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$registerData[0].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<Integer>get("$registerData[0].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$registerData[0].validationRules[0].name")).isEqualTo("MinMax");
        assertThat(jsonModel.<String>get("$registerData[1].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Boolean>get("$registerData[1].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$registerData[1].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<Integer>get("$registerData[1].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$registerData[1].validationRules[0].name")).isEqualTo("MinMax");
        assertThat(jsonModel.<String>get("$registerData[2].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Boolean>get("$registerData[2].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$registerData[2].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<Integer>get("$registerData[2].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$registerData[2].validationRules[0].name")).isEqualTo("MinMax");
    }

    @Test
    public void testEditRegisterData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(readingTimeStamp1));
        when(channel.getRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3)))
                .thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getCalculatedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getPersistedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Collections.emptyList());
        when(channel.toList(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingTimeStamp1, readingTimeStamp2, readingTimeStamp3));
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));


        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.value = BigDecimal.valueOf(101L);
        info.timeStamp = readingTimeStamp3;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/registerData/"+readingTimeStamp3.toEpochMilli())
                .request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel).editReadings(eq(QualityCodeSystem.MDM), readingsCaptor.capture());
        assertThat(readingsCaptor.getValue()).hasSize(1);
        assertThat(readingsCaptor.getValue().get(0).getValue()).isEqualTo(info.value);
        assertThat(readingsCaptor.getValue().get(0).getTimeStamp()).isEqualTo(readingTimeStamp3);
    }

    @Test
    public void testConfirmRegisterData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(readingTimeStamp1));
        when(channel.getRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3)))
                .thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getCalculatedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getPersistedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Collections.emptyList());
        when(channel.toList(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingTimeStamp1, readingTimeStamp2, readingTimeStamp3));
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));

        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.isConfirmed = true;
        info.timeStamp = readingTimeStamp3;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/registerData/"+readingTimeStamp3.toEpochMilli())
                .request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel).confirmReadings(eq(QualityCodeSystem.MDM), readingsCaptor.capture());
        assertThat(readingsCaptor.getValue()).hasSize(1);
        assertThat(readingsCaptor.getValue().get(0).getValue()).isEqualTo(info.value);
        assertThat(readingsCaptor.getValue().get(0).getTimeStamp()).isEqualTo(readingTimeStamp3);
    }

    @Test
    public void testRemoveRegisterData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(readingTimeStamp1));
        when(channel.getRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3)))
                .thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getCalculatedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));
        when(channel.getPersistedRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Collections.emptyList());
        when(channel.toList(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingTimeStamp1, readingTimeStamp2, readingTimeStamp3));
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(channel.getReading(readingTimeStamp3)).thenReturn(Optional.of(readingRecord3));
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));

        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.value = BigDecimal.valueOf(101L);
        info.timeStamp = readingTimeStamp3;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/registerData/"+readingTimeStamp3.toEpochMilli())
                .request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        List<BaseReadingRecord> record = Collections.singletonList(channel.getReading(readingTimeStamp3).get());
        verify(channel).removeReadings(eq(QualityCodeSystem.MDM),eq(record));
        verify(validationService).updateLastChecked(eq(channel), eq(readingTimeStamp3.minusSeconds(1L)));
    }

    private void mockReadingsWithValidationResult(Channel channel) {
        ValidationRule minMax = mockValidationRule(1, "MinMax");

        DataValidationStatus dataValidationStatus_1 = mockValidationStatus(readingTimeStamp1, minMax);
        DataValidationStatus dataValidationStatus_2 = mockValidationStatus(readingTimeStamp2, minMax);
        DataValidationStatus dataValidationStatus_3 = mockValidationStatus(readingTimeStamp3, minMax);

        List<ReadingRecord> readings = Arrays.asList(readingRecord1, readingRecord2, readingRecord3);
        when(channel.getRegisterReadings(any())).thenReturn(readings);

        when(evaluator.getValidationStatus(eq(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC)),
                eq(channel), any(), eq(Range.openClosed(readingTimeStamp1, readingTimeStamp3))))
                .thenReturn(Arrays.asList(dataValidationStatus_1, dataValidationStatus_2, dataValidationStatus_3));
    }

    private ValidationRule mockValidationRule(long id, String name) {
        ValidationRule validationRule = mock(ValidationRule.class);
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(validationRule.getId()).thenReturn(id);
        when(validationRule.getName()).thenReturn(name);
        when(validationRule.getDisplayName()).thenReturn(name);
        when(validationRule.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(ruleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        return validationRule;
    }

    private DataValidationStatus mockValidationStatus(Instant timeStamp, ValidationRule validationRule) {
        DataValidationStatus validationStatus = mock(DataValidationStatus.class);

        when(validationStatus.getReadingTimestamp()).thenReturn(timeStamp);
        when(validationStatus.completelyValidated()).thenReturn(true);
        ReadingQualityType qualityType = new ReadingQualityType("3.5.258");
        ReadingQualityRecord quality = mock(ReadingQualityRecord.class);
        when(quality.getType()).thenReturn(qualityType);
        doReturn(Collections.singletonList(quality)).when(validationStatus).getReadingQualities();
        when(validationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        when(validationStatus.getOffendedRules()).thenReturn(Collections.singletonList(validationRule));
        return validationStatus;
    }
}
