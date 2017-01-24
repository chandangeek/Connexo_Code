package com.elster.jupiter.metering;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;

import java.time.Instant;
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
public class MeterTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

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
    public void testCOPL494() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Meter meter;
        Instant installDate1 = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Instant deactivateDate = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Instant installDate2 = ZonedDateTime.of(2015, 4, 12, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter("myMeter", "myName").create();
        assertThat(meter.getMeterActivations()).isEmpty();
        meter.activate(installDate1);
        JupiterAssertions.assertThat(meter.getCurrentMeterActivation()).isPresent();
        assertThat(meter.getCurrentMeterActivation().get().getStart()).isEqualTo(installDate1);
        assertThat(meter.getCurrentMeterActivation().get().getEnd()).isNull();

        meter.getCurrentMeterActivation().get().endAt(deactivateDate);
        JupiterAssertions.assertThat(meter.getCurrentMeterActivation()).isEmpty();
        assertThat(meter.getMeterActivations()).hasSize(1);
        assertThat(meter.getMeterActivations().get(0).getStart()).isEqualTo(installDate1);
        assertThat(meter.getMeterActivations().get(0).getEnd()).isEqualTo(deactivateDate);

        meter.activate(installDate2);
        JupiterAssertions.assertThat(meter.getCurrentMeterActivation()).isPresent();
        assertThat(meter.getCurrentMeterActivation().get().getStart()).isEqualTo(installDate2);
        assertThat(meter.getCurrentMeterActivation().get().getEnd()).isNull();
        assertThat(meter.getMeterActivations()).hasSize(2);
        assertThat(meter.getMeterActivations().get(0).getStart()).isEqualTo(installDate1);
        assertThat(meter.getMeterActivations().get(0).getEnd()).isEqualTo(deactivateDate);
        assertThat(meter.getMeterActivations().get(1)).isEqualTo(meter.getCurrentMeterActivation().get());
    }
}
