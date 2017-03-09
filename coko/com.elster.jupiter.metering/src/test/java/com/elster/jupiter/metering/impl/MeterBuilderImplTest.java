package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterBuilder;
import com.elster.jupiter.metering.MeteringService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 27/01/2017
 * Time: 13:53
 */
public class MeterBuilderImplTest {

    private static Clock clock = mock(Clock.class);
    private static MeteringInMemoryBootstrapModule inMemoryPersistentModule =
            MeteringInMemoryBootstrapModule.withClockAndReadingTypes(clock, "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryPersistentModule.getTransactionService());

    @BeforeClass
    public static void beforeClass() {
        when(clock.instant()).thenAnswer(invocationOnMock -> Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        inMemoryPersistentModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryPersistentModule.deactivate();
    }

    @Test
    @Transactional
    public void createMeterUsingMeterBuilder() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();

        MeterBuilder meterBuilder = amrSystem.newMeter("ABCD", "EFGHIJKLMNO");
        meterBuilder.setManufacturer("MANUFACTURER");
        meterBuilder.setModelNumber("MODELNUMBER");
        meterBuilder.setModelVersion("MODELVERSION");

        Meter meter = meterBuilder.create();
        assertThat(meter.getManufacturer()).isEqualTo("MANUFACTURER");
        assertThat(meter.getModelNumber()).isEqualTo("MODELNUMBER");
        assertThat(meter.getModelVersion()).isEqualTo("MODELVERSION");
    }
}
