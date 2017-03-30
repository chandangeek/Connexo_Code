/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointResourceChannelDataTest extends AbstractUsagePointResourceChannelDataTest {

    private static final long CHANNEL_ID = 13L;


    @Before
    public void before() {
        beforeSetup();
        when(aggregatedChannel.getId()).thenReturn(CHANNEL_ID);
        when(upChannelsContainer.getChannels()).thenReturn(Arrays.asList(aggregatedChannel, aggregatedChannel));
    }

    @Test
    public void getChannelDataNoSuchUsagePoint() throws Exception {
        // Business method
        Response response = target("/usagepoints/xxx/channels/13/data").queryParam("filter", buildFilter())
                .request()
                .get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("No usage point with name xxx.");
    }

    @Test
    public void getChannelDataMissingIntervalStart() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalEnd", timeStamp.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/UP0001/channels/13/data").queryParam("filter", filter)
                .request()
                .get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void getChannelDataMissingIntervalEnd() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalStart", timeStamp.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/UP0001/channels/13/data").queryParam("filter", filter)
                .request()
                .get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void getChannelDataNoEffectiveMetrologyConfiguration() throws Exception {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", buildFilter())
                .request()
                .get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Usage point UP0001 doesn't have a link to metrology configuration.");
    }

    @Test
    public void getChannelDataNoSuchChannel() throws Exception {
        // Business method
        Response response = target("/usagepoints/UP0001/channels/100/data").queryParam("filter", buildFilter())
                .request()
                .get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Usage point UP0001 doesn't have channel with id 100.");
    }

    @Test
    public void getChannelDataNoUsagePointMeterActivations() throws Exception {
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());

        //Business method
        String json = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", buildFilter())
                .request()
                .get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.data")).hasSize(0);
    }

    @Test
    public void getChannelDataRequestedIntervalDoesNotOverlapWithUsagePointMeterActivations() throws Exception {
        List<MeterActivation> meterActivations = Arrays.asList(
                mockMeterActivationWithRange(timeStamp.minus(4, ChronoUnit.DAYS), timeStamp.minus(3, ChronoUnit.DAYS)),
                mockMeterActivationWithRange(timeStamp.minus(2, ChronoUnit.DAYS), timeStamp.minus(1, ChronoUnit.DAYS)));
        when(usagePoint.getMeterActivations()).thenReturn(meterActivations);

        //Business method
        String json = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", buildFilter())
                .request()
                .get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.data")).hasSize(0);
    }

    @Test
    public void getChannelDataNotValidated() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint());
        when(aggregatedChannel.toList(interval)).thenReturn(Arrays.asList(
                interval_1.upperEndpoint(),
                interval_2.upperEndpoint(),
                interval_3.upperEndpoint()
        ));
        List<IntervalReadingRecord> readings = Arrays.asList(
                mockIntervalReadingRecord(interval_1, BigDecimal.ONE),
                mockIntervalReadingRecord(interval_3, BigDecimal.TEN)
        );
        when(aggregatedChannel.getIntervalReadings(interval)).thenReturn(readings);

        //Business method
        String json = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<Number>get("$.data[0].interval.start")).isEqualTo(interval_3.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].interval.end")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].readingTime")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].value")).isEqualTo(BigDecimal.TEN.toString());
        assertThat(jsonModel.<Boolean>get("$.data[0].dataValidated")).isFalse();

        assertThat(jsonModel.<Number>get("$.data[1].interval.start")).isEqualTo(interval_2.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].interval.end")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].readingTime")).isEqualTo(interval_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Boolean>get("$.data[1].dataValidated")).isFalse();

        assertThat(jsonModel.<Number>get("$.data[2].interval.start")).isEqualTo(interval_1.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].interval.end")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].readingTime")).isEqualTo(interval_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].value")).isEqualTo(BigDecimal.ONE.toString());
        assertThat(jsonModel.<Boolean>get("$.data[2].dataValidated")).isFalse();
    }

    @Test
    public void getChannelDataValidated() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint());
        when(aggregatedChannel.toList(interval)).thenReturn(Arrays.asList(
                interval_1.upperEndpoint(),
                interval_2.upperEndpoint(),
                interval_3.upperEndpoint()
        ));
        List<IntervalReadingRecord> readings = Arrays.asList(
                mockIntervalReadingRecord(interval_1, BigDecimal.ONE),
                mockIntervalReadingRecord(interval_3, BigDecimal.TEN)
        );
        when(aggregatedChannel.getIntervalReadings(interval)).thenReturn(readings);
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(interval_3.upperEndpoint()));

        //Business method
        String json = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<Number>get("$.data[0].interval.start")).isEqualTo(interval_3.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].interval.end")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].readingTime")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].value")).isEqualTo(BigDecimal.TEN.toString());
        assertThat(jsonModel.<Boolean>get("$.data[0].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[1].interval.start")).isEqualTo(interval_2.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].interval.end")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].readingTime")).isEqualTo(interval_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Boolean>get("$.data[1].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[1].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[2].interval.start")).isEqualTo(interval_1.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].interval.end")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].readingTime")).isEqualTo(interval_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].value")).isEqualTo(BigDecimal.ONE.toString());
        assertThat(jsonModel.<Boolean>get("$.data[2].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[2].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());
    }

    @Test
    public void getChannelDataSuspectAndNotValidated() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint());
        when(aggregatedChannel.toList(interval)).thenReturn(Arrays.asList(
                interval_1.upperEndpoint(),
                interval_2.upperEndpoint(),
                interval_3.upperEndpoint()
        ));
        IntervalReadingRecord suspectReading = mockIntervalReadingRecord(interval_1, BigDecimal.ONE);
        ReadingQualityRecord suspectAggregatedQuality = mock(ReadingQualityRecord.class);
        when(suspectAggregatedQuality.isSuspect()).thenReturn(true);
        doReturn(Collections.singletonList(suspectAggregatedQuality)).when(suspectReading).getReadingQualities();
        List<IntervalReadingRecord> readings = Arrays.asList(
                suspectReading,
                mockIntervalReadingRecord(interval_3, BigDecimal.TEN)
        );
        when(aggregatedChannel.getIntervalReadings(interval)).thenReturn(readings);
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(interval_2.upperEndpoint()));

        //Business method
        String json = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<Number>get("$.data[0].interval.start")).isEqualTo(interval_3.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].interval.end")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].readingTime")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].value")).isEqualTo(BigDecimal.TEN.toString());
        assertThat(jsonModel.<Boolean>get("$.data[0].dataValidated")).isFalse();
        assertThat(jsonModel.<String>get("$.data[0].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[1].interval.start")).isEqualTo(interval_2.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].interval.end")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].readingTime")).isEqualTo(interval_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Boolean>get("$.data[1].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[1].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[2].interval.start")).isEqualTo(interval_1.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].interval.end")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].readingTime")).isEqualTo(interval_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].value")).isEqualTo(BigDecimal.ONE.toString());
        assertThat(jsonModel.<Boolean>get("$.data[2].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[2].validationResult")).isEqualTo(ValidationStatus.SUSPECT.getNameKey());
    }

    @Test
    public void getChannelDataAggregatedFromTwoChannelsOfTwoDevices() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_5.upperEndpoint());
        when(aggregatedChannel.toList(interval)).thenReturn(Arrays.asList(
                interval_1.upperEndpoint(),
                interval_2.upperEndpoint(),
                interval_3.upperEndpoint(),
                interval_4.upperEndpoint(),
                interval_5.upperEndpoint()
        ));
        List<IntervalReadingRecord> readings = Arrays.asList(
                mockIntervalReadingRecord(interval_1, BigDecimal.ONE),
                mockIntervalReadingRecord(interval_3, BigDecimal.TEN),
                mockIntervalReadingRecord(interval_5, BigDecimal.ZERO)
        );
        when(aggregatedChannel.getIntervalReadings(interval)).thenReturn(readings);
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(interval_2.upperEndpoint()));

        String filter = ExtjsFilter.filter()
                .property("intervalStart", timeStamp.toEpochMilli())
                .property("intervalEnd", timeStamp.plus(75, ChronoUnit.MINUTES).toEpochMilli())
                .create();

        //Business method
        String json = target("/usagepoints/UP0001/channels/13/data").queryParam("filter", filter)
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(5);
        assertThat(jsonModel.<Number>get("$.data[0].interval.start")).isEqualTo(interval_5.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].interval.end")).isEqualTo(interval_5.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].readingTime")).isEqualTo(interval_5.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].value")).isEqualTo(BigDecimal.ZERO.toString());
        assertThat(jsonModel.<Boolean>get("$.data[0].dataValidated")).isFalse();
        assertThat(jsonModel.<String>get("$.data[0].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[1].interval.start")).isEqualTo(interval_4.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].interval.end")).isEqualTo(interval_4.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].readingTime")).isNull();// No linked devices
        assertThat(jsonModel.<Boolean>get("$.data[1].dataValidated")).isFalse();
        assertThat(jsonModel.<String>get("$.data[1].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[2].interval.start")).isEqualTo(interval_3.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].interval.end")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].readingTime")).isEqualTo(interval_3.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].value")).isEqualTo(BigDecimal.TEN.toString());
        assertThat(jsonModel.<Boolean>get("$.data[2].dataValidated")).isFalse();
        assertThat(jsonModel.<String>get("$.data[2].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[3].interval.start")).isEqualTo(interval_2.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[3].interval.end")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[3].readingTime")).isEqualTo(interval_2.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Boolean>get("$.data[3].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[3].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());

        assertThat(jsonModel.<Number>get("$.data[4].interval.start")).isEqualTo(interval_1.lowerEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[4].interval.end")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[4].readingTime")).isEqualTo(interval_1.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[4].value")).isEqualTo(BigDecimal.ONE.toString());
        assertThat(jsonModel.<Boolean>get("$.data[4].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[4].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());
    }


    private IntervalReadingRecord mockIntervalReadingRecord(Range<Instant> interval, BigDecimal value) {
        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        when(intervalReadingRecord.getTimePeriod()).thenReturn(Optional.of(interval));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(intervalReadingRecord.getValue()).thenReturn(value);
        return intervalReadingRecord;
    }


}
