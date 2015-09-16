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
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

public class ChannelResourceTest extends UsagePointDataRestApplicationJerseyTest {

    public static final String CHANNEL_MRID1 = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

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
    private IntervalReadingRecord readingRecord, missingReadingRecord, editedReadingRecord, confirmedReadingRecord;

    public ChannelResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(meteringService.findMeter("1")).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePoint("1")).thenReturn(Optional.of(usagePoint));

        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel));
        ReadingType readingType = mockReadingType(CHANNEL_MRID1);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(meteringService.getReadingType(CHANNEL_MRID1)).thenReturn(Optional.of(readingType));

        when(channel.getLastDateTime()).thenReturn(LAST_READING);
        when(channel.getReading(LAST_READING)).thenReturn(Optional.of(lastReading));
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(readingType.isCumulative()).thenReturn(false);

        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));
        when(channel.getReadings(interval)).thenReturn(asList(readingRecord, missingReadingRecord, editedReadingRecord, confirmedReadingRecord));
        List myList = asList(readingRecord, missingReadingRecord, editedReadingRecord, confirmedReadingRecord);
        when(usagePoint.getReadingsWithFill(interval, readingType)).thenReturn(myList);

        when(readingRecord.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord.getReadingType()).thenReturn(readingType);

        when(missingReadingRecord.getValue()).thenReturn(null);
        when(missingReadingRecord.getProfileStatus()).thenReturn(new ProfileStatus(256));
        when(missingReadingRecord.getTimeStamp()).thenReturn(LAST_READING);
        when(missingReadingRecord.getReadingType()).thenReturn(readingType);

        when(editedReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(202, 0));
        when(editedReadingRecord.wasAdded()).thenReturn(false);
        when(editedReadingRecord.edited()).thenReturn(true);
        when(editedReadingRecord.getTimeStamp()).thenReturn(LAST_READING);
        when(editedReadingRecord.getReadingType()).thenReturn(readingType);

        when(confirmedReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(203, 0));
        when(confirmedReadingRecord.wasAdded()).thenReturn(false);
        when(confirmedReadingRecord.edited()).thenReturn(false);
        when(confirmedReadingRecord.confirmed()).thenReturn(true);
        when(confirmedReadingRecord.getTimeStamp()).thenReturn(LAST_READING);
        when(confirmedReadingRecord.getReadingType()).thenReturn(readingType);
    }

    @Test
    public void testChannelData() {
        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");

        String json = target("usagepoints/1/channels/" + CHANNEL_MRID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>> get("$.data")).hasSize(4);
        assertThat(jsonModel.<Long> get("$.data[0].interval.start")).isEqualTo(1410786196000L);
        assertThat(jsonModel.<Long> get("$.data[0].interval.end")).isEqualTo(1410787096000L);
        assertThat(jsonModel.<String> get("$.data[0].value")).isEqualTo("203");
        assertThat(jsonModel.<List<?>> get("$.data[0].intervalFlags")).isEmpty();

        assertThat(jsonModel.<Long> get("$.data[1].interval.start")).isEqualTo(1410786196000L);
        assertThat(jsonModel.<Long> get("$.data[1].interval.end")).isEqualTo(1410787096000L);
        assertThat(jsonModel.<String> get("$.data[1].value")).isEqualTo("202");

        assertThat(jsonModel.<Long> get("$.data[2].interval.start")).isEqualTo(1410786196000L);
        assertThat(jsonModel.<Long> get("$.data[2].interval.end")).isEqualTo(1410787096000L);
        assertThat(jsonModel.<String> get("$.data[2].value")).isNull();
        assertThat(jsonModel.<List<String>> get("$.data[2].intervalFlags").get(0)).isEqualTo("MISSING");

        assertThat(jsonModel.<Long> get("$.data[3].interval.start")).isEqualTo(1410786196000L);
        assertThat(jsonModel.<Long> get("$.data[3].interval.end")).isEqualTo(1410787096000L);
        assertThat(jsonModel.<String> get("$.data[3].value")).isEqualTo("200");
    }

    @Test
    public void testChannelInfo() {
        String json = target("usagepoints/1/channels/" + CHANNEL_MRID1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<String> get("$.readingType.mRID")).isEqualTo(CHANNEL_MRID1);
        assertThat(jsonModel.<Number> get("$.lastValueTimestamp")).isEqualTo(LAST_READING.toEpochMilli());
    }

}