package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.issue.datacollection.event.MeterReadingEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.TrendPeriodUnit;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.elster.jupiter.cbo.Accumulation.DELTADELTA;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;

public class MeterReadingIssueEventTest extends BaseTest {

    private String readingTypeCode;
    private ReadingType readingType;
    private TransactionContext context;
    private Meter meter;
    private Clock clock;

    @Before
    public void setUp() {
        readingTypeCode = ReadingTypeCodeBuilder
                .of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .accumulate(DELTADELTA)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .period(TimeAttribute.MINUTE15)
                .code();
        readingType = getOrmService().getDataModel("MTR").get().mapper(ReadingType.class).getOptional(readingTypeCode).get();

        AmrSystem amrSystem = getMeteringService().findAmrSystem(ModuleConstants.MDC_AMR_SYSTEM_ID).get();
        meter = amrSystem.newMeter("test device");
        context = getTransactionService().getContext();
        meter.save();

        clock = Clock.systemUTC();
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void testNoReadings() {
        MeterReadingEvent event = new MeterReadingEvent(meter, readingType, clock);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.HOURS.getId())).isEqualTo(0.0);
    }

    @Test
    public void testOnlyOneReading() {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();

        Instant dateTime = LocalDateTime.now(clock).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
        IntervalBlockImpl block = IntervalBlockImpl.of(readingTypeCode);
        block.addIntervalReading(IntervalReadingImpl.of(dateTime, BigDecimal.valueOf(0)));

        meterReading.addIntervalBlock(block);
        meter.store(meterReading);
        readingType = getMeteringService().getReadingType(readingTypeCode).get();

        MeterReadingEvent event = new MeterReadingEvent(meter, readingType, clock);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.HOURS.getId())).isEqualTo(0.0);
    }

    @Test
    public void testComputeMaxSlope() {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        Instant dateTime = LocalDateTime.now(clock).minusHours(2).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);

        IntervalBlockImpl block = IntervalBlockImpl.of(readingTypeCode);

        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(0, ChronoUnit.MINUTES), BigDecimal.valueOf(10)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(15, ChronoUnit.MINUTES), BigDecimal.valueOf(11)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(12)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(45, ChronoUnit.MINUTES), BigDecimal.valueOf(13)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(60, ChronoUnit.MINUTES), BigDecimal.valueOf(14)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(75, ChronoUnit.MINUTES), BigDecimal.valueOf(15)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(90, ChronoUnit.MINUTES), BigDecimal.valueOf(16)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(105, ChronoUnit.MINUTES), BigDecimal.valueOf(17)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(120, ChronoUnit.MINUTES), BigDecimal.valueOf(18)));
        block.addIntervalReading(IntervalReadingImpl.of(dateTime.plus(135, ChronoUnit.MINUTES), BigDecimal.valueOf(19)));

        meterReading.addIntervalBlock(block);
        meter.store(meterReading);
        readingType = getMeteringService().getReadingType(readingTypeCode).get();

        MeterReadingEvent event = new MeterReadingEvent(meter, readingType, clock);
        assertThat(event.computeMaxSlope(2, TrendPeriodUnit.HOURS.getId())).isEqualTo(4.0);
    }

    @Test
    public void testComputeMaxSlopeForLimitedPeriod() {

        MeterReadingImpl meterReading1 = MeterReadingImpl.newInstance();
        Instant twoHoursAgo =  LocalDateTime.now(clock).minusHours(2).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
        IntervalBlockImpl block1 = IntervalBlockImpl.of(readingTypeCode);
        block1.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(0, ChronoUnit.MINUTES), BigDecimal.valueOf(-100500)));
        block1.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(15, ChronoUnit.MINUTES), BigDecimal.valueOf(100500)));
        meterReading1.addIntervalBlock(block1);
        meter.store(meterReading1);

        MeterReadingImpl meterReading2 = MeterReadingImpl.newInstance();
        IntervalBlockImpl block2 = IntervalBlockImpl.of(readingTypeCode);
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(0, ChronoUnit.MINUTES), BigDecimal.valueOf(0)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(15, ChronoUnit.MINUTES), BigDecimal.valueOf(25)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(45, ChronoUnit.MINUTES), BigDecimal.valueOf(75)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(60, ChronoUnit.MINUTES), BigDecimal.valueOf(100)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(75, ChronoUnit.MINUTES), BigDecimal.valueOf(125)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(90, ChronoUnit.MINUTES), BigDecimal.valueOf(150)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(105, ChronoUnit.MINUTES), BigDecimal.valueOf(175)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(120, ChronoUnit.MINUTES), BigDecimal.valueOf(200)));
        block2.addIntervalReading(IntervalReadingImpl.of(twoHoursAgo.plus(135, ChronoUnit.MINUTES), BigDecimal.valueOf(225)));
        meterReading2.addIntervalBlock(block2);

        meter.store(meterReading2);

        MeterReadingEvent event = new MeterReadingEvent(meter, readingType, clock);
        assertThat(event.computeMaxSlope(2, TrendPeriodUnit.HOURS.getId())).isEqualTo(100.0);
    }
}
