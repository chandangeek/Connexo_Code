/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.data.MapEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConsecutiveZerosValidatorIT {

    private static final String MIN15_DELTA_A_PLUS_KWH = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String MIN15_BULK_A_PLUS_KWH = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    private static final String METER_NAME = "SPE01";

    private static final Clock clock = Clock.system(ZoneId.of("Europe/Athens"));
    private static ValidatorInMemoryBootstrapModule inMemoryBootstrapModule = ValidatorInMemoryBootstrapModule
            .withClockAndReadingTypes(clock, MIN15_DELTA_A_PLUS_KWH, MIN15_BULK_A_PLUS_KWH);

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>15-Minute Delta A+ kWh</td><td>15-Minute Bulk A+ kWh</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td></tr>
     * <tr><td>2017-04-01 00:15</td><td>-</td><td>100</td></tr>
     * <tr><td>2017-04-01 00:30</td><td>200</td><td>300</td></tr>
     * <tr><td>2017-04-01 00:45</td><td>200</td><td>500</td></tr>
     * <tr><td>2017-04-01 01:00</td><td>200</td><td>700</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void allIntervalReadingRecordsInShortIntervalAreAvailableValidCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime.minusSeconds(1));

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(MIN15_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(15, ChronoUnit.MINUTES), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(30, ChronoUnit.MINUTES), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(45, ChronoUnit.MINUTES), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(60, ChronoUnit.MINUTES), new BigDecimal(700))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(60, ChronoUnit.MINUTES));
        ReadingType validatedReadingType = getReadingType(MIN15_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsOnly(
                MapEntry.entry(startTime.plus(15, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(30, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(45, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(60, ChronoUnit.MINUTES), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>15-Minute Delta A+ kWh</td><td>15-Minute Bulk A+ kWh</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td></tr>
     * <tr><td>2017-04-01 00:15</td><td>-</td><td>100</td></tr>
     * <tr><td>2017-04-01 00:30</td><td>200</td><td>300</td></tr>
     * <tr><td>2017-04-01 00:45</td><td>200</td><td>500</td></tr>
     * <tr><td>2017-04-01 01:00</td><td>200</td><td>700</td></tr>
     * ...
     * <tr><td>2017-04-01 03:00</td><td>200</td><td>2300</td></tr>
     * <tr><td>2017-04-01 03:15</td><td>200</td><td>2500</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void allIntervalReadingRecordsInLongIntervalAreAvailableValidCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime.minusSeconds(1));

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(MIN15_BULK_A_PLUS_KWH);
        Instant recordTimeStamp = startTime;
        long bulkValue = 100;
        while (recordTimeStamp.compareTo(startTime.plus(3, ChronoUnit.HOURS)) <= 0) {
            recordTimeStamp = recordTimeStamp.plus(15, ChronoUnit.MINUTES);
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(recordTimeStamp, new BigDecimal(bulkValue)));
            bulkValue += 200;
        }
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithShortMaxPeriod();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(3, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES));
        ReadingType validatedReadingType = getReadingType(MIN15_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsOnly(
                MapEntry.entry(startTime.plus(15, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(30, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(45, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(60, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(75, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(90, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(105, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(120, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(135, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(150, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(165, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(180, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(195, ChronoUnit.MINUTES), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>15-Minute Delta A+ kWh</td><td>15-Minute Bulk A+ kWh</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td></tr>
     * <tr><td>2017-04-01 00:15</td><td>-</td><td>100</td></tr>
     * <tr><td>2017-04-01 00:30</td><td>200</td><td>300</td></tr>
     * <tr><td>2017-04-01 00:45</td><td>200</td><td>500</td></tr>
     * <tr><td>2017-04-01 01:00</td><td>200</td><td>700</td></tr>
     * ...
     * <tr><td>2017-04-01 03:00</td><td>200</td><td>2300</td></tr>
     * <tr><td>2017-04-01 03:15</td><td>200</td><td>2500</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void allIntervalReadingRecordsInZerosIntervalAreAvailableSuspectCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime.minusSeconds(1));

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(MIN15_BULK_A_PLUS_KWH);
        Instant recordTimeStamp = startTime;
        long bulkValue = 100;
        while (recordTimeStamp.compareTo(startTime.plus(3, ChronoUnit.HOURS)) < 0) {
            recordTimeStamp = recordTimeStamp.plus(15, ChronoUnit.MINUTES);
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(recordTimeStamp, new BigDecimal(bulkValue)));
            bulkValue += 200;
        }
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(3, ChronoUnit.HOURS));
        ReadingType validatedReadingType = getReadingType(MIN15_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsOnly(
                MapEntry.entry(startTime.plus(15, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(30, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(45, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(60, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(75, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(90, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(105, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(120, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(135, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(150, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(165, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(180, ChronoUnit.MINUTES), ValidationResult.SUSPECT)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>15-Minute Delta A+ kWh</td><td>15-Minute Bulk A+ kWh</td><td>quality status</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td><td>-</td></tr>
     * <tr><td>2017-04-01 00:15</td><td>-</td><td>100</td><td>Valid</td></tr>
     * <tr><td>2017-04-01 00:30</td><td>200</td><td>300</td><td>Valid</td></tr></tr>
     * <tr><td>2017-04-01 00:45</td><td>200</td><td>500</td><td>Valid</td></tr></tr>
     * <tr><td>2017-04-01 01:00</td><td>200</td><td>700</td><td>Valid</td></tr></tr>
     * ...
     * <tr><td>2017-04-01 03:00</td><td>200</td><td>2300</td></tr>
     * <tr><td>2017-04-01 03:15</td><td>200</td><td>2500</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void intervalReadingRecordsInRetroactivelyPeriodCanBecomeSuspect() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime.minusSeconds(1));

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(MIN15_BULK_A_PLUS_KWH);
        Instant recordTimeStamp = startTime;
        long bulkValue = 100;
        while (recordTimeStamp.compareTo(startTime.plus(3, ChronoUnit.HOURS)) < 0) {
            recordTimeStamp = recordTimeStamp.plus(15, ChronoUnit.MINUTES);
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(recordTimeStamp, new BigDecimal(bulkValue)));
            bulkValue += 200;
        }
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize retroactively period
        Validator validator = createValidatorWithDefaultProperties();
        Instant lastCheck = startTime.plus(1, ChronoUnit.HOURS);
        Range<Instant> interval = Range.openClosed(startTime, lastCheck);
        ReadingType validatedReadingType = getReadingType(MIN15_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsOnly(
                MapEntry.entry(startTime.plus(15, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(30, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(45, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(60, ChronoUnit.MINUTES), ValidationResult.VALID)
        );

        // Initialize validator
        validator = createValidatorWithCheckRetroactively();
        interval = Range.openClosed(lastCheck, lastCheck.plus(2, ChronoUnit.HOURS));

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Range<Instant> intervalWithRetroactivelyPeriod = Range.openClosed(startTime, lastCheck.plus(2, ChronoUnit.HOURS));
        validationResults = channel.getIntervalReadings(intervalWithRetroactivelyPeriod).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsOnly(
                MapEntry.entry(startTime.plus(15, ChronoUnit.MINUTES), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(30, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(45, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(60, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(75, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(90, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(105, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(120, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(135, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(150, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(165, ChronoUnit.MINUTES), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(180, ChronoUnit.MINUTES), ValidationResult.SUSPECT)
        );
    }

    private Validator createValidatorWithDefaultProperties() {
        Map<String, Object> properties = ImmutableMap.of(
                ConsecutiveZerosValidator.MINIMUM_PERIOD, new TimeDuration(2, TimeDuration.TimeUnit.HOURS),
                ConsecutiveZerosValidator.MAXIMUM_PERIOD, new TimeDuration(1, TimeDuration.TimeUnit.DAYS),
                ConsecutiveZerosValidator.MINIMUM_THRESHOLD, new BigDecimal(300.0),
                ConsecutiveZerosValidator.CHECK_RETROACTIVELY, false
        );
        return new ConsecutiveZerosValidator(NlsModule.FakeThesaurus.INSTANCE, new PropertySpecServiceImpl(), properties);
    }

    private Validator createValidatorWithShortMaxPeriod() {
        Map<String, Object> properties = ImmutableMap.of(
                ConsecutiveZerosValidator.MINIMUM_PERIOD, new TimeDuration(2, TimeDuration.TimeUnit.HOURS),
                ConsecutiveZerosValidator.MAXIMUM_PERIOD, new TimeDuration(3, TimeDuration.TimeUnit.HOURS),
                ConsecutiveZerosValidator.MINIMUM_THRESHOLD, new BigDecimal(300.0),
                ConsecutiveZerosValidator.CHECK_RETROACTIVELY, false
        );
        return new ConsecutiveZerosValidator(NlsModule.FakeThesaurus.INSTANCE, new PropertySpecServiceImpl(), properties);
    }

    private Validator createValidatorWithCheckRetroactively() {
        Map<String, Object> properties = ImmutableMap.of(
                ConsecutiveZerosValidator.MINIMUM_PERIOD, new TimeDuration(2, TimeDuration.TimeUnit.HOURS),
                ConsecutiveZerosValidator.MAXIMUM_PERIOD, new TimeDuration(1, TimeDuration.TimeUnit.DAYS),
                ConsecutiveZerosValidator.MINIMUM_THRESHOLD, new BigDecimal(300.0),
                ConsecutiveZerosValidator.CHECK_RETROACTIVELY, true
        );
        return new ConsecutiveZerosValidator(NlsModule.FakeThesaurus.INSTANCE, new PropertySpecServiceImpl(), properties);
    }

    private Meter createAndActivateMeterWithChannels(Instant activationTime) {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = amrSystem.newMeter(METER_NAME, METER_NAME).create();
        MeterActivation meterActivation = meter.activate(activationTime);
        ChannelsContainer channelsContainer = meterActivation.getChannelsContainer();
        channelsContainer.createChannel(getReadingType(MIN15_BULK_A_PLUS_KWH));
        return meter;
    }

    private ReadingType getReadingType(String mrid) {
        return inMemoryBootstrapModule.getMeteringService().getReadingType(mrid)
                .orElseThrow(() -> new IllegalArgumentException("No such reading type with mrid: " + mrid));
    }
}
