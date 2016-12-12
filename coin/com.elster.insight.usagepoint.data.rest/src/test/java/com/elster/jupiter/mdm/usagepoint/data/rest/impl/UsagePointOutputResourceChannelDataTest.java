package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceChannelDataTest extends UsagePointDataRestApplicationJerseyTest {
    private static final String USAGE_POINT_NAME = "Le nom";
    private static final Instant timeStamp = Instant.ofEpochMilli(1410774620100L);

    private static final Range<Instant> interval_1 = Range.openClosed(timeStamp.plus(0, ChronoUnit.MINUTES), timeStamp.plus(15, ChronoUnit.MINUTES));
    private static final Range<Instant> interval_2 = Range.openClosed(timeStamp.plus(15, ChronoUnit.MINUTES), timeStamp.plus(30, ChronoUnit.MINUTES));
    private static final Range<Instant> interval_3 = Range.openClosed(timeStamp.plus(30, ChronoUnit.MINUTES), timeStamp.plus(45, ChronoUnit.MINUTES));

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingTypeInfoFactory readingTypeInfoFactory;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;

    @Before
    public void before() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));

        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfigurationWithContract(1, "mc");

        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer));

        when(usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getRange()).thenReturn(Range.atLeast(interval_1.lowerEndpoint()));

        when(validationService.getLastChecked(any(Channel.class))).thenReturn(Optional.of(timeStamp));
        Estimator estimator = mock(Estimator.class);
        EstimationResult estimationResult = mock(EstimationResult.class);
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        Estimatable estimatable = mock(Estimatable.class);


        when(estimator.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(estimator.estimate(anyListOf(EstimationBlock.class), any(QualityCodeSystem.class))).thenReturn(estimationResult);
        when(estimationResult.estimated()).thenReturn(Collections.singletonList(estimationBlock));
        doReturn(Collections.singletonList(estimatable)).when(estimationBlock).estimatables();
        when(estimatable.getTimestamp()).thenReturn(interval_3.upperEndpoint());
        when(estimatable.getEstimation()).thenReturn(BigDecimal.valueOf(327L));
        when(estimationService.getEstimator("com.elster.jupiter.estimators.impl.ValueFillEstimator")).thenReturn(Optional.of(estimator));
        when(estimationService.getEstimator(eq("com.elster.jupiter.estimators.impl.ValueFillEstimator"), any())).thenReturn(Optional.of(estimator));
        when(estimationService.previewEstimate(any(QualityCodeSystem.class), eq(channelsContainer), any(), any(), eq(estimator))).thenReturn(estimationResult);
    }

    private String buildFilter() throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", interval_1.lowerEndpoint().toEpochMilli())
                .property("intervalEnd", interval_3.upperEndpoint().toEpochMilli())
                .create();
    }

    @Test
    public void testGetChannelDataNoSuchUsagePoint() throws Exception {
        // Business method
        Response response = target("/usagepoints/xxx/purposes/100/outputs/1/channelData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataNoMetrologyConfigurationOnUsagePoint() throws Exception {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataNoSuchContract() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/90030004443343/outputs/1/channelData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataMissingIntervalStart() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalEnd", timeStamp.toEpochMilli()).create();

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
        String filter = ExtjsFilter.filter().property("intervalStart", timeStamp.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.channelData")).isEmpty();
    }

    @Test
    public void testGetChannelDataOnIrregularReadingTypeDeliverable() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/2/channelData")
                .queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetChannelDataRequestedIntervalDoesNotContainData() throws Exception {
        String filter = ExtjsFilter.filter()
                .property("intervalStart", timeStamp.minus(15, ChronoUnit.MINUTES).toEpochMilli())
                .property("intervalEnd", timeStamp.minus(10, ChronoUnit.MINUTES).toEpochMilli())
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
    public void testGetChannelData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(channel.toList(Range.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_1.upperEndpoint(), interval_2.upperEndpoint(), interval_3.upperEndpoint())
        );
        mockIntervalReadingsWithValidationResult(channel);

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .queryParam("filter", buildFilter()).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);

        assertThat(jsonModel.<Long>get("$.channelData[0].interval.start")).isEqualTo(interval_3.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.end")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[0].readingTime")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[0].value")).isEqualTo("10");
        assertThat(jsonModel.<Boolean>get("$.channelData[0].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[0].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[0].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[0].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.channelData[0].validationRules[0].name")).isEqualTo("MinMax");

        assertThat(jsonModel.<Long>get("$.channelData[1].interval.start")).isEqualTo(interval_2.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[1].interval.end")).isEqualTo(interval_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[1].readingTime")).isEqualTo(interval_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Boolean>get("$.channelData[1].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[1].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[1].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[1].validationRules[0].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.channelData[1].validationRules[0].name")).isEqualTo("Missing");

        assertThat(jsonModel.<Long>get("$.channelData[2].interval.start")).isEqualTo(interval_1.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[2].interval.end")).isEqualTo(interval_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Long>get("$.channelData[2].readingTime")).isEqualTo(interval_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<String>get("$.channelData[2].value")).isEqualTo("1");
        assertThat(jsonModel.<Boolean>get("$.channelData[2].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[2].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[2].action")).isEqualTo("FAIL");
        assertThat(jsonModel.<Number>get("$.channelData[2].validationRules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.channelData[2].validationRules[0].name")).isEqualTo("MinMax");
    }

    @Test
    public void testGetChannelDataOfMonthlyChannel() throws UnsupportedEncodingException {
        ZonedDateTime time = ZonedDateTime.of(LocalDateTime.of(2016, 5, 1, 0, 0), ZoneId.systemDefault());
        Range<Instant> interval_JUN = Range.openClosed(time.toInstant(), time.with(Month.JUNE).toInstant());
        Range<Instant> interval_JUL = Range.openClosed(time.with(Month.JUNE).toInstant(), time.with(Month.JULY).toInstant());
        Range<Instant> interval_AUG = Range.openClosed(time.with(Month.JULY).toInstant(), time.with(Month.AUGUST).toInstant());
        Range<Instant> interval_SEP = Range.openClosed(time.with(Month.AUGUST).toInstant(), time.with(Month.SEPTEMBER).toInstant());
        AggregatedChannel channel = mock(AggregatedChannel.class);
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(channel.getIntervalLength()).thenReturn(Optional.of(Period.ofMonths(1)));
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channel.toList(Range.openClosed(time.toInstant(), interval_AUG.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_JUN.upperEndpoint(), interval_JUL.upperEndpoint(), interval_AUG.upperEndpoint())
        );
        IntervalReadingRecord intervalReadingRecord1 = mockIntervalReadingRecord(interval_JUN, BigDecimal.ONE);
        IntervalReadingRecord intervalReadingRecord2 = mockIntervalReadingRecord(interval_JUL, BigDecimal.TEN);
        IntervalReadingRecord intervalReadingRecord3 = mockIntervalReadingRecord(interval_AUG, BigDecimal.ONE);
        IntervalReadingRecord intervalReadingRecord4 = mockIntervalReadingRecord(interval_SEP, BigDecimal.ONE);
        List<IntervalReadingRecord> intervalReadings = Arrays.asList(
                intervalReadingRecord1, intervalReadingRecord2, intervalReadingRecord3, intervalReadingRecord4);
        when(channel.getIntervalReadings(any())).thenReturn(intervalReadings); //Intentionally returns more then three
        when(channel.getCalculatedIntervalReadings(any())).thenReturn(intervalReadings);
        when(channel.getPersistedIntervalReadings(any())).thenReturn(Collections.emptyList());
        ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());

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
    public void testPreviewEstimateChannelData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(channel.toList(Range.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_1.upperEndpoint(), interval_2.upperEndpoint(), interval_3.upperEndpoint())
        );
        mockIntervalReadingsWithValidationResult(channel);

        EstimateChannelDataInfo info = new EstimateChannelDataInfo();
        info.intervals = Collections.singletonList(IntervalInfo.from(interval_3));
        info.estimatorImpl = "com.elster.jupiter.estimators.impl.ValueFillEstimator";
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("valuefill.maxNumberOfConsecutiveSuspects", "Max number of consecutive suspects", new PropertyValueInfo<>(123L, null, 10L, true), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));
        info.properties.add(new PropertyInfo("valuefill.fillValue", "Fill value", new PropertyValueInfo<>(123L, null, 10L, true), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData/estimate")
                .request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Number>get("$[0].readingTime")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$[0].value")).isEqualTo("327");
    }

    @Test
    public void testEditChannelData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(channel.toList(Range.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_1.upperEndpoint(), interval_2.upperEndpoint(), interval_3.upperEndpoint())
        );
        mockIntervalReadingsWithValidationResult(channel);

        OutputChannelDataInfo info = new OutputChannelDataInfo();
        info.value = BigDecimal.valueOf(101L);
        info.readingTime = interval_3.upperEndpoint();
        info.interval = IntervalInfo.from(interval_3);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .request().put(Entity.json(Collections.singletonList(info)));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel).editReadings(eq(QualityCodeSystem.MDM),anyListOf(IntervalReadingImpl.class));
    }

    @Test
    public void testConfirmChannelData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(channel.toList(Range.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_1.upperEndpoint(), interval_2.upperEndpoint(), interval_3.upperEndpoint())
        );
        mockIntervalReadingsWithValidationResult(channel);

        OutputChannelDataInfo info = new OutputChannelDataInfo();
        info.isConfirmed = true;
        info.readingTime = interval_3.upperEndpoint();
        info.interval = IntervalInfo.from(interval_3);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .request().put(Entity.json(Collections.singletonList(info)));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(channel).confirmReadings(eq(QualityCodeSystem.MDM),anyListOf(IntervalReadingImpl.class));
    }

    @Test
    public void testRemoveChannelData() throws Exception {
        AggregatedChannel channel = mock(AggregatedChannel.class);
        ReadingQualityFetcher fetcher = mock(ReadingQualityFetcher.class);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.atTimestamp(any(Instant.class))).thenReturn(fetcher);
        when(fetcher.sorted()).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(Collections.emptyList());
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(effectiveMC.getAggregatedChannel(any(), any())).thenReturn(Optional.of(channel));
        when(channel.toList(Range.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint()))).thenReturn(
                Arrays.asList(interval_1.upperEndpoint(), interval_2.upperEndpoint(), interval_3.upperEndpoint())
        );
        mockIntervalReadingsWithValidationResult(channel);

        OutputChannelDataInfo info = new OutputChannelDataInfo();
        info.value = null;
        info.readingTime = interval_3.upperEndpoint();
        info.interval = IntervalInfo.from(interval_3);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1/channelData")
                .request().put(Entity.json(Collections.singletonList(info)));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        List<BaseReadingRecord> record = Collections.singletonList(channel.getReading(interval_3.upperEndpoint()).get());
        verify(channel).removeReadings(eq(QualityCodeSystem.MDM),eq(record));
        verify(validationService).updateLastChecked(eq(channel), any(Instant.class));
    }

    @Test
    public void testReadingValidationInfoForMissedReadingInTheMiddleOfValidatedData() {
        OutputChannelDataInfoFactory factory = new OutputChannelDataInfoFactory(new ValidationRuleInfoFactory(propertyValueInfoService, readingTypeInfoFactory));
        ReadingWithValidationStatus status = mock(ReadingWithValidationStatus.class);
        when(status.getTimeStamp()).thenReturn(timeStamp.minus(1, ChronoUnit.DAYS));
        when(status.getTimePeriod()).thenReturn(Optional.of(Range.closedOpen(timeStamp.minus(1, ChronoUnit.DAYS), timeStamp)));
        when(status.getValidationStatus()).thenReturn(Optional.empty());
        when(status.getChannelLastChecked()).thenReturn(Optional.of(timeStamp));
        when(status.isChannelValidationActive()).thenReturn(true);
        when(status.getPersistedValue()).thenReturn(Optional.empty());
        when(status.getCalculatedValue()).thenReturn(Optional.empty());
        when(status.getReadingModificationFlag()).thenReturn(Optional.empty());
        OutputChannelDataInfo info = factory.createChannelDataInfo(status);
        assertThat(info.dataValidated).isTrue();
        assertThat(info.validationResult).isEqualTo(ValidationStatus.OK);
    }

    @Test
    public void testReadingValidationInfoForMissedReadingAfterLastCheckedDate() {
        OutputChannelDataInfoFactory factory = new OutputChannelDataInfoFactory(new ValidationRuleInfoFactory(propertyValueInfoService, readingTypeInfoFactory));
        ReadingWithValidationStatus status = mock(ReadingWithValidationStatus.class);
        Instant dayAfter = timeStamp.plus(1, ChronoUnit.DAYS);
        when(status.getTimeStamp()).thenReturn(dayAfter);
        when(status.getTimePeriod()).thenReturn(Optional.of(Range.closedOpen(dayAfter, timeStamp.plus(2, ChronoUnit.DAYS))));
        when(status.getValidationStatus()).thenReturn(Optional.empty());
        when(status.getChannelLastChecked()).thenReturn(Optional.of(timeStamp));
        when(status.isChannelValidationActive()).thenReturn(true);
        when(status.getPersistedValue()).thenReturn(Optional.empty());
        when(status.getCalculatedValue()).thenReturn(Optional.empty());
        when(status.getReadingModificationFlag()).thenReturn(Optional.empty());
        OutputChannelDataInfo info = factory.createChannelDataInfo(status);
        assertThat(info.dataValidated).isFalse();
        assertThat(info.validationResult).isEqualTo(ValidationStatus.NOT_VALIDATED);
    }

    private void mockIntervalReadingsWithValidationResult(AggregatedChannel channel) {
        ValidationRule minMax = mockValidationRule(1, "MinMax");
        ValidationRule missing = mockValidationRule(2, "Missing");

        IntervalReadingRecord intervalReadingRecord1 = mockIntervalReadingRecord(interval_1, BigDecimal.ONE);
        DataValidationStatus dataValidationStatus_1 = mockValidationStatus(interval_1.upperEndpoint(), minMax);
        // 2nd intentionally missing
        DataValidationStatus dataValidationStatus_2 = mockValidationStatus(interval_2.upperEndpoint(), missing);
        IntervalReadingRecord intervalReadingRecord3 = mockIntervalReadingRecord(interval_3, BigDecimal.TEN);
        DataValidationStatus dataValidationStatus_3 = mockValidationStatus(interval_3.upperEndpoint(), minMax);
        DataValidationStatus dataValidationStatus_4 = mockValidationStatus(timeStamp.plus(45, ChronoUnit.MINUTES), minMax);
        //intentionally added one more status which is out of requested interval

        List<IntervalReadingRecord> intervalReadings = Arrays.asList(intervalReadingRecord1, intervalReadingRecord3);
        when(channel.getIntervalReadings(any())).thenReturn(intervalReadings);
        when(channel.getCalculatedIntervalReadings(any())).thenReturn(intervalReadings);
        when(channel.getPersistedIntervalReadings(any())).thenReturn(Collections.emptyList());
        when(channel.getReading(interval_1.upperEndpoint())).thenReturn(Optional.of(intervalReadingRecord1));
        when(channel.getReading(interval_3.upperEndpoint())).thenReturn(Optional.of(intervalReadingRecord3));

        ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel, intervalReadings,
                Range.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint())))
                .thenReturn(Arrays.asList(dataValidationStatus_1, dataValidationStatus_2, dataValidationStatus_3, dataValidationStatus_4));
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

    private IntervalReadingRecord mockIntervalReadingRecord(Range<Instant> interval, BigDecimal value) {
        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        when(intervalReadingRecord.getTimePeriod()).thenReturn(Optional.of(interval));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(intervalReadingRecord.getValue()).thenReturn(value);
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
}
