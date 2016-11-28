package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CumulativeChannelTest {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Test
    @Transactional
    public void test() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myName").create();
        MeterActivation activation = meter.activate(ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        String readingTypeCode = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .code();
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        Channel channel = activation.getChannelsContainer().createChannel(readingType);
        assertThat(channel.getBulkQuantityReadingType().isPresent()).isTrue();
        ReadingStorer storer = meteringService.createOverrulingStorer();
        Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        storer.addReading(channel.getCimChannel(readingType).get(), IntervalReadingImpl.of(instant, BigDecimal.valueOf(1000)));
        storer.addReading(channel.getCimChannel(readingType).get(), IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(1100)));
        storer.execute(QualityCodeSystem.MDC);
        List<BaseReadingRecord> readings = channel.getReadings(Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)));
        assertThat(readings).hasSize(2);
        assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(readings.get(1).getQuantity(1).getValue()).isEqualTo(BigDecimal.valueOf(1100));
    }
}
