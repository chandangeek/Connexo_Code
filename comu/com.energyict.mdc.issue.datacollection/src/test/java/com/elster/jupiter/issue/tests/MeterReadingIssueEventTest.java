package com.elster.jupiter.issue.tests;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.issue.datacollection.MeterReadingIssueEvent;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.TrendPeriodUnit;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.transaction.TransactionContext;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Calendar;

import static com.elster.jupiter.cbo.Accumulation.DELTADELTA;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MeterReadingIssueEventTest extends BaseTest {
    
    private String readingTypeCode;
    private ReadingType readingType;
    private TransactionContext context;
    private Meter meter;
    
    @Before
    public void setUp(){
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
    }
    
    @After
    public void tearDown() {
        context.close();
    }
    
    @Test
    public void testNoReadings() {
        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.HOURS.getId())).isEqualTo(0.0);
    }
    
    @Test
    public void testOnlyOneReading() {
        MeterReadingImpl meterReading = new MeterReadingImpl();
        DateTime dateTime = getCurrentDateTime();

        IntervalBlockImpl block = new IntervalBlockImpl(readingTypeCode);
        block.addIntervalReading(new IntervalReadingImpl(dateTime.toDate(), BigDecimal.valueOf(0)));

        meterReading.addIntervalBlock(block);
        meter.store(meterReading);
        readingType = getMeteringService().getReadingType(readingTypeCode).get();

        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.HOURS.getId())).isEqualTo(0.0);
    }

    @Test
    public void testComputeMaxSlope() {
        MeterReadingImpl meterReading = new MeterReadingImpl();
        DateTime now = getCurrentDateTime();
        DateTime dateTime = now.minusHours(2);

        IntervalBlockImpl block = new IntervalBlockImpl(readingTypeCode);

        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(0).toDate(), BigDecimal.valueOf(10)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(15).toDate(), BigDecimal.valueOf(11)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(30).toDate(), BigDecimal.valueOf(12)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(45).toDate(), BigDecimal.valueOf(13)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(60).toDate(), BigDecimal.valueOf(14)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(75).toDate(), BigDecimal.valueOf(15)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(90).toDate(), BigDecimal.valueOf(16)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(105).toDate(), BigDecimal.valueOf(17)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(120).toDate(), BigDecimal.valueOf(18)));
        block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(135).toDate(), BigDecimal.valueOf(19)));

        meterReading.addIntervalBlock(block);
        meter.store(meterReading);
        readingType = getMeteringService().getReadingType(readingTypeCode).get();

        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(2, TrendPeriodUnit.HOURS.getId())).isEqualTo(4.0);
    }
    
    @Test
    public void testComputeMaxSlopeForLimitedPeriod() {
        DateTime now = getCurrentDateTime();

        MeterReadingImpl meterReading1 = new MeterReadingImpl();
        DateTime twoHoursAgo = now.minusHours(2);// readings 2 hours ago
        IntervalBlockImpl block1 = new IntervalBlockImpl(readingTypeCode);
        block1.addIntervalReading(new IntervalReadingImpl(twoHoursAgo.plusMinutes(0).toDate(), BigDecimal.valueOf(-100500)));
        block1.addIntervalReading(new IntervalReadingImpl(twoHoursAgo.plusMinutes(15).toDate(), BigDecimal.valueOf(100500)));
        meterReading1.addIntervalBlock(block1);
        meter.store(meterReading1);

        MeterReadingImpl meterReading2 = new MeterReadingImpl();
        DateTime twoHoursBefore = now.minusHours(2);// readings 1 hour before
        IntervalBlockImpl block2 = new IntervalBlockImpl(readingTypeCode);
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(0).toDate(), BigDecimal.valueOf(0)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(15).toDate(), BigDecimal.valueOf(25)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(30).toDate(), BigDecimal.valueOf(50)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(45).toDate(), BigDecimal.valueOf(75)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(60).toDate(), BigDecimal.valueOf(100)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(75).toDate(), BigDecimal.valueOf(125)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(90).toDate(), BigDecimal.valueOf(150)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(105).toDate(), BigDecimal.valueOf(175)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(120).toDate(), BigDecimal.valueOf(200)));
        block2.addIntervalReading(new IntervalReadingImpl(twoHoursBefore.plusMinutes(135).toDate(), BigDecimal.valueOf(225)));
        meterReading2.addIntervalBlock(block2);

        meter.store(meterReading2);

        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(2, TrendPeriodUnit.HOURS.getId())).isEqualTo(100.0);
    }

    private static DateTime getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return new DateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                0, 0);
    }
}
