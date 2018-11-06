/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

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
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingInfoType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.BaseReading;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class SendMeterReadingsTest {
    protected static final String METER_MRID = "Meter mrid";
    protected static final String METER_NAME = "Meter name";
    protected static final String USAGE_POINT_MRID = "Usage Point mrid";
    protected static final String USAGE_POINT_NAME = "Usage Point name";
    protected static final String DAILY_MRID = "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    protected static final String DAILY_FULL_ALIAS_NAME = "[Daily] Secondary Delta A+ (kWh)";
    protected static final ZonedDateTime JAN_1ST = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    protected static final ReadingQualityType INFERRED = new ReadingQualityType("3.11.1");

    @Rule
    public TestRule snowy = Using.timeZoneOfMcMurdo();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected List<ReadingInfoType> listReadingInfoType = new ArrayList<>();

    @Mock
    protected Clock clock;
    @Mock
    protected Meter meter;
    @Mock
    protected UsagePoint usagePoint;
    @Mock
    protected ReadingType dailyReadingType;
    @Mock
    protected ReadingStorer readingStorer;

    protected void mockMeter() {
        when(meter.getMRID()).thenReturn(METER_MRID);
        when(meter.getName()).thenReturn(METER_NAME);
    }

    protected void mockUsagePoint() {
        when(usagePoint.getMRID()).thenReturn(USAGE_POINT_MRID);
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
    }

    protected void mockReadingsInfoType(ReadingInfoType mock, ReadingType readingType, BaseReading baseReading) {
        when(mock.getMeter()).thenReturn(meter);
        when(mock.getUsagePoint()).thenReturn(usagePoint);
        when(mock.getReading()).thenReturn(baseReading);
        when(mock.getReadingType()).thenReturn(readingType);
    }

    protected void mockReadingType(ReadingType mock, String mRID, String fullAliasName, boolean regular) {
        when(mock.getMRID()).thenReturn(mRID);
        when(mock.getFullAliasName()).thenReturn(fullAliasName);
        when(mock.isRegular()).thenReturn(regular);
        when(mock.getAccumulation()).thenReturn(regular ? Accumulation.DELTADELTA : Accumulation.BULKQUANTITY);
        when(mock.getAggregate()).thenReturn(regular ? Aggregate.SUM : Aggregate.NOTAPPLICABLE);
        when(mock.getArgument()).thenReturn(regular ? new RationalNumber(1, 3) : RationalNumber.NOTAPPLICABLE);
        when(mock.getCommodity()).thenReturn(regular ? Commodity.ELECTRICITY_SECONDARY_METERED : Commodity.ELECTRICITY_PRIMARY_METERED);
        when(mock.getConsumptionTier()).thenReturn(regular ? 1 : 2);
        when(mock.getCpp()).thenReturn(regular ? 1 : 2);
        when(mock.getCurrency()).thenReturn(Currency.getInstance(regular ? "RUB" : "XXX"));
        when(mock.getFlowDirection()).thenReturn(regular ? FlowDirection.TOTAL : FlowDirection.FORWARD);
        when(mock.getInterharmonic()).thenReturn(regular ? RationalNumber.NOTAPPLICABLE : new RationalNumber(2, 3));
        when(mock.getMacroPeriod()).thenReturn(regular ? MacroPeriod.DAILY : MacroPeriod.NOTAPPLICABLE);
        when(mock.getMeasurementKind()).thenReturn(regular ? MeasurementKind.POWER : MeasurementKind.ENERGY);
        when(mock.getMeasuringPeriod()).thenReturn(regular ? TimeAttribute.NOTAPPLICABLE : TimeAttribute.HOUR24);
        when(mock.getMultiplier()).thenReturn(regular ? MetricMultiplier.KILO : MetricMultiplier.ZERO);
        when(mock.getPhases()).thenReturn(regular ? Phase.NOTAPPLICABLE : Phase.PHASES1);
        when(mock.getTou()).thenReturn(regular ? 1 : 2);
        when(mock.getUnit()).thenReturn(regular ? ReadingTypeUnit.WATTHOUR : ReadingTypeUnit.WATT);
    }

    protected void mockIntervalReading(AggregatedChannel.AggregatedIntervalReadingRecord mock,
                                       Range<Instant> interval, double value, ReadingQualityRecord... qualities) {
        when(mock.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(mock.getReportedDateTime()).thenReturn(interval.upperEndpoint().plusSeconds(1));
        when(mock.getValue()).thenReturn(BigDecimal.valueOf(value));
        doReturn(Arrays.asList(qualities)).when(mock).getReadingQualities();
    }

    protected void mockReadingQuality(ReadingQualityRecord mock, ReadingQualityType type, Instant timestamp, String comment) {
        when(mock.getType()).thenReturn(type);
        when(mock.getReadingTimestamp()).thenReturn(timestamp);
        when(mock.getTimestamp()).thenReturn(timestamp.plusSeconds(3));
        when(mock.getComment()).thenReturn(comment);
    }
}