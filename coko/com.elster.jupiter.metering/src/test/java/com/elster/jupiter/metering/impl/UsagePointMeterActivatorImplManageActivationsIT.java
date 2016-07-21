package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointMeterActivatorImplManageActivationsIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();
    private static Instant INSTALLATION_TIME = ZonedDateTime.of(2016, 7, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static Instant THREE_DAYS_BEFORE = INSTALLATION_TIME.minus(3, ChronoUnit.DAYS);
    private static Instant TWO_DAYS_BEFORE = INSTALLATION_TIME.minus(2, ChronoUnit.DAYS);
    private static Instant ONE_DAY_BEFORE = INSTALLATION_TIME.minus(1, ChronoUnit.DAYS);
    private static Instant ONE_DAY_AFTER = INSTALLATION_TIME.plus(1, ChronoUnit.DAYS);
    private static Instant TWO_DAYS_AFTER = INSTALLATION_TIME.plus(2, ChronoUnit.DAYS);
    private static Instant THREE_DAYS_AFTER = INSTALLATION_TIME.plus(3, ChronoUnit.DAYS);

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private static Meter meter;
    private static MeterRole meterRole;
    private static UsagePoint usagePoint;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            meter = system.newMeter("Meter").create();
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("UsagePoint", INSTALLATION_TIME).create();
            meterRole = inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
            context.commit();
        }
    }

    private static void reloadObjects() {
        meter = inMemoryBootstrapModule.getMeteringService().findMeter(meter.getId()).get();
        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePoint(usagePoint.getId()).get();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @After
    public void afterTest() {
        reloadObjects();
    }

    @Test
    @Transactional
    public void testHasNoActivations() {
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }
}
