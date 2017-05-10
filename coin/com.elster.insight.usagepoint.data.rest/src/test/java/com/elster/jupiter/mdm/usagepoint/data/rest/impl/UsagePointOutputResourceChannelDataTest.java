/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.BigDecimalFunction;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.inject.Singleton;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceChannelDataTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGE_POINT_NAME = "Le nom";
    private static final Instant TIMESTAMP = Instant.ofEpochMilli(1410774620100L);

    private static final Range<Instant> INTERVAL_1 = Range.openClosed(TIMESTAMP.plus(0, ChronoUnit.MINUTES), TIMESTAMP.plus(15, ChronoUnit.MINUTES));
    private static final Range<Instant> INTERVAL_2 = Range.openClosed(TIMESTAMP.plus(15, ChronoUnit.MINUTES), TIMESTAMP.plus(30, ChronoUnit.MINUTES));
    private static final Range<Instant> INTERVAL_3 = Range.openClosed(TIMESTAMP.plus(30, ChronoUnit.MINUTES), TIMESTAMP.plus(45, ChronoUnit.MINUTES));
    private static final Range<Instant> INTERVAL_4 = Range.openClosed(TIMESTAMP.plus(45, ChronoUnit.MINUTES), TIMESTAMP.plus(1, ChronoUnit.HOURS));

    private static final long SOURCE_INTERVAL_START = 1492150500000L;
    private static final long SOURCE_INTERVAL_END = 1492151400000L;
    private static final long REFERENCE_INTERVAL_START = 1492146900000L;
    private static final long REFERENCE_INTERVAL_END = 1492147800000L;

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ChannelsContainer channelsContainer1, channelsContainer2;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingTypeInfoFactory readingTypeInfoFactory;
    @Mock
    private ReadingQualityInfoFactory readingQualityInfoFactory;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC1, effectiveMC2, effectiveMC3, effectiveMC4;
    @Mock
    private AggregatedChannel channel1, channel2;
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private EstimationRuleInfoFactory estimationRuleInfoFactory;

    @Captor
    private ArgumentCaptor<List<IntervalReadingImpl>> intervalReadingsCaptor;

    MetrologyPurpose billing;
    MetrologyPurpose information;

    @Before
    public void before() {
        Category timeOfUseCategory = mock(Category.class);
        when(this.calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name())).thenReturn(Optional.of(timeOfUseCategory));
        UsagePoint.UsedCalendars usedCalendars = mock(UsagePoint.UsedCalendars.class);
        when(usedCalendars.getCalendar(any(Instant.class), any(Category.class))).thenReturn(Optional.empty());
        when(this.usagePoint.getUsedCalendars()).thenReturn(usedCalendars);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findReadingQualityComment(anyLong())).thenReturn(Optional.empty());

        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC1));
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(effectiveMC1, effectiveMC2, effectiveMC3, effectiveMC4));
        billing = mockMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        information = mockMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        UsagePointMetrologyConfiguration metrologyConfiguration1 = mockMetrologyConfigurationWithContract(1, "mc1", billing, information);
        when(effectiveMC1.getMetrologyConfiguration()).thenReturn(metrologyConfiguration1);
        when(effectiveMC1.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC1.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer1));
        when(effectiveMC1.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel1));
        UsagePointMetrologyConfiguration metrologyConfiguration2 = mockMetrologyConfigurationWithContract(2, "mc2", billing, information);
        when(effectiveMC2.getMetrologyConfiguration()).thenReturn(metrologyConfiguration2);
        when(effectiveMC2.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC2.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer2));
        when(effectiveMC2.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel2));
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
                    when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.closedOpen(TIMESTAMP.minusSeconds(3600), TIMESTAMP)));
                    when(channelsContainer.getChannel(any(ReadingType.class))).thenReturn(Optional.empty());
                    when(channelsContainer.getChannels()).thenReturn(Collections.emptyList());
                    when(effectiveMC4.getAggregatedChannel(eq(contract), any(ReadingType.class))).thenReturn(Optional.empty());
                });
        when(effectiveMC4.getMetrologyConfiguration()).thenReturn(metrologyConfiguration4);
        when(effectiveMC4.getUsagePoint()).thenReturn(usagePoint);

        when(channelsContainer1.getInterval()).thenReturn(Interval.of(Range.atLeast(INTERVAL_3.upperEndpoint())));
        when(channelsContainer1.getChannel(any())).thenReturn(Optional.of(channel1));
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(Range.closedOpen(TIMESTAMP, INTERVAL_3.upperEndpoint())));
        when(channelsContainer2.getChannel(any())).thenReturn(Optional.of(channel2));
        when(channel1.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channel1.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(channel2.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channel2.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(channel1.toList(Range.openClosed(INTERVAL_4.lowerEndpoint(), INTERVAL_4.upperEndpoint())))
                .thenReturn(Collections.singletonList(INTERVAL_4.upperEndpoint()));
        when(channel2.toList(Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_3.upperEndpoint())))
                .thenReturn(Arrays.asList(
                        INTERVAL_1.upperEndpoint(),
                        INTERVAL_2.upperEndpoint(),
                        INTERVAL_3.upperEndpoint())
                );
        when(channel2.toList(Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint()))).thenReturn(
                Arrays.asList(INTERVAL_1.upperEndpoint(), INTERVAL_2.upperEndpoint(), INTERVAL_3.upperEndpoint())
        );
        when(usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getRange()).thenReturn(Range.atLeast(INTERVAL_1.lowerEndpoint()));

        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getLastChecked(eq(channelsContainer1), any(ReadingType.class))).thenReturn(Optional.of(INTERVAL_1.lowerEndpoint()));

        Estimator estimator = mock(Estimator.class);
        EstimationResult estimationResult = mock(EstimationResult.class);
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        Estimatable estimatable = mock(Estimatable.class);
        when(estimator.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(estimator.estimate(anyListOf(EstimationBlock.class), any(QualityCodeSystem.class))).thenReturn(estimationResult);
        when(estimationResult.estimated()).thenReturn(Collections.singletonList(estimationBlock));
        doReturn(Collections.singletonList(estimatable)).when(estimationBlock).estimatables();
        when(estimatable.getTimestamp()).thenReturn(INTERVAL_3.upperEndpoint());
        when(estimatable.getEstimation()).thenReturn(BigDecimal.valueOf(327L));
        when(estimationService.getEstimator("com.elster.jupiter.estimators.impl.ValueFillEstimator")).thenReturn(Optional.of(estimator));
        when(estimationService.getEstimator(eq("com.elster.jupiter.estimators.impl.ValueFillEstimator"), any())).thenReturn(Optional.of(estimator));
        when(estimationService.previewEstimate(any(QualityCodeSystem.class), eq(channelsContainer2), any(), any(), eq(estimator))).thenReturn(estimationResult);
    }

    private String defaultFilter() throws UnsupportedEncodingException {
        return this.buildFilter(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint());
    }

    private String buildFilter(Instant start, Instant end) throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", start.toEpochMilli())
                .property("intervalEnd", end.toEpochMilli())
                .create();
    }

    private String buildFilterForData(Instant start, Instant end, Long intervalId, String unitId, boolean includeBulk) throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", start.toEpochMilli())
                .property("intervalEnd", end.toEpochMilli())
                .property("timeInterval", intervalId)
                .property("unit", unitId)
                .property("bulk", String.valueOf(includeBulk))
                .create();
    }

    @Test
    public void testGetChannelDataNoSuchUsagePoint() throws UnsupportedEncodingException {
        // Business method
        Response response = target("/usagepoints/xxx/purposes/100/outputs/1/channelData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataNoMetrologyConfigurationOnUsagePoint() throws UnsupportedEncodingException {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.emptyList());

        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataNoSuchContract() throws UnsupportedEncodingException {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/90030004443343/outputs/1/channelData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataMissingIntervalStart() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalEnd", TIMESTAMP.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.channelData")).isEmpty();
    }

    @Test
    public void testGetChannelDataMissingIntervalEnd() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalStart", TIMESTAMP.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.channelData")).isEmpty();
    }

    @Test
    public void testGetChannelDataOnIrregularReadingTypeDeliverable() throws UnsupportedEncodingException {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/channelData")
                .queryParam("filter", defaultFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataRequestedIntervalDoesNotContainData() throws UnsupportedEncodingException {
        String filter = ExtjsFilter.filter()
                .property("intervalStart", TIMESTAMP.minus(15, ChronoUnit.MINUTES).toEpochMilli())
                .property("intervalEnd", TIMESTAMP.minus(10, ChronoUnit.MINUTES).toEpochMilli())
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
    public void testGetChannelData() throws UnsupportedEncodingException {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(channel.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channel.toList(Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())))
                .thenReturn(
                        Arrays.asList(
                                INTERVAL_1.upperEndpoint(),
                                INTERVAL_2.upperEndpoint(),
                                INTERVAL_3.upperEndpoint(),
                                INTERVAL_4.upperEndpoint())
                );
        when(effectiveMC1.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(this.usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(this.effectiveMC1));
        when(this.channelsContainer1.getInterval()).thenReturn(Interval.of(Range.closedOpen(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())));
        mockDifferentIntervalReadings(channel, this.channelsContainer1);


        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", defaultFilter()).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);

        assertThat(jsonModel.<Long>get("$.channelData[0].interval.start")).isEqualTo(INTERVAL_4.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.end")).isEqualTo(INTERVAL_4.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].reportedDateTime")).isEqualTo(INTERVAL_4.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[0].value")).isEqualTo("10");
        assertThat(jsonModel.<Boolean>get("$.channelData[0].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<Number>get("$.channelData[0].estimatedByRule.id")).isEqualTo(3);
        assertThat(jsonModel.<String>get("$.channelData[0].estimatedByRule.name")).isEqualTo("Estimation");
        assertThat(jsonModel.<String>get("$.channelData[0].readingQualities[0].comment")).isEqualTo("Estimated by market rule 11");

        assertThat(jsonModel.<Long>get("$.channelData[1].interval.start")).isEqualTo(INTERVAL_3.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[1].interval.end")).isEqualTo(INTERVAL_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[1].reportedDateTime")).isEqualTo(INTERVAL_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[1].value")).isEqualTo("10");
        assertThat(jsonModel.<Boolean>get("$.channelData[1].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[1].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[1].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[1].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.channelData[1].validationRules[0].name")).isEqualTo("MinMax");


        assertThat(jsonModel.<Long>get("$.channelData[2].interval.start")).isEqualTo(INTERVAL_2.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[2].interval.end")).isEqualTo(INTERVAL_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[2].reportedDateTime")).isNull();  // Missing intervals do not have a reported time
        assertThat(jsonModel.<Boolean>get("$.channelData[2].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[2].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[2].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[2].validationRules[0].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.channelData[2].validationRules[0].name")).isEqualTo("Missing");


        assertThat(jsonModel.<Long>get("$.channelData[3].interval.start")).isEqualTo(INTERVAL_1.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[3].interval.end")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[3].reportedDateTime")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[3].value")).isEqualTo("1");
        assertThat(jsonModel.<Boolean>get("$.channelData[3].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[3].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[3].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[3].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.channelData[3].validationRules[0].name")).isEqualTo("MinMax");

    }

    @Test
    public void testGetSuspectChannelData() throws UnsupportedEncodingException {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(channel.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channel.toList(Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())))
                .thenReturn(Arrays.asList(
                        INTERVAL_1.upperEndpoint(),
                        INTERVAL_2.upperEndpoint(),
                        INTERVAL_3.upperEndpoint(),
                        INTERVAL_4.upperEndpoint())
                );
        when(this.effectiveMC1.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(this.channelsContainer1.getInterval()).thenReturn(Interval.of(Range.closedOpen(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())));
        when(this.usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(this.effectiveMC1));
        mockDifferentIntervalReadings(channel, this.channelsContainer1);
        String filter = ExtjsFilter.filter()
                .property("intervalStart", INTERVAL_1.lowerEndpoint().toEpochMilli())
                .property("intervalEnd", INTERVAL_4.upperEndpoint().toEpochMilli())
                .property("suspect", Collections.singletonList("suspect"))
                .create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);

        assertThat(jsonModel.<Long>get("$.channelData[0].interval.start")).isEqualTo(INTERVAL_3.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.end")).isEqualTo(INTERVAL_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].reportedDateTime")).isEqualTo(INTERVAL_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[0].value")).isEqualTo("10");
        assertThat(jsonModel.<Boolean>get("$.channelData[0].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[0].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[0].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[0].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.channelData[0].validationRules[0].name")).isEqualTo("MinMax");
    }

    @Test
    public void testGetChannelDataOfMonthlyChannel() throws UnsupportedEncodingException {
        ZonedDateTime time = ZonedDateTime.of(LocalDateTime.of(2016, 5, 1, 0, 0), ZoneId.systemDefault());
        Range<Instant> interval_JUN = Range.openClosed(time.toInstant(), time.with(Month.JUNE).toInstant());
        Range<Instant> interval_JUL = Range.openClosed(time.with(Month.JUNE).toInstant(), time.with(Month.JULY).toInstant());
        Range<Instant> interval_AUG = Range.openClosed(time.with(Month.JULY).toInstant(), time.with(Month.AUGUST).toInstant());
        Range<Instant> interval_SEP = Range.openClosed(time.with(Month.AUGUST).toInstant(), time.with(Month.SEPTEMBER).toInstant());

        when(channel1.getIntervalLength()).thenReturn(Optional.of(Period.ofMonths(1)));
        when(channel1.toList(Range.openClosed(time.toInstant(), interval_AUG.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_JUN.upperEndpoint(), interval_JUL.upperEndpoint(), interval_AUG.upperEndpoint())
        );
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord1 = mockAggregatedIntervalReadingRecord(interval_JUN, BigDecimal.ONE);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord2 = mockAggregatedIntervalReadingRecord(interval_JUL, BigDecimal.TEN);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord3 = mockAggregatedIntervalReadingRecord(interval_AUG, BigDecimal.ONE);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord4 = mockAggregatedIntervalReadingRecord(interval_SEP, BigDecimal.ONE);
        when(channel1.getAggregatedIntervalReadings(any())).thenReturn(
                Arrays.asList(
                        aggregatedIntervalReadingRecord1,
                        aggregatedIntervalReadingRecord2,
                        aggregatedIntervalReadingRecord3,
                        aggregatedIntervalReadingRecord4));
        IntervalReadingRecord intervalReadingRecord1 = mockIntervalReadingRecord(aggregatedIntervalReadingRecord1);
        IntervalReadingRecord intervalReadingRecord2 = mockIntervalReadingRecord(aggregatedIntervalReadingRecord2);
        IntervalReadingRecord intervalReadingRecord3 = mockIntervalReadingRecord(aggregatedIntervalReadingRecord3);
        IntervalReadingRecord intervalReadingRecord4 = mockIntervalReadingRecord(aggregatedIntervalReadingRecord4);
        when(channel1.getIntervalReadings(any()))
                .thenReturn(
                        Arrays.asList(
                                intervalReadingRecord1,
                                intervalReadingRecord2,
                                intervalReadingRecord3,
                                intervalReadingRecord4)); //Intentionally returns more then three
        ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
        when(evaluator.isValidationEnabled(this.channel1)).thenReturn(true);
        when(evaluator.getLastChecked(eq(this.channelsContainer1), any(ReadingType.class))).thenReturn(Optional.of(TIMESTAMP));
        when(validationService.getEvaluator()).thenReturn(evaluator);

        String filter = ExtjsFilter.filter()
                .property("intervalStart", time.toInstant().toEpochMilli())
                .property("intervalEnd", interval_AUG.upperEndpoint().toEpochMilli())
                .create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<String>>get("$.channelData[*].value")).containsExactly("1", "10", "1");
    }

    @Test
    public void testPreviewEstimateChannelData() throws IOException {
        when(channel1.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channel1.toList(Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_3.upperEndpoint()))).thenReturn(
                Arrays.asList(INTERVAL_1.upperEndpoint(), INTERVAL_2.upperEndpoint(), INTERVAL_3.upperEndpoint())
        );
        mockIntervalReadingsWithValidationResult();

        EstimateChannelDataInfo info = new EstimateChannelDataInfo();
        info.intervals = Collections.singletonList(IntervalInfo.from(INTERVAL_3));
        info.estimatorImpl = "com.elster.jupiter.estimators.impl.ValueFillEstimator";
        info.properties = new ArrayList<>();
        info.commentId = 0L;

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData/estimate")
                .request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$[0].value")).isEqualTo("327");
    }

    @Test
    public void testEditChannelData() throws Exception {
        DataAggregationService.MetrologyContractDataEditor editor = FakeBuilder.initBuilderStub(usagePoint, DataAggregationService.MetrologyContractDataEditor.class);
        when(dataAggregationService.edit(eq(usagePoint), any(MetrologyContract.class), any(ReadingTypeDeliverable.class), eq(QualityCodeSystem.MDM))).thenReturn(editor);

        ReadingQualityComment readingQualityComment = mock(ReadingQualityComment.class);
        when(readingQualityComment.getId()).thenReturn(555L);
        when(readingQualityComment.getComment()).thenReturn("Estimated by market rule 11");
        when(meteringService.findReadingQualityComment(any(Long.class))).thenReturn(Optional.of(readingQualityComment));

        OutputChannelDataInfo info1 = new OutputChannelDataInfo();
        info1.value = BigDecimal.valueOf(101L);
        info1.reportedDateTime = INTERVAL_3.upperEndpoint();
        info1.interval = IntervalInfo.from(INTERVAL_3);
        OutputChannelDataInfo info2 = new OutputChannelDataInfo();
        info2.value = BigDecimal.valueOf(102L);
        info2.reportedDateTime = INTERVAL_4.upperEndpoint();
        info2.interval = IntervalInfo.from(INTERVAL_4);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .request().put(Entity.json(Arrays.asList(info1, info2)));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<List> readingsCaptor = ArgumentCaptor.forClass(List.class);
        verify(editor).updateAll(readingsCaptor.capture());
        assertThat(readingsCaptor.getAllValues()).hasSize(1);
        assertThat(readingsCaptor.getValue()).hasSize(2);

        Object capturedListElement = readingsCaptor.getValue().get(0);
        assertThat(capturedListElement).isInstanceOf(BaseReading.class);
        BaseReading capturedReading = (BaseReading) capturedListElement;
        assertThat(capturedReading.getValue()).isEqualTo(info1.value);
        assertThat(capturedReading.getTimeStamp()).isEqualTo(INTERVAL_3.upperEndpoint());

        capturedListElement = readingsCaptor.getValue().get(1);
        assertThat(capturedListElement).isInstanceOf(BaseReading.class);
        capturedReading = (BaseReading) capturedListElement;
        assertThat(capturedReading.getValue()).isEqualTo(info2.value);
        assertThat(capturedReading.getTimeStamp()).isEqualTo(INTERVAL_4.upperEndpoint());
    }

    @Test
    public void testConfirmChannelData() throws Exception {
        DataAggregationService.MetrologyContractDataEditor editor = FakeBuilder.initBuilderStub(usagePoint, DataAggregationService.MetrologyContractDataEditor.class);
        when(dataAggregationService.edit(eq(usagePoint), any(MetrologyContract.class), any(ReadingTypeDeliverable.class), eq(QualityCodeSystem.MDM))).thenReturn(editor);

        OutputChannelDataInfo info1 = new OutputChannelDataInfo();
        info1.isConfirmed = true;
        info1.reportedDateTime = INTERVAL_4.upperEndpoint();
        info1.interval = IntervalInfo.from(INTERVAL_4);
        OutputChannelDataInfo info2 = new OutputChannelDataInfo();
        info2.isConfirmed = true;
        info2.reportedDateTime = INTERVAL_3.upperEndpoint();
        info2.interval = IntervalInfo.from(INTERVAL_3);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .request().put(Entity.json(Arrays.asList(info1, info2)));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<List> readingsCaptor = ArgumentCaptor.forClass(List.class);
        verify(editor).confirmAll(readingsCaptor.capture());
        assertThat(readingsCaptor.getAllValues()).hasSize(1);
        assertThat(readingsCaptor.getValue()).hasSize(2);

        Object capturedListElement = readingsCaptor.getValue().get(0);
        assertThat(capturedListElement).isInstanceOf(BaseReading.class);
        BaseReading capturedReading = (BaseReading) capturedListElement;
        assertThat(capturedReading.getTimeStamp()).isEqualTo(INTERVAL_4.upperEndpoint());

        capturedListElement = readingsCaptor.getValue().get(1);
        assertThat(capturedListElement).isInstanceOf(BaseReading.class);
        capturedReading = (BaseReading) capturedListElement;
        assertThat(capturedReading.getTimeStamp()).isEqualTo(INTERVAL_3.upperEndpoint());
    }

    @Test
    public void testRemoveChannelData() throws Exception {
        DataAggregationService.MetrologyContractDataEditor editor = FakeBuilder.initBuilderStub(usagePoint, DataAggregationService.MetrologyContractDataEditor.class);
        when(dataAggregationService.edit(eq(usagePoint), any(MetrologyContract.class), any(ReadingTypeDeliverable.class), eq(QualityCodeSystem.MDM))).thenReturn(editor);

        OutputChannelDataInfo info1 = new OutputChannelDataInfo();
        info1.value = null;
        info1.reportedDateTime = INTERVAL_1.upperEndpoint();
        info1.interval = IntervalInfo.from(INTERVAL_1);
        OutputChannelDataInfo info2 = new OutputChannelDataInfo();
        info2.value = null;
        info2.reportedDateTime = INTERVAL_4.upperEndpoint();
        info2.interval = IntervalInfo.from(INTERVAL_4);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .request().put(Entity.json(Arrays.asList(info1, info2)));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(editor).removeTimestamps(eq(ImmutableSet.of(INTERVAL_1.upperEndpoint(), INTERVAL_4.upperEndpoint())));
    }

    @Test
    public void testGetReadingQualitiesFromCalculatedReading() throws UnsupportedEncodingException {
        Range<Instant> interval = Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_1.upperEndpoint());
        when(channel2.toList(interval)).thenReturn(Collections.singletonList(INTERVAL_1.upperEndpoint()));
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedReading = mockAggregatedIntervalReadingRecord(INTERVAL_1, BigDecimal.ONE);
        when(channel2.getAggregatedIntervalReadings(interval)).thenReturn(Collections.singletonList(aggregatedReading));
        doReturn(Arrays.asList(
                mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC)),
                mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT))
        )).when(aggregatedReading).getReadingQualities();
        when(this.evaluator.isValidationEnabled(this.channel2)).thenReturn(true);
        when(this.evaluator.getLastChecked(eq(this.channelsContainer2), any(ReadingType.class))).thenReturn(Optional.of(TIMESTAMP));

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", buildFilter(INTERVAL_1.lowerEndpoint(), INTERVAL_1.upperEndpoint())).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.start")).isEqualTo(INTERVAL_1.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.end")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].reportedDateTime")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[0].value")).isEqualTo("1");
        assertThat(jsonModel.<List<?>>get("$.channelData[0].readingQualities")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.channelData[0].readingQualities[*].cimCode")).containsOnly("2.7.0", "2.5.258");
    }

    @Test
    public void testGetChannelDataOfAllOutputs() throws UnsupportedEncodingException {
        IdWithNameInfo intervalInfo  = new IdWithNameInfo(11, "Daily");
        IdWithNameInfo unitInfo = new IdWithNameInfo("-2:5", "A");
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(effectiveMC2));
        mockDifferentIntervalReadings();
        effectiveMC2.getMetrologyConfiguration().getContracts().forEach( contract -> {
                    contract.getDeliverables().forEach(deliverable -> {
                                when(deliverable.getReadingType().isCumulative()).thenReturn(true);
                                when(deliverable.getReadingType().getMultiplier()).thenReturn(MetricMultiplier.CENTI);
                                when(deliverable.getReadingType().getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
                                    }
                            );
                });
        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/data")
                .queryParam("filter", buildFilterForData(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint(), 11L, "-2:5", true)).request().get(String.class);
        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<Long>get("$.data[0].interval.start")).isEqualTo(INTERVAL_3.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.data[0].interval.end")).isEqualTo(INTERVAL_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Map>get("$.data[0].channelData")).containsEntry("1",10);
        assertThat(jsonModel.<Long>get("$.data[1].interval.start")).isEqualTo(INTERVAL_1.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.data[1].interval.end")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Map>get("$.data[1].channelData")).containsEntry("1",1);
    }

    @Test
    public void testGetReadingQualitiesFromPersistedReading() throws UnsupportedEncodingException {
        Range<Instant> interval = Range.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_1.upperEndpoint());
        when(channel2.toList(interval)).thenReturn(Collections.singletonList(INTERVAL_1.upperEndpoint()));
        AggregatedChannel.AggregatedIntervalReadingRecord reading = mockAggregatedIntervalReadingRecord(INTERVAL_1, BigDecimal.ONE);
        when(channel2.getAggregatedIntervalReadings(interval)).thenReturn(Collections.singletonList(reading));

        DataValidationStatus dataValidationStatus = mockValidationStatus(INTERVAL_1.upperEndpoint(), mockValidationRule(1, "MinMax"));
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel2, Collections.singletonList(reading), INTERVAL_1))
                .thenReturn(Collections.singletonList(dataValidationStatus));
        when(this.evaluator.isValidationEnabled(this.channel2)).thenReturn(true);
        when(this.evaluator.getLastChecked(eq(this.channelsContainer2), any(ReadingType.class))).thenReturn(Optional.of(TIMESTAMP));

        doReturn(Arrays.asList(
                mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC)),
                mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)),
                mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.VALIDATED))// 3.0.1 should be filtered out
        )).when(dataValidationStatus).getReadingQualities();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", buildFilter(INTERVAL_1.lowerEndpoint(), INTERVAL_1.upperEndpoint())).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.start")).isEqualTo(INTERVAL_1.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.end")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].reportedDateTime")).isEqualTo(INTERVAL_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[0].value")).isEqualTo("1");
        assertThat(jsonModel.<List<?>>get("$.channelData[0].readingQualities")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.channelData[0].readingQualities[*].cimCode")).containsOnly("3.7.0", "3.5.258");
    }

    private ReadingQualityRecord mockReadingQualityRecord(ReadingQualityType type) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.getType()).thenReturn(type);
        return readingQualityRecord;
    }

    @Test
    public void testReadingValidationInfoForMissedReadingInTheMiddleOfValidatedData() {
        OutputChannelDataInfoFactory factory = new OutputChannelDataInfoFactory(new ValidationRuleInfoFactory(propertyValueInfoService, readingTypeInfoFactory), readingQualityInfoFactory, estimationRuleInfoFactory, meteringService);
        ChannelReadingWithValidationStatus status = mock(ChannelReadingWithValidationStatus.class);
        when(status.getTimeStamp()).thenReturn(TIMESTAMP.minus(1, ChronoUnit.DAYS));
        when(status.getTimePeriod()).thenReturn(Range.closedOpen(TIMESTAMP.minus(1, ChronoUnit.DAYS), TIMESTAMP));
        when(status.getValidationStatus()).thenReturn(Optional.empty());
        when(status.getChannelLastChecked()).thenReturn(Optional.of(TIMESTAMP));
        when(status.isChannelValidationActive()).thenReturn(true);
        when(status.getCalculatedValue()).thenReturn(null);
        when(status.getReadingModificationFlag()).thenReturn(Optional.empty());
        when(status.getCalendar()).thenReturn(Optional.empty());
        OutputChannelDataInfo info = factory.createChannelDataInfo(status);
        assertThat(info.dataValidated).isTrue();
        assertThat(info.validationResult).isEqualTo(ValidationStatus.OK);
    }

    @Test
    public void testReadingValidationInfoForMissedReadingAfterLastCheckedDate() {
        OutputChannelDataInfoFactory factory = new OutputChannelDataInfoFactory(new ValidationRuleInfoFactory(propertyValueInfoService, readingTypeInfoFactory), readingQualityInfoFactory, estimationRuleInfoFactory, meteringService);
        ChannelReadingWithValidationStatus status = mock(ChannelReadingWithValidationStatus.class);
        Instant dayAfter = TIMESTAMP.plus(1, ChronoUnit.DAYS);
        when(status.getTimeStamp()).thenReturn(dayAfter);
        when(status.getTimePeriod()).thenReturn(Range.closedOpen(dayAfter, TIMESTAMP.plus(2, ChronoUnit.DAYS)));
        when(status.getValidationStatus()).thenReturn(Optional.empty());
        when(status.getChannelLastChecked()).thenReturn(Optional.of(TIMESTAMP));
        when(status.isChannelValidationActive()).thenReturn(true);
        when(status.getCalculatedValue()).thenReturn(null);
        when(status.getReadingModificationFlag()).thenReturn(Optional.empty());
        when(status.getCalendar()).thenReturn(Optional.empty());
        OutputChannelDataInfo info = factory.createChannelDataInfo(status);
        assertThat(info.dataValidated).isFalse();
        assertThat(info.validationResult).isEqualTo(ValidationStatus.NOT_VALIDATED);
    }

    @Test
    public void testGetApplicableEstimationRules() throws IOException {
        mockPropertyValueInfoService();

        MetrologyContract contract1 = effectiveMC1.getMetrologyConfiguration().getContracts().get(0);
        MetrologyContract contract2 = effectiveMC2.getMetrologyConfiguration().getContracts().get(0);
        MetrologyContract contract3 = effectiveMC4.getMetrologyConfiguration().getContracts().get(0);
        EstimationRuleSet estimationRuleSet1 = mockEstimationRuleSet(15, 17, "ABC", regularReadingType);
        EstimationRuleSet estimationRuleSet2 = mockEstimationRuleSet(16, 18, "AAC", regularReadingType);
        EstimationRuleSet estimationRuleSet3 = mockEstimationRuleSet(17, 19, "CBA", irregularReadingType);
        when(usagePointConfigurationService.getEstimationRuleSets(contract1)).thenReturn(Collections.singletonList(estimationRuleSet1));
        when(usagePointConfigurationService.getEstimationRuleSets(contract2)).thenReturn(Collections.singletonList(estimationRuleSet2));
        when(usagePointConfigurationService.getEstimationRuleSets(contract3)).thenReturn(Collections.singletonList(estimationRuleSet3));

        UsagePointEstimation usagePointEstimation = mock(UsagePointEstimation.class);
        when(usagePointDataModelService.forEstimation(usagePoint)).thenReturn(usagePointEstimation);
        doReturn(Optional.empty()).when(usagePointEstimation).findOverriddenProperties(any(), any());

        EstimationRule ruleWithOverriddenProperties = estimationRuleSet2.getRules().get(0);
        List<PropertySpec> propertySpecs = Arrays.asList(mockPropertySpec("P1"), mockPropertySpec("P2"));
        when(ruleWithOverriddenProperties.getPropertySpecs()).thenReturn(propertySpecs);
        when(ruleWithOverriddenProperties.getProps()).thenReturn(ImmutableMap.of("P1", 100));
        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        doReturn(Optional.of(overriddenProperties)).when(usagePointEstimation).findOverriddenProperties(ruleWithOverriddenProperties, regularReadingType);
        when(overriddenProperties.getProperties()).thenReturn(ImmutableMap.of("P2", 200));

        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData/applicableEstimationRules").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Number>>get("$.rules[*].id")).containsExactly(18, 17);
        assertThat(jsonModel.<List<String>>get("$.rules[*].name")).containsExactly("AAC", "ABC");
        assertThat(jsonModel.<List<Number>>get("$.rules[*].ruleSetId")).containsExactly(16, 15);
        assertThat(jsonModel.<List<Boolean>>get("$.rules[*].deleted")).containsExactly(false, false);
        assertThat(jsonModel.<List<String>>get("$.rules[*].application.id")).containsExactly("MDM", "MDM");

        assertThat(jsonModel.<List<String>>get("$.rules[0].properties[*].key")).containsExactly("P1", "P2");
        assertThat(jsonModel.<List<Number>>get("$.rules[0].properties[*].propertyValueInfo.value")).containsExactly(100, 200);
    }

    private static EstimationRuleSet mockEstimationRuleSet(long id, long ruleId, String ruleName, ReadingType readingType) {
        EstimationRule estimationRule = mock(EstimationRule.class);
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        doReturn(Collections.singletonList(estimationRule)).when(estimationRuleSet).getRules();
        doReturn(Collections.singletonList(estimationRule)).when(estimationRuleSet).getRules(Collections.singleton(readingType));
        when(estimationRule.getRuleSet()).thenReturn(estimationRuleSet);
        when(estimationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(estimationRuleSet.getId()).thenReturn(id);
        when(estimationRule.getId()).thenReturn(ruleId);
        when(estimationRule.getName()).thenReturn(ruleName);
        when(estimationRule.getComment()).thenReturn(Optional.empty());
        return estimationRuleSet;
    }

    private void mockIntervalReadingsWithValidationResult() {
        ValidationRule minMax = mockValidationRule(1, "MinMax");
        ValidationRule missing = mockValidationRule(2, "Missing");

        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord1 = mockAggregatedIntervalReadingRecord(INTERVAL_1, BigDecimal.ONE);
        DataValidationStatus dataValidationStatus_1 = mockValidationStatus(INTERVAL_1.upperEndpoint(), minMax);
        // 2nd intentionally missing
        DataValidationStatus dataValidationStatus_2 = mockValidationStatus(INTERVAL_2.upperEndpoint(), missing);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord3 = mockAggregatedIntervalReadingRecord(INTERVAL_3, BigDecimal.TEN);
        DataValidationStatus dataValidationStatus_3 = mockValidationStatus(INTERVAL_3.upperEndpoint(), minMax);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord4 = mockAggregatedIntervalReadingRecord(INTERVAL_4, BigDecimal.ZERO);
        DataValidationStatus dataValidationStatus_4 = mockValidationStatus(INTERVAL_4.upperEndpoint(), minMax);

        List<IntervalReadingRecord> intervalReadings = Arrays.asList(aggregatedIntervalReadingRecord1, aggregatedIntervalReadingRecord3);
        List<AggregatedChannel.AggregatedIntervalReadingRecord> aggregatedIntervalReadings = Arrays.asList(aggregatedIntervalReadingRecord1, aggregatedIntervalReadingRecord3);
        when(channel2.getIntervalReadings(any())).thenReturn(intervalReadings);
        when(channel2.getAggregatedIntervalReadings(any())).thenReturn(aggregatedIntervalReadings);
        when(channel2.getReading(INTERVAL_1.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord1));
        when(channel2.getReading(INTERVAL_3.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord3));
        when(channel1.getIntervalReadings(any())).thenReturn(Collections.singletonList(aggregatedIntervalReadingRecord1));
        when(channel1.getAggregatedIntervalReadings(any())).thenReturn(Collections.singletonList(aggregatedIntervalReadingRecord1));
        when(channel1.getReading(INTERVAL_4.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord4));

        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel2, intervalReadings,
                Ranges.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())))
                .thenReturn(Arrays.asList(dataValidationStatus_1, dataValidationStatus_2, dataValidationStatus_3));
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel1, Collections.singletonList(aggregatedIntervalReadingRecord4),
                Ranges.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())))
                .thenReturn(Collections.singletonList(dataValidationStatus_4));
    }

    private void mockDifferentIntervalReadings(AggregatedChannel channel, ChannelsContainer channelsContainer) {
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        ValidationRule minMax = mockValidationRule(1, "MinMax");
        ValidationRule missing = mockValidationRule(2, "Missing");
        EstimationRule estimation = mockEstimationRule(3, "Estimation");

        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord1 = mockAggregatedIntervalReadingRecord(INTERVAL_1, BigDecimal.ONE);
        DataValidationStatus dataValidationStatus_1 = mockValidationStatus(INTERVAL_1.upperEndpoint(), minMax);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord2 = mockAggregatedIntervalReadingRecord(INTERVAL_1, BigDecimal.ONE);
        DataValidationStatus dataValidationStatus_2 = mockValidationStatus(INTERVAL_2.upperEndpoint(), missing);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord3 = mockAggregatedIntervalReadingRecord(INTERVAL_3, BigDecimal.TEN);
        DataValidationStatus dataValidationStatus_3 = mockValidationStatus(INTERVAL_3.upperEndpoint(), minMax);
        AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord4 = mockAggregatedIntervalReadingRecord(INTERVAL_4, BigDecimal.TEN);
        DataValidationStatus dataValidationStatus_4 = mockEstimationStatus(INTERVAL_4.upperEndpoint(), estimation);

        List<IntervalReadingRecord> intervalReadings = Arrays.asList(aggregatedIntervalReadingRecord1, aggregatedIntervalReadingRecord2, aggregatedIntervalReadingRecord3, aggregatedIntervalReadingRecord4);
        List<AggregatedChannel.AggregatedIntervalReadingRecord> aggregatedIntervalReadings = Arrays.asList(aggregatedIntervalReadingRecord1, aggregatedIntervalReadingRecord2, aggregatedIntervalReadingRecord3, aggregatedIntervalReadingRecord4);
        when(channel.getIntervalReadings(any())).thenReturn(intervalReadings);
        when(channel.getAggregatedIntervalReadings(any())).thenReturn(aggregatedIntervalReadings);
        when(channel.getReading(INTERVAL_1.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord1));
        when(channel.getReading(INTERVAL_2.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord2));
        when(channel.getReading(INTERVAL_3.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord3));
        when(channel.getReading(INTERVAL_4.upperEndpoint())).thenReturn(Optional.of(aggregatedIntervalReadingRecord4));
        when(evaluator
                .getValidationStatus(
                        EnumSet.of(QualityCodeSystem.MDM),
                        channel,
                        intervalReadings,
                        Ranges.openClosed(INTERVAL_1.lowerEndpoint(), INTERVAL_4.upperEndpoint())))
                .thenReturn(Arrays.asList(
                        dataValidationStatus_1,
                        dataValidationStatus_2,
                        dataValidationStatus_3,
                        dataValidationStatus_4));
        when(this.evaluator.isValidationEnabled(channel)).thenReturn(true);
        when(this.evaluator.getLastChecked(eq(channelsContainer), any(ReadingType.class))).thenReturn(Optional.of(TIMESTAMP));
    }

    @Test
    public void testCorrectValuesOnChannelData() {
        Instant now = Instant.now();
        AggregatedChannel channel = mock(AggregatedChannel.class);
        IntervalReadingRecord readingRecord = mock(IntervalReadingRecord.class);
        TemporalAmount temporalAmount = mock(TemporalAmount.class);
        ReadingQualityComment readingQualityComment = mock(ReadingQualityComment.class);
        ValueCorrectionInfo valueCorrectionInfo = new ValueCorrectionInfo();
        valueCorrectionInfo.intervals = Collections.singletonList(com.elster.jupiter.mdm.common.rest.IntervalInfo.from(Range.openClosed(now, now.plusSeconds(60))));
        valueCorrectionInfo.amount = new BigDecimal(10);
        valueCorrectionInfo.type = BigDecimalFunction.MULTIPLY;
        when(meteringService.findReadingQualityComment(any(Long.class))).thenReturn(Optional.of(readingQualityComment));
        when(readingQualityComment.getId()).thenReturn(555L);
        when(readingQualityComment.getComment()).thenReturn("Estimated by market rule 11");
        when(effectiveMC1.getAggregatedChannel(any(MetrologyContract.class), any(ReadingType.class))).thenReturn(Optional.of(channel));
        when(channel.getIntervalReadings(any(Range.class))).thenReturn(Collections.singletonList(readingRecord));
        when(readingRecord.getValue()).thenReturn(new BigDecimal(10));
        when(readingRecord.getTimeStamp()).thenReturn(now.plusSeconds(60));
        when(readingRecord.getReadingType()).thenReturn(regularReadingType);
        when(regularReadingType.getIntervalLength()).thenReturn(Optional.of(temporalAmount));
        when(temporalAmount.subtractFrom(any(Instant.class))).thenReturn(now);

        JsonModel model = JsonModel.create(target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData/correctValues").request()
                .put(Entity.json(valueCorrectionInfo), String.class));

        assertThat(model.<String>get("$.[0].value")).isEqualTo("100");
    }

    @Test
    public void testCopyFromReferenceChannel() {
        ReadingType readingType = effectiveMC1.getMetrologyConfiguration().getContracts().get(0).getDeliverables().get(0).getReadingType();
        ReferenceChannelDataInfo info = new ReferenceChannelDataInfo();
        info.referenceUsagePoint = "testUP";
        info.readingType = readingType.getMRID();
        List<IntervalInfo> intervalInfos = new ArrayList<>();
        Range<Instant> sourceRange = Range.openClosed(Instant.ofEpochMilli(SOURCE_INTERVAL_START), Instant.ofEpochMilli(SOURCE_INTERVAL_END));
        intervalInfos.add(IntervalInfo.from(sourceRange));
        info.intervals = intervalInfos;
        info.referencePurpose = 1L;
        info.allowSuspectData = true;
        info.completePeriod = true;
        info.startDate = Instant.ofEpochMilli(REFERENCE_INTERVAL_END);
        when(meteringService.findUsagePointByName(info.referenceUsagePoint)).thenReturn(Optional.of(usagePoint));
        when(meteringService.getReadingType(readingType.getMRID())).thenReturn(Optional.of(readingType));
        when(metrologyConfigurationService.findMetrologyPurpose(1L)).thenReturn(Optional.of(billing));
        when(channel1.getMainReadingType()).thenReturn(readingType);
        Range<Instant> referenceRange = Range.openClosed(Instant.ofEpochMilli(REFERENCE_INTERVAL_START), Instant.ofEpochMilli(REFERENCE_INTERVAL_END));
        IntervalReadingRecord readingRecord = mockIntervalReadingRecord(referenceRange, BigDecimal.ONE);
        when(channel1.getIntervalReadings(any())).thenReturn(Collections.singletonList(readingRecord));
        when(usagePoint.getEffectiveMetrologyConfigurations(any())).thenReturn(Collections.singletonList(effectiveMC1));
        when(effectiveMC1.getRange()).thenReturn(Range.all());
        when(effectiveMC1.overlaps(any())).thenReturn(true);
        when(effectiveMC1.isEffectiveAt(Instant.ofEpochMilli(REFERENCE_INTERVAL_END))).thenReturn(true);
        ReadingQualityWithTypeFetcher readingQualityFetcher = mock(ReadingQualityWithTypeFetcher.class);
        when(channel1.findReadingQualities()).thenReturn(readingQualityFetcher);
        doReturn(readingQualityFetcher).when(readingQualityFetcher).ofQualityIndex(QualityCodeIndex.SUSPECT);
        when(readingQualityFetcher.inTimeInterval(any())).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.stream()).thenReturn(Stream.empty());

        JsonModel json = JsonModel.create(target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData/copyfromreference").request()
                .post(Entity.json(info), String.class));

        assertThat(json.<String>get("$.[0].value")).isEqualTo("1");
        assertThat(json.<Long>get("$.[0].interval.start")).isEqualTo(SOURCE_INTERVAL_START);
        assertThat(json.<Long>get("$.[0].interval.end")).isEqualTo(SOURCE_INTERVAL_END);
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

    private EstimationRule mockEstimationRule(long id, String name) {
        EstimationRule estimationRule = mock(EstimationRule.class);
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        ReadingQualityComment readingQualityComment = mock(ReadingQualityComment.class);
        when(readingQualityComment.getId()).thenReturn(555L);
        when(readingQualityComment.getComment()).thenReturn("Estimated by market rule 11");
        when(estimationRule.getId()).thenReturn(id);
        when(estimationRule.getName()).thenReturn(name);
        when(estimationRule.getDisplayName()).thenReturn(name);
        when(estimationRule.getRuleSet()).thenReturn(estimationRuleSet);
        when(estimationRule.getComment()).thenReturn(Optional.of(readingQualityComment));
        when(estimationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        return estimationRule;
    }

    private AggregatedChannel.AggregatedIntervalReadingRecord mockAggregatedIntervalReadingRecord(Range<Instant> interval, BigDecimal value) {
        AggregatedChannel.AggregatedIntervalReadingRecord intervalReadingRecord = mock(AggregatedChannel.AggregatedIntervalReadingRecord.class);
        when(intervalReadingRecord.getTimePeriod()).thenReturn(Optional.of(interval));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(intervalReadingRecord.getReportedDateTime()).thenReturn(interval.upperEndpoint());
        when(intervalReadingRecord.getValue()).thenReturn(value);
        when(intervalReadingRecord.getReadingType()).thenReturn(regularReadingType);
        when(regularReadingType.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        return intervalReadingRecord;
    }

    private IntervalReadingRecord mockIntervalReadingRecord(AggregatedChannel.AggregatedIntervalReadingRecord aggregatedIntervalReadingRecord) {
        return mockIntervalReadingRecord(aggregatedIntervalReadingRecord.getTimePeriod().get(), aggregatedIntervalReadingRecord.getValue());
    }

    private IntervalReadingRecord mockIntervalReadingRecord(Range<Instant> interval, BigDecimal value) {
        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        when(intervalReadingRecord.getTimePeriod()).thenReturn(Optional.of(interval));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(intervalReadingRecord.getValue()).thenReturn(value);
        when(intervalReadingRecord.getReadingType()).thenReturn(regularReadingType);
        when(regularReadingType.getIntervalLength()).thenReturn(Optional.empty());
        return intervalReadingRecord;
    }

    private DataValidationStatus mockValidationStatus(Instant timeStamp, ValidationRule validationRule) {
        DataValidationStatus validationStatus = mock(DataValidationStatus.class);
        ReadingQualityType qualityType = new ReadingQualityType("3.5.258");
        ReadingQualityRecord quality = mock(ReadingQualityRecord.class);
        when(quality.getType()).thenReturn(qualityType);
        doReturn(Collections.singletonList(quality)).when(validationStatus).getReadingQualities();
        when(validationStatus.getReadingTimestamp()).thenReturn(timeStamp);
        when(validationStatus.completelyValidated()).thenReturn(true);
        when(validationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        when(validationStatus.getOffendedRules()).thenReturn(Collections.singletonList(validationRule));
        return validationStatus;
    }

    private DataValidationStatus mockEstimationStatus(Instant timeStamp, EstimationRule estimationRule) {
        DataValidationStatus validationStatus = mock(DataValidationStatus.class);
        ReadingQualityType qualityType = new ReadingQualityType("3.8.4");
        ReadingQualityRecord quality = mock(ReadingQualityRecord.class);
        when(quality.hasEstimatedCategory()).thenReturn(true);
        when(quality.getType()).thenReturn(qualityType);
        when(quality.getId()).thenReturn(555L);
        when(quality.getComment()).thenReturn("Estimated by market rule 11");
        doReturn(Optional.of(estimationRule)).when(estimationService).findEstimationRuleByQualityType(qualityType);
        doReturn(Collections.singletonList(quality)).when(validationStatus).getReadingQualities();
        when(validationStatus.getReadingTimestamp()).thenReturn(timeStamp);
        when(validationStatus.completelyValidated()).thenReturn(true);
        when(validationStatus.getValidationResult()).thenReturn(ValidationResult.VALID);
        return validationStatus;
    }
}
