/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.assertj.core.api.AssertionsForClassTypes;
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
public class MeterConfigurationIT {

    public static final String MULTIPLIER_TYPE_NAME = "Pulse";
    public static final BigDecimal VALUE = BigDecimal.valueOf(2, 0);
    private static final ZonedDateTime ACTIVE_DATE = ZonedDateTime.of(2014, 4, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END_DATE = ZonedDateTime.of(2014, 8, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0");

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private Meter meter;
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
        createAndActivateMeter();
        createMultiplierType();

        MeterConfiguration meterConfiguration;

        meterConfiguration = meter
                .startingConfigurationOn(ACTIVE_DATE.toInstant())
                .endingAt(END_DATE.toInstant())
                .configureReadingType(secondaryMetered)
                .withOverflowValue(BigDecimal.valueOf(15))
                .withNumberOfFractionDigits(3)
                .withMultiplierOfType(multiplierType)
                .calculating(primaryMetered)
                .create();


        assertThat(meter.getConfiguration(ACTIVE_DATE.toInstant())).contains(meterConfiguration);

        meter = inMemoryBootstrapModule.getMeteringService().findMeterById(meter.getId()).get();
        meterConfiguration = meter.getConfiguration(ACTIVE_DATE.toInstant()).get();

        assertThat(meterConfiguration.getRange()).isEqualTo(Range.closedOpen(ACTIVE_DATE.toInstant(), END_DATE.toInstant()));
        assertThat(meterConfiguration.getReadingTypeConfigs()).hasSize(1);

        MeterReadingTypeConfiguration meterReadingTypeConfiguration = meterConfiguration.getReadingTypeConfigs().get(0);

        assertThat(meterReadingTypeConfiguration.getMeasured()).isEqualTo(secondaryMetered);
        assertThat(meterReadingTypeConfiguration.getCalculated()).contains(primaryMetered);
        assertThat(meterReadingTypeConfiguration.getMultiplierType()).isEqualTo(multiplierType);
        assertThat(meterReadingTypeConfiguration.getOverflowValue()).hasValue(BigDecimal.valueOf(15));
        assertThat(meterReadingTypeConfiguration.getNumberOfFractionDigits()).hasValue(3);
    }

    @Test
    @Transactional
    public void testEndConfiguration() {
        createAndActivateMeter();
        createMultiplierType();

        MeterConfiguration meterConfiguration = meter
                .startingConfigurationOn(ACTIVE_DATE.toInstant())
                .configureReadingType(secondaryMetered)
                .withOverflowValue(BigDecimal.valueOf(15))
                .withNumberOfFractionDigits(3)
                .withMultiplierOfType(multiplierType)
                .calculating(primaryMetered)
                .create();


        assertThat(meter.getConfiguration(ACTIVE_DATE.toInstant())).contains(meterConfiguration);

        meter = inMemoryBootstrapModule.getMeteringService().findMeterById(meter.getId()).get();
        meterConfiguration = meter.getConfiguration(ACTIVE_DATE.toInstant()).get();
        meterConfiguration.endAt(END_DATE.toInstant());

        meter = inMemoryBootstrapModule.getMeteringService().findMeterById(meter.getId()).get();
        meterConfiguration = meter.getConfiguration(ACTIVE_DATE.toInstant()).get();

        Range<Instant> range = meterConfiguration.getRange();
        assertThat(range.hasUpperBound());
        assertThat(range.upperEndpoint()).isEqualTo(END_DATE.toInstant());
    }

    @Test
    @Transactional
    public void testSetMultiplier() {
        createAndActivateMeter();
        createMultiplierType();

        meterActivation.setMultiplier(multiplierType, VALUE);

        AssertionsForClassTypes.assertThat(meterActivation.getMultiplier(multiplierType)).contains(VALUE);
    }

    private void createMultiplierType() {
        multiplierType = inMemoryBootstrapModule.getMeteringService().createMultiplierType(MULTIPLIER_TYPE_NAME);
    }

    private void createAndActivateMeter() {
        meter = inMemoryBootstrapModule.getMeteringService().findAmrSystem(1).get()
                .newMeter("amrID", "myName")
                .create();
        meterActivation = meter.activate(ACTIVE_DATE.toInstant());
        meter = inMemoryBootstrapModule.getMeteringService().findMeterById(meter.getId()).get();
        meterActivation = meter.getMeterActivations().get(0);
    }
}
