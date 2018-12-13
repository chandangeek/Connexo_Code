/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingQualityLifeCycleTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
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
    public void test() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Meter meter;
        String readingTypeCode;
        ZonedDateTime dateTime = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault());

        //step 1
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter("myMeter", "myName").create();
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
        readingTypeCode = builder.code();

        //step 2
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        for (Cases testCase : Cases.values()) {
            Instant date = dateTime.plusMinutes(testCase.ordinal()).toInstant();
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), date);
            reading.addQuality("1.1.1", "Same");
            meterReading.addReading(reading);
        }
        meter.store(QualityCodeSystem.MDC, meterReading);
        assertThat(meter.getReadingQualities(Range.atLeast(Instant.EPOCH))).hasSize(Cases.values().length);

        //step 3
        meterReading = MeterReadingImpl.newInstance();
        for (Cases testCase : Cases.values()) {
            Instant date = dateTime.plusMinutes(testCase.ordinal()).toInstant();
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(testCase.sameReading ? 1 : 2), date);
            switch (testCase.readingQualityBehavior) {
                case SAME:
                    reading.addQuality("1.1.1", "Same");
                    break;
                case DIFFERENT:
                    reading.addQuality("1.1.2", "Different");
                    break;
                case NONE:
                    break;
            }
            meterReading.addReading(reading);
        }
        meter.store(QualityCodeSystem.MDC, meterReading);
        List<? extends BaseReading> readings = meter.getReadings(
                Range.atLeast(Instant.EPOCH),
                meteringService.getReadingType(readingTypeCode).get());
        assertThat(readings).hasSize(Cases.values().length);
        for (int i = 0; i < Cases.values().length; i++) {
            BaseReading reading = readings.get(i);
            Cases testCase = Cases.values()[i];
            switch (testCase.readingQualityBehavior) {
                case SAME:
                    assertThat(reading.getReadingQualities()).hasSize(1);
                    assertThat(reading.getReadingQualities().get(0).getComment()).isEqualTo("Same");
                    break;
                case DIFFERENT:
                    assertThat(reading.getReadingQualities()).hasSize(1);
                    assertThat(reading.getReadingQualities().get(0).getComment()).isEqualTo("Different");
                    break;
                case NONE:
                    assertThat(reading.getReadingQualities()).isEmpty();
                    break;
            }
        }
        assertThat(meter.getReadingQualities(Range.atLeast(Instant.EPOCH))).
                hasSize((int) Arrays.stream(Cases.values())
                        .filter(testCase -> testCase.readingQualityBehavior != ReadingQualityBehavior.NONE)
                        .count());
    }

    private enum ReadingQualityBehavior {
        SAME,
        DIFFERENT,
        NONE
    }

    private enum Cases {
        SAMEREADINGSAMEREADINGQUALITY(true, ReadingQualityBehavior.SAME),
        SAMEREADINGDIFFERENTREADINGQUALITY(true, ReadingQualityBehavior.DIFFERENT),
        SAMEREADINGNOREADINGQUALITY(true, ReadingQualityBehavior.NONE),
        DIFFERENTREADINGSAMEREADINGQUALITY(false, ReadingQualityBehavior.SAME),
        DIFFERENTREADINGDIFFERENTREADINGQUALITY(false, ReadingQualityBehavior.DIFFERENT),
        DIFFERENTREADINGNOREADINGQUALITY(false, ReadingQualityBehavior.NONE),;

        private boolean sameReading;
        private ReadingQualityBehavior readingQualityBehavior;

        Cases(boolean sameReading, ReadingQualityBehavior readingQualityBehavior) {
            this.sameReading = sameReading;
            this.readingQualityBehavior = readingQualityBehavior;
        }
    }

}
