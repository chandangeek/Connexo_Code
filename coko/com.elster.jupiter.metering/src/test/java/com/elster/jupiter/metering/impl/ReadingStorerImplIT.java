package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplIT {

    private static final Set<ReadingQualityType> BATTERY_LOW = new HashSet<>(Arrays.asList(ProtocolReadingQualities.BATTERY_LOW.getReadingQualityType()));

    public static final String SECONDARY_DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PRIMARY_DELTA = "0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String SECONDARY_BULK = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PRIMARY_BULK = "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String SECONDARY_PULSE_DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.111.0";
    public static final String SECONDARY_BULK_REG = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PRIMARY_BULK_REG = "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0";

    private static final ZonedDateTime ACTIVATION = ZonedDateTime.of(1975, 9, 19, 21, 46, 55, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime BASE = ZonedDateTime.of(2025, 12, 20, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withClockAndReadingTypes(Clock.fixed(BASE.plusMonths(1)
                    .toInstant(), TimeZoneNeutral.getMcMurdo()),
            SECONDARY_DELTA, SECONDARY_BULK, PRIMARY_DELTA, PRIMARY_BULK, SECONDARY_PULSE_DELTA, SECONDARY_BULK_REG, PRIMARY_BULK_REG);

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testWriteBulkData() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType deltaReadingType = meteringService.getReadingType(SECONDARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();
        Channel channel = createMeterAndChannelWithDelta(bulkReadingType);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(9);
        assertThat(readings.get(0).getQuantity(deltaReadingType)).isNull();
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.toInstant());

        for (int i = 1; i < 8; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }

        meterReading = MeterReadingImpl.newInstance();
        intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        readings = channel.getReadings(Range.atLeast(BASE.plusMinutes(15 * 9).toInstant()));

        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(19000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * 9).toInstant());
    }

    @Test
    @Transactional
    public void testWriteBulkDataWithMultiplier() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType deltaReadingType = meteringService.getReadingType(PRIMARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();
        ReadingType bulkPrimaryReadingType = meteringService.getReadingType(PRIMARY_BULK).get();

        Channel channel = createMeterAndChannelWithMultiplier(bulkReadingType, bulkPrimaryReadingType, BigDecimal.valueOf(5, 0));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));
        assertThat(readings).hasSize(9);
        assertThat(readings.get(0).getQuantity(deltaReadingType)).isNull();
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.toInstant());

        for (int i = 1; i < 8; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(5000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }

        meterReading = MeterReadingImpl.newInstance();
        intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        readings = channel.getReadings(Range.atLeast(BASE.plusMinutes(15 * 9).toInstant()));
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(5000, 2), 3, "Wh"));
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(19000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * 9).toInstant());
    }

    @Test
    @Transactional
    public void testWritePulseDataWithMultiplier() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType deltaReadingType = meteringService.getReadingType(PRIMARY_DELTA).get();
        ReadingType pulseDeltaReadingType = meteringService.getReadingType(SECONDARY_PULSE_DELTA).get();

        Channel channel = createMeterAndChannelWithMultiplier(pulseDeltaReadingType, deltaReadingType, BigDecimal.valueOf(50, 0));

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(pulseDeltaReadingType.getMRID());
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18, 0), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));
        assertThat(readings).hasSize(9);

        for (int i = 0; i < 8; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(500 + 50 * i, 0), 3, "Wh"));
            assertThat(baseReadingRecord.getQuantity(pulseDeltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10 + i, 0), 0, "Count"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }

        Channel readChannel = meteringService.findChannel(channel.getId()).get();
        assertThat(readChannel.getReadingTypes()).hasSize(2);
    }

    @Test
    @Transactional
    public void testWriteIrregularBulkWithMultiplied() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType multipliedBulkReadingType = meteringService.getReadingType(PRIMARY_BULK_REG).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK_REG).get();

        Channel channel = createMeterAndChannelWithMultiplier(bulkReadingType, multipliedBulkReadingType, BigDecimal.valueOf(2, 0));

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        Reading reading = ReadingImpl.of(bulkReadingType.getMRID(), BigDecimal.valueOf(123406, 2), BASE.toInstant());
        meterReading.addReading(reading);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(1);
        BaseReadingRecord baseReadingRecord = readings.get(0);
        assertThat(baseReadingRecord.getQuantity(multipliedBulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(246812, 2), 3, "Wh"));
        assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(123406, 2), 3, "Wh"));
        assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.toInstant());

        Channel readChannel = meteringService.findChannel(channel.getId()).get();
        assertThat(readChannel.getReadingTypes()).hasSize(2);
    }

    @Test
    @Transactional
    public void testWriteMissingBulkData() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType deltaReadingType = meteringService.getReadingType(SECONDARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();

        Channel channel = createMeterAndChannelWithDelta(bulkReadingType);

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
        // so not this one ! This is the missing one at first : intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(8);

        for (int i = 0; i < 7; i++) {
            int multiplier = i >= 4 ? i + 1 : i;
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0 && i != 4) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + multiplier * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * multiplier).toInstant());
        }

        meterReading = MeterReadingImpl.newInstance();
        intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);

        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(10);

        for (int i = 0; i < 9; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }
    }

    @Test
    @Transactional
    public void testWriteMissingBulkDataWithMultiplier() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType deltaReadingType = meteringService.getReadingType(PRIMARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();
        ReadingType bulkPrimaryReadingType = meteringService.getReadingType(PRIMARY_BULK).get();

        Channel channel = createMeterAndChannelWithMultiplier(bulkReadingType, bulkPrimaryReadingType, BigDecimal.valueOf(2, 0));

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
        // so not this one ! This is the missing one at first : intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));
        assertThat(readings).hasSize(8);

        for (int i = 0; i < 7; i++) {
            int multiplier = i >= 4 ? i + 1 : i;
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0 && i != 4) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(2000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + multiplier * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * multiplier).toInstant());
        }

        meterReading = MeterReadingImpl.newInstance();
        intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));
        meterReading.addIntervalBlock(intervalBlock);
        channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);

        readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(10);

        for (int i = 0; i < 9; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(2000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }
    }

    @Test
    @Transactional
    public void testWriteBulkDataWithBackflowUnderflowAndOverflowForMdcAndMdm() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType deltaReadingType = meteringService.getReadingType(SECONDARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();

        Channel channel = createMeterAndChannelWithDelta(bulkReadingType);

        Meter meter = channel.getChannelsContainer().getMeter().get();
        meter.startingConfigurationOn(Instant.EPOCH)
                .configureReadingType(bulkReadingType)
                .withOverflowValue(BigDecimal.valueOf(999999))
                .create();

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(100, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(999998, 0), Collections.emptySet()));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(30).toInstant(), BigDecimal.valueOf(100, 0), Collections.emptySet()));

        meterReading.addIntervalBlock(intervalBlock);

        Stream.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM).forEach(system -> {
            channel.getChannelsContainer().getMeter().get().store(system, meterReading);

            List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

            assertThat(readings).hasSize(3);

            assertThat(readings.get(0).getQuantity(deltaReadingType)).isNull();
            assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(100, 0), 3, "Wh"));
            assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.toInstant());

            assertThat(readings.get(1).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(-102, 0), 3, "Wh"));
            assertThat(readings.get(1).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(999998, 0), 3, "Wh"));
            assertThat(readings.get(1).getTimeStamp()).isEqualTo(BASE.plusMinutes(15).toInstant());

            assertThat(readings.get(2).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(102, 0), 3, "Wh"));
            assertThat(readings.get(2).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(100, 0), 3, "Wh"));
            assertThat(readings.get(2).getTimeStamp()).isEqualTo(BASE.plusMinutes(30).toInstant());

            List<ReadingQualityRecord> qualities = channel.findReadingQualities()
                    .atTimestamp(BASE.plusMinutes(15).toInstant())
                    .collect();
            assertThat(qualities).hasSize(1);
            assertThat(qualities.get(0).getTypeCode()).isEqualTo(ReadingQualityType.of(system, QualityCodeIndex.REVERSEROTATION).getCode());

            qualities = channel.findReadingQualities()
                    .atTimestamp(BASE.plusMinutes(30).toInstant())
                    .collect();
            assertThat(qualities).hasSize(1);
            assertThat(qualities.get(0).getTypeCode()).isEqualTo(ReadingQualityType.of(system, QualityCodeIndex.OVERFLOWCONDITIONDETECTED).getCode());
        });
    }

    private Channel createMeterAndChannelWithMultiplier(ReadingType measured, ReadingType caluclated, BigDecimal multiplierValue) {
        AmrSystem mdc = inMemoryBootstrapModule.getMeteringService().findAmrSystem(1L).get();
        Meter meter = mdc.newMeter("AMR_ID", "myName")
                .create();
        MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());

        MultiplierType multiplierType = inMemoryBootstrapModule.getMeteringService().createMultiplierType("multiplierType");

        meterActivation.setMultiplier(multiplierType, multiplierValue);

        meter.startingConfigurationOn(ACTIVATION.toInstant())
                .configureReadingType(measured)
                .withMultiplierOfType(multiplierType)
                .calculating(caluclated)
                .create();

        return meterActivation.getChannelsContainer().createChannel(measured);
    }

    private Channel createMeterAndChannelWithDelta(ReadingType bulkReadingType) {
        AmrSystem mdc = inMemoryBootstrapModule.getMeteringService().findAmrSystem(1L).get();
            Meter meter = mdc.newMeter("AMR_ID", "myName")
                    .create();
        MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());
        return meterActivation.getChannelsContainer().createChannel(bulkReadingType);
    }
}
