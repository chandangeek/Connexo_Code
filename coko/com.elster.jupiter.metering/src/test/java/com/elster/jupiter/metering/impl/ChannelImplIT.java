package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link MeterImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (11:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class ChannelImplIT {

    private static final String BULK = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final ZonedDateTime ACTIVATION = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static AtomicLong offsets = new AtomicLong(5);
    private static Clock clock = new ProgrammableClock(TimeZoneNeutral.getMcMurdo(), () -> ACTIVATION.plusDays(offsets.getAndIncrement()).toInstant());
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withClockAndReadingTypes(clock, BULK, DELTA);

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
    public void createEndDeviceWithManagedState() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType bulkReadingType = meteringService.getReadingType(BULK).get();
        ReadingType deltaReadingType = meteringService.getReadingType(DELTA).get();
        Meter meter;

        meter = meteringService.findAmrSystem(1).get()
                .newMeter("amrID", "myName")
                .create();

        MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());
        Channel channel = meterActivation.getChannelsContainer().createChannel(bulkReadingType);
        assertThat((List<ReadingType>) channel.getReadingTypes()).contains(deltaReadingType);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(BULK);

        IntervalReading reading1 = IntervalReadingImpl.of(ACTIVATION.plusDays(3).toInstant(), BigDecimal.valueOf(5));
        IntervalReading reading2 = IntervalReadingImpl.of(ACTIVATION.plusDays(3).plusMinutes(15).toInstant(), BigDecimal.valueOf(7));
        intervalBlock.addAllIntervalReadings(Arrays.asList(reading1, reading2));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        Instant since = clock.instant();
        ChannelsContainer channelsContainer = meter.getChannelsContainers().get(0);
        channel = channelsContainer.getChannels().get(0);
        channel.getCimChannel(deltaReadingType)
                .get()
                .editReadings(QualityCodeSystem.MDC, Collections.singletonList(IntervalReadingImpl.of(ACTIVATION.plusDays(3).toInstant(), BigDecimal.valueOf(5))));

        channel = meter.getChannelsContainers().get(0).getChannels().get(0);
        List<BaseReadingRecord> bulkReadingsUpdatedSince = channel.getReadingsUpdatedSince(bulkReadingType, Range.all(), since);
        assertThat(bulkReadingsUpdatedSince).isEmpty();
        List<BaseReadingRecord> deltaReadingsUpdatedSince = channel.getReadingsUpdatedSince(deltaReadingType, Range.all(), since);
        assertThat(deltaReadingsUpdatedSince).hasSize(1);
    }
}
