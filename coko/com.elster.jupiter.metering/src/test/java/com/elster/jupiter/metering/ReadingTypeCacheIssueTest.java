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
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.transaction.TransactionContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeCacheIssueTest {

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

    public void test() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Meter meter;
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter", "myName").create();
            ctx.commit();
        }
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.AIR)
                .period(TimeAttribute.NOTAPPLICABLE)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
        String readingTypeCode = builder.code();
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            Instant instant = LocalDate.of(2014, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Reading reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1000), instant);
            meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(reading));
            //rollback
        }
        assertThat(meteringService.getReadingType(readingTypeCode).isPresent()).isFalse();
        meter = meteringService.findMeterById(meter.getId()).get(); // get fresh copy from DB
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
            Reading reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1000), instant);
            meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(reading));
            ctx.commit();
        }
        assertThat(meteringService.getReadingType(readingTypeCode).isPresent()).isTrue();
    }
}
