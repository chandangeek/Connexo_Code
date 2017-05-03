/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

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
import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.NoneOrBigDecimal;
import com.elster.jupiter.properties.NoneOrTimeDurationValue;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validators.impl.ValidatorInMemoryBootstrapModule;
import com.elster.jupiter.validators.impl.properties.ReadingTypeReference;

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
public class MeterAdvanceValidatorIT {

    private static final String DAILY_DELTA_A_PLUS_KWH = "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DAILY_BULK_A_PLUS_KWH = "11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String BULK_A_PLUS_MWH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.6.72.0";

    private static final String METER_NAME = "SPE01";

    private static final Clock clock = Clock.system(ZoneId.of("Europe/Athens"));
    private static ValidatorInMemoryBootstrapModule inMemoryBootstrapModule = ValidatorInMemoryBootstrapModule
            .withClockAndReadingTypes(clock, DAILY_DELTA_A_PLUS_KWH, DAILY_BULK_A_PLUS_KWH, BULK_A_PLUS_MWH);

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
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td><td>0.0</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>0.5</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>-</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>100</td><td>1000</td><td>0.8</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void firstAndLastRegisterReadingsAreAvailableSuspectCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime.minusSeconds(1));

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(3, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.8), startTime.plus(6, ChronoUnit.DAYS))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(6, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.SUSPECT)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:01</td><td>-</td><td>-</td><td>0.1</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>0.5</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>0.9</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>100</td><td>1000</td><td>-</td></tr>
     * <tr><td>2017-04-07 00:01</td><td>-</td><td>-</td><td>1.0</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void firstAndLastRegisterReadingsAreAvailableValidCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime.plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(3, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.9), startTime.plus(5, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1.0), startTime.plus(6, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(6, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td><td>-</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:01</td><td>-</td><td>-</td><td>0.310</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>-</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>1.0</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>100</td><td>1000</td><td>-</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void firstRegisterReadingIsAfterFirstIntervalAndLastIsBeforeLastIntervalSuspectCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.310), startTime.plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1.0), startTime.plus(5, ChronoUnit.DAYS))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(6, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED)
        );
    }


    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>-</td><td>-</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:01</td><td>-</td><td>-</td><td>0.310</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>-</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>0.910</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>100</td><td>1000</td><td>-</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void firstRegisterReadingIsAfterFirstIntervalAndLastIsBeforeLastIntervalValidCase() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.310), startTime.plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.910), startTime.plus(5, ChronoUnit.DAYS))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(6, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:01</td><td>-</td><td>-</td><td>0.1</td></tr>
     * <tr><td>2017-04-01 00:02</td><td>-</td><td>-</td><td>0.5{must not be taken into account}</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>0.5</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>0.9</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>100</td><td>1000</td><td>-</td></tr>
     * <tr><td>2017-04-07 00:01</td><td>-</td><td>-</td><td>1.0</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void moreThanOneRegisterReadingIsAvailableForFirstInterval() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime.plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(2, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(3, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.9), startTime.plus(5, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1.0), startTime.plus(6, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(6, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:01</td><td>-</td><td>-</td><td>0.1</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>0.5</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>0.9</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>100</td><td>1000</td><td>1.0</td></tr>
     * <tr><td>2017-04-07 00:01</td><td>-</td><td>-</td><td>1000.0{should not be taken into account}</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void moreThanOneRegisterReadingIsAvailableForLastInterval() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime.plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(2, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(3, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.9), startTime.plus(5, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime.plus(6, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1000.0), startTime.plus(6, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(6, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-01-01 00:01</td><td>-</td><td>-</td><td>1000</td></tr>
     * <tr><td>2017-02-01 00:01</td><td>-</td><td>-</td><td>0.1</td></tr>
     * <tr><td>2017-02-01 00:02</td><td>-</td><td>-</td><td>1000{should not be taken into account}</td></tr>
     * <tr><td>2017-02-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-02-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-01 00:00</td><td>-</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>200</td><td>500</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:01</td><td>-</td><td>-</td><td>0.7</td></tr>
     * <tr><td>2017-04-03 00:02</td><td>-</td><td>-</td><td>1000{should not be taken into account}</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>900</td><td>-</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>100</td><td>1000</td><td>-</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void firstRegisterReadingIsBeforeStartAndLastOneIsBeforeEndOfValidatedInterval() {
        ZonedDateTime meterActivationTime = ZonedDateTime.of(LocalDateTime.of(2017, 1, 1, 0, 0, 0, 0), clock.getZone());
        Meter meter = createAndActivateMeterWithChannels(meterActivationTime.toInstant());

        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(meterActivationTime.plus(1, ChronoUnit.MONTHS).plus(1, ChronoUnit.DAYS).toInstant(), new BigDecimal(100)),
                IntervalReadingImpl.of(meterActivationTime.plus(1, ChronoUnit.MONTHS).plus(2, ChronoUnit.DAYS).toInstant(), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime, new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(1000))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1000.0), meterActivationTime.plus(1, ChronoUnit.MINUTES).toInstant()),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), meterActivationTime.plus(1, ChronoUnit.MONTHS).plus(1, ChronoUnit.MINUTES).toInstant()),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1000), meterActivationTime.plus(1, ChronoUnit.MONTHS).plus(2, ChronoUnit.MINUTES).toInstant()),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.7), startTime.plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1000.0), startTime.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.MINUTES))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(4, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-01-01 00:01</td><td>-</td><td>-</td><td>1000{should not be taken into account}</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>0.5</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>100</td><td>600</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>-</td><td>-</td><td>1000{should not be taken into account}</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void onlyOneRegisterReadingFound() {
        ZonedDateTime meterActivationTime = ZonedDateTime.of(LocalDateTime.of(2017, 1, 1, 0, 0, 0, 0), clock.getZone());
        Meter meter = createAndActivateMeterWithChannels(meterActivationTime.toInstant());

        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(600))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1000.0), meterActivationTime.plus(1, ChronoUnit.MINUTES).toInstant()),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.5), startTime.plus(3, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1000.0), startTime.plus(5, ChronoUnit.DAYS))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(4, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>-</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void noRegisterReadingsAreAvailableAndPeriodIsLessThanReferencePeriod() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(3, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        // All should be not validated because validated interval is less or equal than reference period (4 days set by parameter)
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.NOT_VALIDATED)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>-</td></tr>
     * <tr><td>2017-04-05 00:00</td><td>200</td><td>700</td><td>-</td></tr>
     * <tr><td>2017-04-06 00:00</td><td>200</td><td>900</td><td>-</td></tr>
     * <tr><td>2017-04-07 00:00</td><td>0</td><td>900</td><td>-</td></tr>
     * <tr><td>2017-04-08 00:00</td><td>0</td><td>900</td><td>-</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void noRegisterReadingsAreAvailableAndPeriodIsGreaterThanReferencePeriod() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500)),
                IntervalReadingImpl.of(startTime.plus(4, ChronoUnit.DAYS), new BigDecimal(700)),
                IntervalReadingImpl.of(startTime.plus(5, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(6, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(7, ChronoUnit.DAYS), new BigDecimal(900)),
                IntervalReadingImpl.of(startTime.plus(8, ChronoUnit.DAYS), new BigDecimal(900))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(8, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        // All should be valid because validated interval is greater than reference period (4 days set by parameter)
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(4, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(5, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(6, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(7, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(8, ChronoUnit.DAYS), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-01 00:01</td><td>-</td><td>-</td><td>0.1</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>-</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>0.3</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:01</td><td>-</td><td>-</td><td>0.10001</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void differenceBetweenRegisterReadingsIsLessThanMinThreshold() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime);

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime.plus(1, ChronoUnit.MINUTES)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.3), startTime.plus(2, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.10001), startTime.plus(3, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(3, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.VALID)
        );
    }

    /**
     * Test channels have the following readings:
     * <table border="1">
     * <tr><td>Timestamp</td><td>Daily Delta A+ kWh</td><td>Daily Bulk A+ kWh</td><td>Bulk A+ MWh</td></tr>
     * <tr><td>2017-04-02 00:00</td><td>-</td><td>100</td><td>0.1</td></tr>
     * <tr><td>2017-04-03 00:00</td><td>200</td><td>300</td><td>-</td></tr>
     * <tr><td>2017-04-04 00:00</td><td>200</td><td>500</td><td>1.0</td></tr>
     * </table>
     */
    @Test
    @Transactional
    public void firstRegisterReadingTimeMatchesFirstInterval() {
        Instant startTime = ZonedDateTime.of(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0), clock.getZone()).toInstant();
        Meter meter = createAndActivateMeterWithChannels(startTime.plusMillis(1));

        // Prepare readings for validation
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(DAILY_BULK_A_PLUS_KWH);
        intervalBlock.addAllIntervalReadings(Arrays.asList(
                IntervalReadingImpl.of(startTime.plus(1, ChronoUnit.DAYS), new BigDecimal(100)),
                IntervalReadingImpl.of(startTime.plus(2, ChronoUnit.DAYS), new BigDecimal(300)),
                IntervalReadingImpl.of(startTime.plus(3, ChronoUnit.DAYS), new BigDecimal(500))
        ));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addAllReadings(Arrays.asList(
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(0.1), startTime.plus(1, ChronoUnit.DAYS)),
                ReadingImpl.of(BULK_A_PLUS_MWH, new BigDecimal(1.0), startTime.plus(3, ChronoUnit.DAYS))
        ));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Initialize validator
        Validator validator = createValidatorWithDefaultProperties();
        Range<Instant> interval = Range.openClosed(startTime, startTime.plus(3, ChronoUnit.DAYS));
        ReadingType validatedReadingType = getReadingType(DAILY_DELTA_A_PLUS_KWH);
        Channel channel = meter.getChannelsContainers().get(0).getChannel(validatedReadingType).get();

        // Business method
        validator.init(channel, validatedReadingType, interval);
        Map<Instant, ValidationResult> validationResults = channel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, validator::validate));

        // Asserts
        assertThat(validationResults).containsExactly(
                MapEntry.entry(startTime.plus(1, ChronoUnit.DAYS), ValidationResult.VALID),
                MapEntry.entry(startTime.plus(2, ChronoUnit.DAYS), ValidationResult.SUSPECT),
                MapEntry.entry(startTime.plus(3, ChronoUnit.DAYS), ValidationResult.SUSPECT)
        );
    }

    private Validator createValidatorWithDefaultProperties() {
        Map<String, Object> properties = ImmutableMap.of(
                MeterAdvanceValidator.REFERENCE_READING_TYPE, new ReadingTypeValueFactory.ReadingTypeReference(getReadingType(BULK_A_PLUS_MWH)),
                MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE, new TwoValuesDifference(TwoValuesDifference.Type.ABSOLUTE, new BigDecimal(0.001)),
                MeterAdvanceValidator.REFERENCE_PERIOD, NoneOrTimeDurationValue.of(new TimeDuration(7, TimeDuration.TimeUnit.DAYS)),
                MeterAdvanceValidator.MIN_THRESHOLD, NoneOrBigDecimal.of(new BigDecimal(0.001))
        );
        return new MeterAdvanceValidator(NlsModule.FakeThesaurus.INSTANCE, new PropertySpecServiceImpl(), inMemoryBootstrapModule.getMeteringService(), properties);
    }

    private Meter createAndActivateMeterWithChannels(Instant activationTime) {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = amrSystem.newMeter(METER_NAME, METER_NAME).create();
        MeterActivation meterActivation = meter.activate(activationTime);
        ChannelsContainer channelsContainer = meterActivation.getChannelsContainer();
        channelsContainer.createChannel(getReadingType(DAILY_BULK_A_PLUS_KWH));
        channelsContainer.createChannel(getReadingType(BULK_A_PLUS_MWH));
        return meter;
    }

    private ReadingType getReadingType(String mrid) {
        return inMemoryBootstrapModule.getMeteringService().getReadingType(mrid)
                .orElseThrow(() -> new IllegalArgumentException("No such reading type with mrid: " + mrid));
    }
}
