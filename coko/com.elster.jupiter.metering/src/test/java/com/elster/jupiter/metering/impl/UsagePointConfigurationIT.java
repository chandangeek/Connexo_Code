package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.metering.config.DefaultMeterRole;

import com.google.common.collect.Range;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConfigurationIT {
    public static final String MULTIPLIER_TYPE_NAME = "Pulse";
    private static final ZonedDateTime ACTIVE_DATE = ZonedDateTime.of(2014, 4, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END_DATE = ZonedDateTime.of(2014, 8, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(
            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",  // no macro period, measuring period =  15 min, secondary metered
            "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0"  // no macro period, measuring period =  15 min, primary metered
    );

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private UsagePoint usagePoint;
    private MeterActivation meterActivation;
    private MultiplierType multiplierType;
    private ReadingType secondaryMetered;
    private ReadingType primaryMetered;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void before() throws SQLException {
        secondaryMetered = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        primaryMetered = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
    }

    @Test
    @Transactional
    public void testCreateConfiguration() {
        createAndActivateUsagePoint();
        createMultiplierType();

        UsagePointConfiguration usagePointConfiguration;
            usagePointConfiguration = usagePoint
                    .startingConfigurationOn(ACTIVE_DATE.toInstant())
                    .endingAt(END_DATE.toInstant())
                    .configureReadingType(secondaryMetered)
                    .withMultiplierOfType(multiplierType)
                    .calculating(primaryMetered)
                    .create();

        assertThat(usagePoint.getConfiguration(ACTIVE_DATE.toInstant())).contains(usagePointConfiguration);

        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
        usagePointConfiguration = usagePoint.getConfiguration(ACTIVE_DATE.toInstant()).get();

        assertThat(usagePointConfiguration.getRange()).isEqualTo(Range.closedOpen(ACTIVE_DATE.toInstant(), END_DATE.toInstant()));
        assertThat(usagePointConfiguration.getReadingTypeConfigs()).hasSize(1);

        UsagePointReadingTypeConfiguration usagePointReadingTypeConfiguration = usagePointConfiguration.getReadingTypeConfigs().get(0);

        assertThat(usagePointReadingTypeConfiguration.getMeasured()).isEqualTo(secondaryMetered);
        assertThat(usagePointReadingTypeConfiguration.getCalculated()).contains(primaryMetered);
        assertThat(usagePointReadingTypeConfiguration.getMultiplierType()).isEqualTo(multiplierType);
    }

    @Test
    @Transactional
    public void testEndConfiguration() {
        createAndActivateUsagePoint();
        createMultiplierType();

        UsagePointConfiguration usagePointConfiguration;
        usagePointConfiguration = usagePoint
                .startingConfigurationOn(ACTIVE_DATE.toInstant())
                .configureReadingType(secondaryMetered)
                .withMultiplierOfType(multiplierType)
                .calculating(primaryMetered)
                .create();

        assertThat(usagePoint.getConfiguration(ACTIVE_DATE.toInstant())).contains(usagePointConfiguration);

        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
        usagePointConfiguration = usagePoint.getConfiguration(ACTIVE_DATE.toInstant()).get();

        usagePointConfiguration.endAt(END_DATE.toInstant());
        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
        usagePointConfiguration = usagePoint.getConfiguration(ACTIVE_DATE.toInstant()).get();

        Range<Instant> range = usagePointConfiguration.getRange();
        assertThat(range.hasUpperBound());
        assertThat(range.upperEndpoint()).isEqualTo(END_DATE.toInstant());
    }

    private void createMultiplierType() {
        multiplierType = inMemoryBootstrapModule.getMeteringService().createMultiplierType(MULTIPLIER_TYPE_NAME);
    }

    private void createAndActivateUsagePoint() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        usagePoint = electricity.newUsagePoint("mrId", Instant.EPOCH).create();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("meter", "myName").create();
        meterActivation = usagePoint.activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findDefaultMeterRole(DefaultMeterRole.DEFAULT), ACTIVE_DATE.toInstant());
        usagePoint = meteringService.findUsagePointById(usagePoint.getId()).get();
        meterActivation = usagePoint.getMeterActivations().get(0);
    }
}
