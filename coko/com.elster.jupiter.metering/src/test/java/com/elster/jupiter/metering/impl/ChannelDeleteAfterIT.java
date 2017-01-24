package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.orm.TransactionRequiredException;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bbl on 13/06/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChannelDeleteAfterIT {

    private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Instant time1 = ZONED_DATE_TIME.toInstant();
    private static final Instant register_time2 = ZONED_DATE_TIME.plusDays(1).toInstant();
    private static final Instant register_time3 = ZONED_DATE_TIME.plusDays(2).toInstant();
    private static final Instant channel_time2 = ZONED_DATE_TIME.plusMinutes(1 * 15).toInstant();
    private static final Instant channel_time3 = ZONED_DATE_TIME.plusMinutes(2 * 15).toInstant();

    public static final String BULK_REGULAR = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String DELTA_REGULAR = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String BULK_IRRREGULAR = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String BILLING_PERIOD_IRRREGULAR = "8.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PULSE_COUNT_REGULAR = "0.0.2.1.0.0.142.0.0.1.1.0.0.0.0.0.111.0";
    public static final String METER_NAME = "myName";
    public static final String DEVICE_READING_QUALITY_CODE = "1.3.4";
    public static final String DEVICE_READING_QUALITY_COMMENT = "Device reading quality";

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(BULK_IRRREGULAR, BULK_REGULAR, DELTA_REGULAR, BILLING_PERIOD_IRRREGULAR, PULSE_COUNT_REGULAR);
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        inMemoryBootstrapModule.getTransactionService().run(() -> {
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            Meter meter = meteringService.findAmrSystem(1).get()
                    .newMeter("amrID", METER_NAME)
                    .create();

            MultiplierType multiplierType = meteringService.createMultiplierType("Pulse multiplier");
            meter.startingConfigurationOn(ZONED_DATE_TIME.minusMinutes(15).toInstant())
                    .configureReadingType(meteringService.getReadingType(PULSE_COUNT_REGULAR).get())
                    .withMultiplierOfType(multiplierType)
                    .calculating(meteringService.getReadingType(BULK_REGULAR).get())
                    .create();

            MeterActivation activate = meter.activate(ZONED_DATE_TIME.minusMinutes(15).toInstant());
            activate.setMultiplier(multiplierType, BigDecimal.TEN);

            MeterReadingImpl mr = MeterReadingImpl.newInstance();
            ReadingImpl reading = ReadingImpl.of(BULK_IRRREGULAR, BigDecimal.ONE, time1);
            mr.addReading(reading);
            reading = ReadingImpl.of(BULK_IRRREGULAR, BigDecimal.valueOf(2), register_time2);
            reading.addQuality(DEVICE_READING_QUALITY_CODE, DEVICE_READING_QUALITY_COMMENT);
            mr.addReading(reading);
            reading = ReadingImpl.of(BULK_IRRREGULAR, BigDecimal.valueOf(3), register_time3);
            mr.addReading(reading);

            reading = ReadingImpl.of(BILLING_PERIOD_IRRREGULAR, BigDecimal.ONE, time1);
            mr.addReading(reading);
            reading = ReadingImpl.of(BILLING_PERIOD_IRRREGULAR, BigDecimal.valueOf(2), register_time2);
            reading.addQuality(DEVICE_READING_QUALITY_CODE, DEVICE_READING_QUALITY_COMMENT);
            reading.setTimePeriod(Range.openClosed(time1, register_time2));
            mr.addReading(reading);
            reading = ReadingImpl.of(BILLING_PERIOD_IRRREGULAR, BigDecimal.valueOf(3), register_time3);
            mr.addReading(reading);

            IntervalBlockImpl block = IntervalBlockImpl.of(BULK_REGULAR);
            block.addIntervalReading(IntervalReadingImpl.of(ZONED_DATE_TIME.toInstant(), BigDecimal.ONE));
            IntervalReadingImpl of = IntervalReadingImpl.of(channel_time2, BigDecimal.valueOf(2));
            of.addQuality(DEVICE_READING_QUALITY_CODE, DEVICE_READING_QUALITY_COMMENT);
            block.addIntervalReading(of);
            block.addIntervalReading(IntervalReadingImpl.of(channel_time3, BigDecimal.valueOf(3)));
            mr.addIntervalBlock(block);

            block = IntervalBlockImpl.of(PULSE_COUNT_REGULAR);
            block.addIntervalReading(IntervalReadingImpl.of(ZONED_DATE_TIME.toInstant(), BigDecimal.ONE));
            of = IntervalReadingImpl.of(channel_time2, BigDecimal.valueOf(2));
            of.addQuality(DEVICE_READING_QUALITY_CODE, DEVICE_READING_QUALITY_COMMENT);
            block.addIntervalReading(of);
            block.addIntervalReading(IntervalReadingImpl.of(channel_time3, BigDecimal.valueOf(3)));
            mr.addIntervalBlock(block);

            meter.store(QualityCodeSystem.MDC, mr);
        });
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private Optional<ChannelImpl> getChannelFor(String readingType) {
        Meter meter = inMemoryBootstrapModule.getMeteringService().findMeterByName(METER_NAME).get();

        MeterActivation meterActivation = meter.getMeterActivation(ZONED_DATE_TIME.toInstant()).get();
        return meterActivation.getChannelsContainer()
                .getChannels()
                .stream()
                .map(ChannelImpl.class::cast)
                .filter(channel -> channel.getReadingTypes().stream().anyMatch(rt -> readingType.equals(rt.getMRID())))
                .findFirst();
    }

    @Test
    @Transactional
    public void testDeleteAfterOfRegister() {
        ChannelImpl channelUnderTest = getChannelFor(BULK_IRRREGULAR).get();

        MeterReading meterReading = channelUnderTest.deleteReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()));
        assertThat(meterReading.getIntervalBlocks()).isEmpty();
        assertThat(meterReading.getReadings()).hasSize(2);
        assertThat(meterReading.getReadings().get(0).getReadingTypeCode()).isEqualTo(BULK_IRRREGULAR);
        assertThat(meterReading.getReadings().get(0).getTimeStamp()).isEqualTo(register_time2);
        assertThat(meterReading.getReadings().get(0).getValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(meterReading.getReadings().get(0).getReadingQualities()).hasSize(1);
        assertThat(meterReading.getReadings().get(0).getTimePeriod()).isEqualTo(Optional.empty());
        assertThat(meterReading.getReadings().get(0).getReadingQualities()).has(new Condition<>((Predicate<List<? extends ReadingQuality>>) readingQualities -> readingQualities.stream()
                .anyMatch(rq -> DEVICE_READING_QUALITY_CODE.equals(rq.getTypeCode())), "wrong qualities"));

        assertThat(meterReading.getReadings().get(1).getReadingTypeCode()).isEqualTo(BULK_IRRREGULAR);
        assertThat(meterReading.getReadings().get(1).getTimeStamp()).isEqualTo(register_time3);
        assertThat(meterReading.getReadings().get(1).getValue()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(meterReading.getReadings().get(1).getReadingQualities()).isEmpty();
        assertThat(meterReading.getReadings().get(0).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(channelUnderTest.getRegisterReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))).hasSize(1);
        assertThat(channelUnderTest.getRegisterReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .get(0)
                .getTimeStamp()).isEqualTo(time1);
        assertThat(channelUnderTest.findReadingQualities()
                .inTimeInterval(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .collect()).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteAfterOfBillingRegister() {
        ChannelImpl channelUnderTest = getChannelFor(BILLING_PERIOD_IRRREGULAR).get();

        MeterReading meterReading = channelUnderTest.deleteReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()));
        assertThat(meterReading.getIntervalBlocks()).isEmpty();
        assertThat(meterReading.getReadings()).hasSize(2);
        assertThat(meterReading.getReadings().get(0).getReadingTypeCode()).isEqualTo(BILLING_PERIOD_IRRREGULAR);
        assertThat(meterReading.getReadings().get(0).getTimeStamp()).isEqualTo(register_time2);
        assertThat(meterReading.getReadings().get(0).getValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(meterReading.getReadings().get(0).getReadingQualities()).hasSize(1);
        assertThat(meterReading.getReadings().get(0).getTimePeriod()).isEqualTo(Optional.of(Range.openClosed(time1, register_time2)));
        assertThat(meterReading.getReadings().get(0).getReadingQualities()).has(new Condition<>((Predicate<List<? extends ReadingQuality>>) readingQualities -> readingQualities.stream()
                .anyMatch(rq -> DEVICE_READING_QUALITY_CODE.equals(rq.getTypeCode())), "wrong qualities"));

        assertThat(meterReading.getReadings().get(1).getReadingTypeCode()).isEqualTo(BILLING_PERIOD_IRRREGULAR);
        assertThat(meterReading.getReadings().get(1).getTimeStamp()).isEqualTo(register_time3);
        assertThat(meterReading.getReadings().get(1).getValue()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(meterReading.getReadings().get(1).getReadingQualities()).isEmpty();
        assertThat(meterReading.getReadings().get(1).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(channelUnderTest.getRegisterReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))).hasSize(1);
        assertThat(channelUnderTest.getRegisterReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .get(0)
                .getTimeStamp()).isEqualTo(time1);
        assertThat(channelUnderTest.findReadingQualities()
                .inTimeInterval(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .collect()).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteAfterOfRegularChannel() {
        ChannelImpl channelUnderTest = getChannelFor(BULK_REGULAR).get();

        MeterReading meterReading = channelUnderTest.deleteReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()));
        assertThat(meterReading.getReadings()).isEmpty();
        assertThat(meterReading.getIntervalBlocks()).hasSize(2);

        assertThat(meterReading.getIntervalBlocks()).has(new Condition<>((Predicate<List<? extends IntervalBlock>>) ibs -> ibs
                .stream()
                .anyMatch(ib -> BULK_REGULAR.equals(ib.getReadingTypeCode())), "does not contain " + BULK_REGULAR));
        assertThat(meterReading.getIntervalBlocks()).has(new Condition<>((Predicate<List<? extends IntervalBlock>>) ibs -> ibs
                .stream()
                .anyMatch(ib -> DELTA_REGULAR.equals(ib.getReadingTypeCode())), "does not contain " + DELTA_REGULAR));

        IntervalBlock block = meterReading.getIntervalBlocks()
                .stream()
                .filter(ib -> BULK_REGULAR.equals(ib.getReadingTypeCode()))
                .findFirst()
                .get();
        assertThat(block.getIntervals()).hasSize(2);
        assertThat(block.getIntervals().get(0).getTimeStamp()).isEqualTo(channel_time2);
        assertThat(block.getIntervals().get(0).getValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(block.getIntervals().get(0).getReadingQualities()).hasSize(1);
        assertThat(block.getIntervals().get(0).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(block.getIntervals().get(1).getTimeStamp()).isEqualTo(channel_time3);
        assertThat(block.getIntervals().get(1).getValue()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(block.getIntervals().get(1).getReadingQualities()).isEmpty();
        assertThat(block.getIntervals().get(1).getTimePeriod()).isEqualTo(Optional.empty());

        block = meterReading.getIntervalBlocks()
                .stream()
                .filter(ib -> DELTA_REGULAR.equals(ib.getReadingTypeCode()))
                .findFirst()
                .get();
        assertThat(block.getIntervals()).hasSize(2);
        assertThat(block.getIntervals().get(0).getTimeStamp()).isEqualTo(channel_time2);
        assertThat(block.getIntervals().get(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(block.getIntervals().get(0).getReadingQualities()).hasSize(1);
        assertThat(block.getIntervals()
                .get(0)
                .getReadingQualities()).has(new Condition<>((Predicate<List<? extends ReadingQuality>>) readingQualities -> readingQualities
                .stream()
                .anyMatch(rq -> DEVICE_READING_QUALITY_CODE.equals(rq.getTypeCode())), "reading qualities do not contain " + DEVICE_READING_QUALITY_CODE));
        assertThat(block.getIntervals().get(0).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(block.getIntervals().get(1).getTimeStamp()).isEqualTo(channel_time3);
        assertThat(block.getIntervals().get(1).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(block.getIntervals().get(1).getReadingQualities()).isEmpty();
        assertThat(block.getIntervals().get(1).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(channelUnderTest.getIntervalReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))).hasSize(1);
        assertThat(channelUnderTest.getIntervalReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .get(0)
                .getTimeStamp()).isEqualTo(time1);
        assertThat(channelUnderTest.findReadingQualities()
                .inTimeInterval(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .collect()).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteAfterOfRegularPulseChannel() {
        ChannelImpl channelUnderTest = getChannelFor(PULSE_COUNT_REGULAR).get();

        MeterReading meterReading = channelUnderTest.deleteReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()));
        assertThat(meterReading.getReadings()).isEmpty();
        assertThat(meterReading.getIntervalBlocks()).hasSize(2);

        assertThat(meterReading.getIntervalBlocks()).has(new Condition<>((Predicate<List<? extends IntervalBlock>>) ibs -> ibs
                .stream()
                .anyMatch(ib -> PULSE_COUNT_REGULAR.equals(ib.getReadingTypeCode())), "does not contain " + PULSE_COUNT_REGULAR));
        assertThat(meterReading.getIntervalBlocks()).has(new Condition<>((Predicate<List<? extends IntervalBlock>>) ibs -> ibs
                .stream()
                .anyMatch(ib -> DELTA_REGULAR.equals(ib.getReadingTypeCode())), "does not contain " + DELTA_REGULAR));

        IntervalBlock block = meterReading.getIntervalBlocks()
                .stream()
                .filter(ib -> PULSE_COUNT_REGULAR.equals(ib.getReadingTypeCode()))
                .findFirst()
                .get();
        assertThat(block.getIntervals()).hasSize(2);
        assertThat(block.getIntervals().get(0).getTimeStamp()).isEqualTo(channel_time2);
        assertThat(block.getIntervals().get(0).getValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(block.getIntervals().get(0).getReadingQualities()).hasSize(1);
        assertThat(block.getIntervals().get(0).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(block.getIntervals().get(1).getTimeStamp()).isEqualTo(channel_time3);
        assertThat(block.getIntervals().get(1).getValue()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(block.getIntervals().get(1).getReadingQualities()).isEmpty();
        assertThat(block.getIntervals().get(1).getTimePeriod()).isEqualTo(Optional.empty());

        block = meterReading.getIntervalBlocks()
                .stream()
                .filter(ib -> DELTA_REGULAR.equals(ib.getReadingTypeCode()))
                .findFirst()
                .get();
        assertThat(block.getIntervals()).hasSize(2);
        assertThat(block.getIntervals().get(0).getTimeStamp()).isEqualTo(channel_time2);
        assertThat(block.getIntervals().get(0).getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(block.getIntervals().get(0).getReadingQualities()).hasSize(1);
        assertThat(block.getIntervals()
                .get(0)
                .getReadingQualities()).has(new Condition<>((Predicate<List<? extends ReadingQuality>>) readingQualities -> readingQualities
                .stream()
                .anyMatch(rq -> DEVICE_READING_QUALITY_CODE.equals(rq.getTypeCode())), "reading qualities do not contain " + DEVICE_READING_QUALITY_CODE));
        assertThat(block.getIntervals().get(0).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(block.getIntervals().get(1).getTimeStamp()).isEqualTo(channel_time3);
        assertThat(block.getIntervals().get(1).getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(block.getIntervals().get(1).getReadingQualities()).isEmpty();
        assertThat(block.getIntervals().get(1).getTimePeriod()).isEqualTo(Optional.empty());

        assertThat(channelUnderTest.getIntervalReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))).hasSize(1);
        assertThat(channelUnderTest.getIntervalReadings(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .get(0)
                .getTimeStamp()).isEqualTo(time1);
        assertThat(channelUnderTest.findReadingQualities()
                .inTimeInterval(Range.atLeast(ZONED_DATE_TIME.toInstant()))
                .collect()).isEmpty();
    }


    @Test(expected = TransactionRequiredException.class)
    public void testDeleteTransactionRequired() {
        ChannelImpl channelUnderTest = getChannelFor(BULK_IRRREGULAR).get();

        channelUnderTest.deleteReadings(Range.atLeast(ZONED_DATE_TIME.plusHours(6).toInstant()));
    }
}
