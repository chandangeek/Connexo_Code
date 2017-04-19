/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import junit.framework.AssertionFailedError;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
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
    private static final Instant READING_TIME_STAMP_1 = Instant.ofEpochMilli(1410774620100L);
    private static final Instant READING_TIME_STAMP_2 = READING_TIME_STAMP_1.plus(5, ChronoUnit.MINUTES);
    private static final Instant READING_TIME_STAMP_3 = READING_TIME_STAMP_2.plus(10, ChronoUnit.MINUTES);
    private static final Instant TIME_STAMP_BEFORE_1 = READING_TIME_STAMP_1.minus(60, ChronoUnit.MINUTES);

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC1, effectiveMC2, effectiveMC3, effectiveMC4;
    @Mock
    private ChannelsContainer channelsContainer1, channelsContainer2;
    @Mock
    private AggregatedChannel channel1, channel2;
    @Mock
    private ReadingRecord readingRecord1, readingRecord2, readingRecord3;
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private ReadingType readingType1;
    @Mock
    private ReadingType readingType2;
    @Mock
    private ReadingType readingType3;

    @Captor
    private ArgumentCaptor<List<ReadingImpl>> readingsCaptor;

    @Before
    public void before() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(meteringService.findUsagePointByName(any())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC1));
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(effectiveMC1, effectiveMC2, effectiveMC3, effectiveMC4));
        MetrologyPurpose billing = mockMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose information = mockMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        UsagePointMetrologyConfiguration metrologyConfiguration1 = mockMetrologyConfigurationWithContract(1, "mc1", billing, information);
        when(effectiveMC1.getMetrologyConfiguration()).thenReturn(metrologyConfiguration1);
        when(effectiveMC1.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC1.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer1));
        when(effectiveMC1.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel1));
        Range<Instant> range1 = Range.atLeast(READING_TIME_STAMP_2);
        when(channelsContainer1.getRange()).thenReturn(range1);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        when(channelsContainer1.getChannel(any())).thenReturn(Optional.of(channel1));
        when(channel1.toList(any())).thenThrow(new AssertionFailedError("toList() should not be called on register"));
        UsagePointMetrologyConfiguration metrologyConfiguration2 = mockMetrologyConfigurationWithContract(2, "mc2", billing, information);
        when(effectiveMC2.getMetrologyConfiguration()).thenReturn(metrologyConfiguration2);
        when(effectiveMC2.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC2.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer2));
        when(effectiveMC2.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel2));
        Range<Instant> range2 = Range.closedOpen(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_2);
        when(channelsContainer2.getRange()).thenReturn(range2);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        when(channelsContainer2.getChannel(any())).thenReturn(Optional.of(channel2));
        when(channel2.toList(any())).thenThrow(new AssertionFailedError("toList() should not be called on register"));
        UsagePointMetrologyConfiguration metrologyConfiguration3 = mockMetrologyConfigurationWithContract(3, "mc3");
        when(effectiveMC3.getMetrologyConfiguration()).thenReturn(metrologyConfiguration3);
        when(effectiveMC3.getUsagePoint()).thenReturn(usagePoint);
        UsagePointMetrologyConfiguration metrologyConfiguration4 = mockMetrologyConfigurationWithContract(4, "mc4", billing, information);
        metrologyConfiguration4.getContracts().stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(billing))
                .forEach(contract -> {
                    when(contract.getDeliverables()).thenReturn(Collections.emptyList());
                    ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
                    when(effectiveMC4.getChannelsContainer(contract)).thenReturn(Optional.of(channelsContainer));
                    when(channelsContainer.getInterval()).thenReturn(Interval.of(
                            Range.closedOpen(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_1)));
                    when(channelsContainer.getChannel(any(ReadingType.class))).thenReturn(Optional.empty());
                    when(channelsContainer.getChannels()).thenReturn(Collections.emptyList());
                    when(effectiveMC4.getAggregatedChannel(eq(contract), any(ReadingType.class))).thenReturn(Optional.empty());
                });
        when(effectiveMC4.getMetrologyConfiguration()).thenReturn(metrologyConfiguration4);
        when(effectiveMC4.getUsagePoint()).thenReturn(usagePoint);
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenAnswer(invocation -> {
            Instant when = (Instant) invocation.getArguments()[0];
            if (range1.contains(when)) {
                return Optional.of(effectiveMC1);
            }
            if (range2.contains(when)) {
                return Optional.of(effectiveMC2);
            }
            return Optional.empty();
        });

        when(readingRecord1.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord1.getTimeStamp()).thenReturn(READING_TIME_STAMP_1);
        when(readingRecord1.getReportedDateTime()).thenReturn(READING_TIME_STAMP_1);
        when(readingRecord1.getTimePeriod()).thenReturn(Optional.empty());
        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(206, 0));
        when(readingRecord2.getTimeStamp()).thenReturn(READING_TIME_STAMP_2);
        when(readingRecord2.getTimePeriod()).thenReturn(Optional.empty());
        when(readingRecord2.getReportedDateTime()).thenReturn(READING_TIME_STAMP_2);
        when(readingRecord3.getValue()).thenReturn(BigDecimal.valueOf(250, 0));
        when(readingRecord3.getTimeStamp()).thenReturn(READING_TIME_STAMP_3);
        when(readingRecord3.getReportedDateTime()).thenReturn(READING_TIME_STAMP_3);
        when(readingRecord1.getReadingType()).thenReturn(readingType1);
        when(readingRecord2.getReadingType()).thenReturn(readingType2);
        when(readingRecord3.getReadingType()).thenReturn(readingType3);
        when(readingType1.getMacroPeriod()).thenReturn(MacroPeriod.BILLINGPERIOD);
        when(readingType2.getMacroPeriod()).thenReturn(MacroPeriod.BILLINGPERIOD);
        when(readingType3.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);

        evaluator = mock(ValidationEvaluator.class);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationStatus(eq(EnumSet.of(QualityCodeSystem.MDM)), any(Channel.class), any(),
                eq(Range.openClosed(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_3))))
                .thenReturn(Collections.emptyList());
    }

    private String defaultFilter() throws UnsupportedEncodingException {
        return this.buildFilter(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_3);
    }

    private String buildFilter(Instant start, Instant end) throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", start.toEpochMilli())
                .property("intervalEnd", end.toEpochMilli())
                .create();
    }

    @Test
    public void testGetRegisterDataNoSuchUsagePoint() throws Exception {
        // Business method
        Response response = target("/usagepoints/xxx/purposes/1/outputs/2/registerData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataNoMetrologyConfigurationOnUsagePoint() throws Exception {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/1/outputs/2/registerData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataNoSuchContract() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/90030004443343/outputs/2/registerData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataMissingIntervalStart() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalEnd", READING_TIME_STAMP_3.toEpochMilli()).create();

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
        String filter = ExtjsFilter.filter().property("intervalStart", TIME_STAMP_BEFORE_1.toEpochMilli()).create();

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
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/registerData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataRequestedIntervalDoesNotContainData() throws Exception {
        String filter = ExtjsFilter.filter()
                .property("intervalStart", TIME_STAMP_BEFORE_1.toEpochMilli())
                .property("intervalEnd", READING_TIME_STAMP_1.minus(50, ChronoUnit.MINUTES).toEpochMilli())
                .create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.registerData")).isEmpty();
    }

    @Test
    public void testGetRegisterOutputDataValidated() throws Exception {
        mockReadingsWithValidationResult();

        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", defaultFilter()).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$registerData[*].timeStamp")).containsExactly(
                READING_TIME_STAMP_3.toEpochMilli(), READING_TIME_STAMP_2.toEpochMilli(), READING_TIME_STAMP_1.toEpochMilli());
        assertThat(jsonModel.<List<Number>>get("$registerData[*].reportedDateTime")).containsExactly(
                READING_TIME_STAMP_3.toEpochMilli(), READING_TIME_STAMP_2.toEpochMilli(), READING_TIME_STAMP_1.toEpochMilli());
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
        assertThat(jsonModel.<Boolean>get("$registerData[2].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$registerData[2].validationResult")).isEqualTo("validationStatus.ok");
        assertThat(jsonModel.<Integer>get("$registerData[2].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$registerData[2].validationRules[0].name")).isEqualTo("MinMax");
    }

    @Test
    public void testGetRegisterSuspectOutputData() throws Exception {
        mockReadingsWithValidationResult();
        String filter = ExtjsFilter.filter()
                .property("intervalStart", TIME_STAMP_BEFORE_1.toEpochMilli())
                .property("intervalEnd", READING_TIME_STAMP_3.toEpochMilli())
                .property("suspect", Collections.singletonList("suspect"))
                .create();
        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Number>>get("$registerData[*].timeStamp")).containsExactly(
                READING_TIME_STAMP_3.toEpochMilli(), READING_TIME_STAMP_2.toEpochMilli());
        assertThat(jsonModel.<List<Number>>get("$registerData[*].reportedDateTime")).containsExactly(
                READING_TIME_STAMP_3.toEpochMilli(), READING_TIME_STAMP_2.toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$registerData[*].value")).containsExactly("250", "206");
        assertThat(jsonModel.<List<String>>get("$registerData[*].validationResult")).containsExactly("validationStatus.suspect", "validationStatus.suspect");
    }

    @Test
    public void testEditRegisterData() throws Exception {
        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.value = BigDecimal.valueOf(101L);
        info.timeStamp = READING_TIME_STAMP_2;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData/" + READING_TIME_STAMP_2.toEpochMilli())
                .request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel2).editReadings(eq(QualityCodeSystem.MDM), readingsCaptor.capture());
        assertThat(readingsCaptor.getValue()).hasSize(1);
        assertThat(readingsCaptor.getValue().get(0).getValue()).isEqualTo(info.value);
        assertThat(readingsCaptor.getValue().get(0).getTimeStamp()).isEqualTo(READING_TIME_STAMP_2);
    }

    @Test
    public void testEditRegisterDataWithoutEffectiveMC() throws Exception {
        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.value = BigDecimal.valueOf(101L);
        info.timeStamp = TIME_STAMP_BEFORE_1;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData/" + TIME_STAMP_BEFORE_1.toEpochMilli())
                .request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<List>get("$.errors")).hasSize(1);
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("timeStamp");
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT_AT_THE_MOMENT.getDefaultFormat());
    }

    @Test
    public void testConfirmRegisterData() throws Exception {
        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.isConfirmed = true;
        info.timeStamp = READING_TIME_STAMP_1;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData/" + READING_TIME_STAMP_1.toEpochMilli())
                .request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel2).confirmReadings(eq(QualityCodeSystem.MDM), readingsCaptor.capture());
        assertThat(readingsCaptor.getValue()).hasSize(1);
        assertThat(readingsCaptor.getValue().get(0).getValue()).isEqualTo(info.value);
        assertThat(readingsCaptor.getValue().get(0).getTimeStamp()).isEqualTo(READING_TIME_STAMP_1);
    }

    @Test
    public void testConfirmRegisterDataWithoutEffectiveMC() throws Exception {
        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.isConfirmed = true;
        info.timeStamp = TIME_STAMP_BEFORE_1;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData/" + TIME_STAMP_BEFORE_1.toEpochMilli())
                .request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<List>get("$.errors")).hasSize(1);
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("timeStamp");
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT_AT_THE_MOMENT.getDefaultFormat());
    }

    @Test
    public void testRemoveRegisterData() throws Exception {
        when(channel2.getReading(READING_TIME_STAMP_2)).thenReturn(Optional.of(readingRecord1));

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData/" + READING_TIME_STAMP_2.toEpochMilli())
                .request().delete();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel2).removeReadings(QualityCodeSystem.MDM, Collections.singletonList(readingRecord1));
    }

    @Test
    public void testGetRegisterOutputDataFromCalculatedReading() throws Exception {
        Range<Instant> range = Range.openClosed(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_1);
        when(channel2.getCalculatedRegisterReadings(range)).thenReturn(Collections.singletonList(readingRecord1));
        when(channel2.getPersistedRegisterReadings(range)).thenReturn(Collections.emptyList());
        doReturn(Arrays.asList(
                mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT)),
                mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC))
        )).when(readingRecord1).getReadingQualities();

        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", buildFilter(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_1)).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.registerData")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.registerData[0].timeStamp")).isEqualTo(READING_TIME_STAMP_1.toEpochMilli());
        assertThat(jsonModel.<String>get("$.registerData[0].value")).isEqualTo("200");
        assertThat(jsonModel.<List<?>>get("$.registerData[0].readingQualities")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.registerData[0].readingQualities[*].cimCode")).containsOnly("2.5.258", "2.7.0");
    }

    @Test
    public void testGetRegisterOutputDataFromPersistedReading() throws Exception {
        Range<Instant> interval = Range.openClosed(READING_TIME_STAMP_1, READING_TIME_STAMP_2);
        when(channel2.getPersistedRegisterReadings(interval)).thenReturn(Collections.singletonList(readingRecord2));
        when(channel2.getCalculatedRegisterReadings(interval)).thenReturn(Collections.emptyList());
        DataValidationStatus dataValidationStatus = mockSuspectValidationStatus(READING_TIME_STAMP_2, mockValidationRule(1, "MinMax"));
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel2, Collections.singletonList(readingRecord2), interval))
                .thenReturn(Collections.singletonList(dataValidationStatus));

        doReturn(Arrays.asList(
                mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC)),
                mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)),
                mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.VALIDATED)) // 3.0.1 should be filtered out
        )).when(dataValidationStatus).getReadingQualities();

        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/registerData")
                .queryParam("filter", buildFilter(READING_TIME_STAMP_1, READING_TIME_STAMP_2)).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.registerData")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.registerData[0].timeStamp")).isEqualTo(READING_TIME_STAMP_2.toEpochMilli());
        assertThat(jsonModel.<String>get("$.registerData[0].value")).isEqualTo("206");
        assertThat(jsonModel.<List<?>>get("$.registerData[0].readingQualities")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.registerData[0].readingQualities[*].cimCode")).containsOnly("3.5.258", "3.7.0");
    }

    private ReadingQuality mockReadingQuality(ReadingQualityType type) {
        ReadingQuality readingQuality = mock(ReadingQuality.class);
        when(readingQuality.getType()).thenReturn(type);
        return readingQuality;
    }

    private void mockReadingsWithValidationResult() {
        Range<Instant> range1 = Range.openClosed(READING_TIME_STAMP_2, READING_TIME_STAMP_3);
        when(channel1.getRegisterReadings(range1)).thenReturn(Collections.singletonList(readingRecord3));
        when(channel1.getCalculatedRegisterReadings(range1)).thenReturn(Collections.singletonList(readingRecord3));
        when(channel1.getPersistedRegisterReadings(range1)).thenReturn(Collections.emptyList());
        Range<Instant> range2 = Range.openClosed(TIME_STAMP_BEFORE_1, READING_TIME_STAMP_2);
        when(channel2.getRegisterReadings(range2)).thenReturn(Arrays.asList(readingRecord1, readingRecord2));
        when(channel2.getCalculatedRegisterReadings(range2)).thenReturn(Arrays.asList(readingRecord1, readingRecord2));
        when(channel2.getPersistedRegisterReadings(range2)).thenReturn(Collections.emptyList());

        ValidationRule minMax = mockValidationRule(1, "MinMax");
        DataValidationStatus dataValidationStatus_1 = mockValidationStatus(READING_TIME_STAMP_1, minMax);
        DataValidationStatus dataValidationStatus_2 = mockSuspectValidationStatus(READING_TIME_STAMP_2, minMax);
        DataValidationStatus dataValidationStatus_3 = mockSuspectValidationStatus(READING_TIME_STAMP_3, minMax);

        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel1, Collections.singletonList(readingRecord3), range1))
                .thenReturn(Collections.singletonList(dataValidationStatus_3));
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel2, Arrays.asList(readingRecord1, readingRecord2), range2))
                .thenReturn(Arrays.asList(dataValidationStatus_1, dataValidationStatus_2));
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

    private DataValidationStatus mockSuspectValidationStatus(Instant timeStamp, ValidationRule validationRule) {
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

    private DataValidationStatus mockValidationStatus(Instant timeStamp, ValidationRule validationRule) {
        DataValidationStatus validationStatus = mock(DataValidationStatus.class);

        when(validationStatus.getReadingTimestamp()).thenReturn(timeStamp);
        when(validationStatus.completelyValidated()).thenReturn(true);
        ReadingQualityType qualityType = new ReadingQualityType("3.1.0");
        ReadingQualityRecord quality = mock(ReadingQualityRecord.class);
        when(quality.getType()).thenReturn(qualityType);
        doReturn(Collections.singletonList(quality)).when(validationStatus).getReadingQualities();
        when(validationStatus.getValidationResult()).thenReturn(ValidationResult.VALID);
        when(validationStatus.getOffendedRules()).thenReturn(Collections.singletonList(validationRule));
        return validationStatus;
    }
}
