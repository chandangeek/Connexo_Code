/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.Ranges;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test to verify register data provided by usage point resource<br>
 * NOTE: test is based on the same mocks as test for channels with minor changes
 */
public class UsagePointResourceRegisterDataTest extends AbstractUsagePointResourceChannelDataTest {

    private static final long REGISTER_ID = 14L;

    @Mock
    private Channel register;

    @Before
    public void before() {
        beforeSetup();

        when(register.getId()).thenReturn(REGISTER_ID);
        when(register.isRegular()).thenReturn(false);
        when(register.getMainReadingType()).thenReturn(readingType);

        when(upChannelsContainer.getChannels()).thenReturn(Arrays.asList(aggregatedChannel, register));
    }

    @Test
    public void getRegisterDataNoSuchUsagePoint() throws Exception {
        // Business method
        Response response = target("/usagepoints/xxx/registers/14/data").queryParam("filter", buildFilter()).request()
                .get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("No usage point with name xxx.");
    }

    @Test
    public void getRegisterDataMissingIntervalStart() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalEnd", timeStamp.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/UP0001/registers/14/data").queryParam("filter", filter).request().get(String
                .class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }


    @Test
    public void getRegisterDataMissingIntervalEnd() throws Exception {
        String filter = ExtjsFilter.filter().property("intervalStart", timeStamp.toEpochMilli()).create();

        // Business method
        String json = target("usagepoints/UP0001/registers/14/data").queryParam("filter", filter)
                .request()
                .get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void getRegisterDataNoEffectiveMetrologyConfiguration() throws Exception {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", buildFilter())
                .request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Usage point UP0001 doesn't have a link to metrology configuration.");
    }

    @Test
    public void getRegisterDataNoSuchRegister() throws Exception {
        // Business method
        Response response = target("/usagepoints/UP0001/registers/100/data").queryParam("filter", buildFilter())
                .request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Usage point UP0001 doesn't have register with id 100.");
    }


    @Test
    public void getRegisterDataNoUsagePointMeterActivations() throws Exception {
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());

        //Business method
        String json = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", buildFilter()).request().get
                (String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.data")).hasSize(0);
    }


    @Test
    public void getRegisterDataRequestedIntervalDoesNotOverlapWithUsagePointMeterActivations() throws Exception {
        List<MeterActivation> meterActivations = Arrays.asList(
                mockMeterActivationWithRange(timeStamp.minus(4, ChronoUnit.DAYS), timeStamp.minus(3, ChronoUnit.DAYS)),
                mockMeterActivationWithRange(timeStamp.minus(2, ChronoUnit.DAYS), timeStamp.minus(1, ChronoUnit.DAYS)));
        when(usagePoint.getMeterActivations()).thenReturn(meterActivations);

        //Business method
        String json = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.data")).hasSize(0);
    }


    @Test
    public void getRegisterDataNotValidated() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint());
        List<ReadingRecord> readings = Arrays.asList(
                mockReadingRecord(interval_1.upperEndpoint(), BigDecimal.ONE),
                mockReadingRecord(interval_3.upperEndpoint(), BigDecimal.TEN)
        );
        when(register.getRegisterReadings(interval)).thenReturn(readings);

        //Business method
        String json = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
    }


    @Test
    public void getRegisterDataValidated() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint());
        List<ReadingRecord> readings = Arrays.asList(
                mockReadingRecord(interval_1.upperEndpoint(), BigDecimal.ONE),
                mockReadingRecord(interval_3.upperEndpoint(), BigDecimal.TEN)
        );
        when(register.getRegisterReadings(interval)).thenReturn(readings);
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(interval_3.upperEndpoint()));

        //Business method
        String json = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
    }


    @Test
    public void getRegisterDataSuspectAndNotValidated() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_3.upperEndpoint());
        ReadingRecord suspectReading = mockReadingRecord(interval_1.upperEndpoint(), BigDecimal.ONE);
        ReadingQualityRecord suspectAggregatedQuality = mock(ReadingQualityRecord.class);
        when(suspectAggregatedQuality.isSuspect()).thenReturn(true);

        ReadingQualityType readingQualityType = new ReadingQualityType("11111");

        when(suspectAggregatedQuality.getType()).thenReturn(readingQualityType);
        doReturn(Collections.singletonList(suspectAggregatedQuality)).when(suspectReading).getReadingQualities();
        List<ReadingRecord> readings = Arrays.asList(
                suspectReading,
                mockReadingRecord(interval_2.upperEndpoint(), BigDecimal.valueOf(5)),
                mockReadingRecord(interval_3.upperEndpoint(), BigDecimal.TEN)
        );
        when(register.getRegisterReadings(interval)).thenReturn(readings);
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(interval_2.upperEndpoint()));

        //Business method
        String json = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", buildFilter())
                .request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<Boolean>get("$.data[0].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[0].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[0].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[0].collectedValue")).isEqualTo(BigDecimal.TEN.intValue());
        assertThat(jsonModel.<Number>get("$.data[0].measurementPeriod.start")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].measurementPeriod.end")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].deltaValue")).isEqualTo(BigDecimal.TEN.subtract(BigDecimal.valueOf(5))
                .intValue());
        assertThat(jsonModel.<String>get("$.data[0].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());
        assertThat(jsonModel.<List>get("$.data[0].readingQualities")).hasSize(0);

        assertThat(jsonModel.<Boolean>get("$.data[1].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[1].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[1].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[1].collectedValue")).isEqualTo(BigDecimal.valueOf(5).intValue());
        assertThat(jsonModel.<Number>get("$.data[1].measurementPeriod.start")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].measurementPeriod.end")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].deltaValue")).isEqualTo(BigDecimal.valueOf(5)
                .subtract(BigDecimal.ONE)
                .intValue());
        assertThat(jsonModel.<String>get("$.data[1].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());
        assertThat(jsonModel.<List>get("$.data[1].readingQualities")).hasSize(0);


        assertThat(jsonModel.<Boolean>get("$.data[2].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[2].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[2].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[2].collectedValue")).isEqualTo(BigDecimal.ONE.intValue());
        assertThat(jsonModel.<Number>get("$.data[2].measurementPeriod.start")).isNull();
        assertThat(jsonModel.<Number>get("$.data[2].measurementPeriod.end")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].deltaValue")).isNull();
        assertThat(jsonModel.<String>get("$.data[2].validationResult")).isEqualTo(ValidationStatus.SUSPECT.getNameKey());
        assertThat(jsonModel.<List>get("$.data[2].readingQualities")).hasSize(0);
    }


    @Test
    public void getRegisterDataAggregatedFromTwoChannelsOfTwoDevices() throws Exception {
        Range<Instant> interval = Ranges.openClosed(interval_1.lowerEndpoint(), interval_5.upperEndpoint());
        List<ReadingRecord> readings = Arrays.asList(
                mockReadingRecord(interval_1.upperEndpoint(), BigDecimal.valueOf(1)),
                mockReadingRecord(interval_2.upperEndpoint(), BigDecimal.valueOf(10)),
                mockReadingRecord(interval_3.upperEndpoint(), BigDecimal.valueOf(30)),
                mockReadingRecord(interval_4.upperEndpoint(), BigDecimal.valueOf(60)),
                mockReadingRecord(interval_5.upperEndpoint(), BigDecimal.valueOf(100))
        );
        when(register.getRegisterReadings(interval)).thenReturn(readings);
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(interval_2.upperEndpoint()));

        String filter = ExtjsFilter.filter()
                .property("intervalStart", timeStamp.toEpochMilli())
                .property("intervalEnd", timeStamp.plus(75, ChronoUnit.MINUTES).toEpochMilli())
                .create();

        //Business method
        String json = target("/usagepoints/UP0001/registers/14/data").queryParam("filter", filter)
                .request().get(String.class);


        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(5);

        assertThat(jsonModel.<Boolean>get("$.data[0].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[0].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[0].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[0].collectedValue")).isEqualTo(BigDecimal.valueOf(100).intValue());
        assertThat(jsonModel.<Number>get("$.data[0].measurementPeriod.start")).isEqualTo(interval_4.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].measurementPeriod.end")).isEqualTo(interval_5.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[0].deltaValue")).isEqualTo(BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(60))
                .intValue());
        assertThat(jsonModel.<String>get("$.data[0].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());
        assertThat(jsonModel.<List>get("$.data[0].readingQualities")).hasSize(0);

        assertThat(jsonModel.<Boolean>get("$.data[1].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[1].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[1].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[1].collectedValue")).isEqualTo(BigDecimal.valueOf(60).intValue());
        assertThat(jsonModel.<Number>get("$.data[1].measurementPeriod.start")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].measurementPeriod.end")).isEqualTo(interval_4.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[1].deltaValue")).isEqualTo(BigDecimal.valueOf(60)
                .subtract(BigDecimal.valueOf(30))
                .intValue());
        assertThat(jsonModel.<String>get("$.data[1].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());
        assertThat(jsonModel.<List>get("$.data[1].readingQualities")).hasSize(0);

        assertThat(jsonModel.<Boolean>get("$.data[2].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[2].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[2].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[2].collectedValue")).isEqualTo(BigDecimal.valueOf(30).intValue());
        assertThat(jsonModel.<Number>get("$.data[2].measurementPeriod.start")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].measurementPeriod.end")).isEqualTo(interval_3.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[2].deltaValue")).isEqualTo(BigDecimal.valueOf(30)
                .subtract(BigDecimal.valueOf(10))
                .intValue());
        assertThat(jsonModel.<String>get("$.data[2].validationResult")).isEqualTo(ValidationStatus.NOT_VALIDATED.getNameKey());
        assertThat(jsonModel.<List>get("$.data[2].readingQualities")).hasSize(0);

        assertThat(jsonModel.<Boolean>get("$.data[3].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[3].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[3].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[3].collectedValue")).isEqualTo(BigDecimal.valueOf(10).intValue());
        assertThat(jsonModel.<Number>get("$.data[3].measurementPeriod.start")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[3].measurementPeriod.end")).isEqualTo(interval_2.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[3].deltaValue")).isEqualTo(BigDecimal.valueOf(10)
                .subtract(BigDecimal.valueOf(1))
                .intValue());
        assertThat(jsonModel.<String>get("$.data[3].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());
        assertThat(jsonModel.<List>get("$.data[3].readingQualities")).hasSize(0);

        assertThat(jsonModel.<Boolean>get("$.data[4].isCumulative")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.data[4].hasEvent")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.data[4].isBilling")).isFalse();
        assertThat(jsonModel.<Number>get("$.data[4].collectedValue")).isEqualTo(BigDecimal.valueOf(1).intValue());
        assertThat(jsonModel.<Number>get("$.data[4].measurementPeriod.start")).isNull();
        assertThat(jsonModel.<Number>get("$.data[4].measurementPeriod.end")).isEqualTo(interval_1.upperEndpoint()
                .toEpochMilli());
        assertThat(jsonModel.<Number>get("$.data[4].deltaValue")).isNull();
        assertThat(jsonModel.<String>get("$.data[4].validationResult")).isEqualTo(ValidationStatus.OK.getNameKey());
        assertThat(jsonModel.<List>get("$.data[4].readingQualities")).hasSize(0);
    }

    private ReadingRecord mockReadingRecord(Instant time, BigDecimal value) {
        ReadingRecord readingRecord = mock(ReadingRecord.class);
        when(readingRecord.getTimeStamp()).thenReturn(time);
        when(readingRecord.getValue()).thenReturn(value);
        when(readingRecord.getReadingType()).thenReturn(readingType);
        when(readingRecord.getTimePeriod()).thenReturn(Optional.empty());
        return readingRecord;
    }
}
