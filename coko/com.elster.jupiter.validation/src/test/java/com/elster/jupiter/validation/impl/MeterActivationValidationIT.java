/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationValidationIT {
    private static ValidationInMemoryBootstrapModule inMemoryBootstrapModule = new ValidationInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    private static MeteringService meteringService;
    private static ValidationServiceImpl validationService;
    private static IValidationRuleSet validationRuleSet;

    @Rule
    public TestRule timeZone = Using.timeZone("GMT");
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.get(TransactionService.class));

    @BeforeClass
    public static void setUp() throws SQLException {
        inMemoryBootstrapModule.activate();
        meteringService = inMemoryBootstrapModule.get(MeteringService.class);
        validationService = (ValidationServiceImpl) inMemoryBootstrapModule.get(ValidationService.class);

        ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
        Validator validator = mock(Validator.class);
        when(validatorFactory.available()).thenReturn(Collections.singletonList("autoPass"));
        when(validatorFactory.create("autoPass", Collections.emptyMap())).thenReturn(validator);
        when(validatorFactory.createTemplate("autoPass")).thenReturn(validator);
        when(validator.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.VALID);
        when(validator.getPropertySpecs()).thenReturn(Collections.emptyList());
        validationService.addValidatorFactory(validatorFactory);

        ValidationEventHandler validationEventHandler = new ValidationEventHandler();
        validationEventHandler.setValidationService(validationService);
        inMemoryBootstrapModule.get(Publisher.class)
                .addSubscriber(validationEventHandler);

        RangeSet<Instant> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.atLeast(Instant.EPOCH));
        validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
            @Override
            public Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext) {
                return Collections.singletonMap(validationRuleSet, rangeSet);
            }

            @Override
            public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
                return false;
            }

            @Override
            public boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceId) {
                return true;
            }

            @Override
            public boolean canHandleRuleSetStatus() {
                return true;
            }
        });
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testPersistence() {
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("1", "myName").create();
        MeterActivation meterActivation = meter.activate(ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant());
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        Channel channel = meterActivation.getChannelsContainer().createChannel(readingType);
        MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
        assertThat(loaded.getChannelsContainer().getChannels()).hasSize(1).contains(channel);
    }

    @Test
    @Transactional
    public void testAdvanceWithReadings() {
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        createRuleSet(readingType);
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("1", "myName").create();
        ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
        ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        MeterActivation meterActivation = meter.activate(startTime.toInstant());
        meterActivation.endAt(originalCutOff.toInstant());
        meterActivation.getChannelsContainer().createChannel(readingType);
        MeterActivation currentActivation = meter.activate(originalCutOff.toInstant());
        currentActivation.getChannelsContainer().createChannel(readingType);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.minusMinutes(15).toInstant(), BigDecimal.valueOf(4025, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.toInstant(), BigDecimal.valueOf(4175, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4225, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.toInstant(), BigDecimal.valueOf(4725, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4825, 2)));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);
        validationService.activateValidation(meter);
        validationService.validate(Collections.emptySet(), meterActivation.getChannelsContainer());
        validationService.validate(Collections.emptySet(), currentActivation.getChannelsContainer());

        currentActivation.advanceStartDate(newCutOff.toInstant());

        assertThat(meter.getMeterActivations()).hasSize(2);
        MeterActivation first = meter.getMeterActivations().get(0);
        MeterActivation second = meter.getMeterActivations().get(1);
        assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
        assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));

        Instant lastCheckedOfPrior = validationService.getPersistedChannelsContainerValidations(meterActivation.getChannelsContainer()).get(0)
                .getChannelValidation(meterActivation.getChannelsContainer().getChannels().get(0)).get()
                .getLastChecked();
        assertThat(lastCheckedOfPrior).isEqualTo(newCutOff.toInstant());
        Instant lastCheckedOfCurrent = validationService.getPersistedChannelsContainerValidations(currentActivation.getChannelsContainer()).get(0)
                .getChannelValidation(currentActivation.getChannelsContainer().getChannels().get(0)).get()
                .getLastChecked();
        assertThat(lastCheckedOfCurrent).isEqualTo(newCutOff.toInstant());
    }

    private void createRuleSet(ReadingType readingType) {
        validationRuleSet = (IValidationRuleSet) validationService.createValidationRuleSet("forTest", QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("First, Last and Always", Instant.EPOCH);
        validationRuleSetVersion.addRule(ValidationAction.FAIL, "autoPass", "autoPass")
                .withReadingType(readingType)
                .active(true).create();
    }
}
