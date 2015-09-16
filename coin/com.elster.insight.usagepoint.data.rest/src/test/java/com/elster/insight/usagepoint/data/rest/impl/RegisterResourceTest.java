package com.elster.insight.usagepoint.data.rest.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

public class RegisterResourceTest extends UsagePointDataRestApplicationJerseyTest {

    public static final String REGISTER_MRID1 = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    public static final Instant LAST_READING = Instant.ofEpochMilli(1410786196000L);
    private static long intervalStart = 1410774630000L;
    private static long intervalEnd = 1410828630000L;

    @Mock
    private Meter meter;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Channel channel;
    @Mock
    private BaseReadingRecord lastReading;
    @Mock
    private ReadingRecord readingRecord, readingRecord2, readingRecord3, readingRecord4;

    public RegisterResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(meteringService.findMeter("1")).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePoint("1")).thenReturn(Optional.of(usagePoint));

        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel));
        ReadingType readingType = mockReadingType(REGISTER_MRID1);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(meteringService.getReadingType(REGISTER_MRID1)).thenReturn(Optional.of(readingType));

        when(channel.getLastDateTime()).thenReturn(LAST_READING);
        when(channel.getReading(LAST_READING)).thenReturn(Optional.of(lastReading));
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(readingType.isCumulative()).thenReturn(false);

        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));
        when(channel.getReadings(interval)).thenReturn(asList(readingRecord, readingRecord2, readingRecord3, readingRecord4));
        List myList = asList(readingRecord, readingRecord2, readingRecord3, readingRecord4);
        when(usagePoint.getReadings(interval, readingType)).thenReturn(myList);

        when(readingRecord.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord.getReadingType()).thenReturn(readingType);
        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(201, 0));
        when(readingRecord2.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord2.getReadingType()).thenReturn(readingType);
        when(readingRecord3.getValue()).thenReturn(BigDecimal.valueOf(202, 0));
        when(readingRecord3.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord3.getReadingType()).thenReturn(readingType);
        when(readingRecord4.getValue()).thenReturn(BigDecimal.valueOf(203, 0));
        when(readingRecord4.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord4.getReadingType()).thenReturn(readingType);
    }

    @Test
    public void testChannelData() {
        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");

        String json = target("usagepoints/1/registers/" + REGISTER_MRID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>> get("$.data")).hasSize(4);
        assertThat(jsonModel.<Long> get("$.data[0].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[0].value")).isEqualTo("200");

        assertThat(jsonModel.<Long> get("$.data[1].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[1].value")).isEqualTo("201");

        assertThat(jsonModel.<Long> get("$.data[2].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[2].value")).isEqualTo("202");

        assertThat(jsonModel.<Long> get("$.data[3].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[3].value")).isEqualTo("203");
    }

    @Test
    public void testRegisterInfo() {
        String json = target("usagepoints/1/registers/" + REGISTER_MRID1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<String> get("$.readingType.mRID")).isEqualTo(REGISTER_MRID1);
        assertThat(jsonModel.<Number> get("$.lastValueTimestamp")).isEqualTo(LAST_READING.toEpochMilli());
    }

}