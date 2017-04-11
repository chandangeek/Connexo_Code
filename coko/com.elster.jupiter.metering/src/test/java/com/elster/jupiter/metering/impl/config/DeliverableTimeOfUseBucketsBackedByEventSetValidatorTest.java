/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeliverableTimeOfUseBucketsBackedByEventSetValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-20 (14:34)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeliverableTimeOfUseBucketsBackedByEventSetValidatorTest {

    private static final int PEAK_CODE = 3;
    private static final int OFF_PEAK_CODE = 5;

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DataModel dataModel;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private EventService eventService;
    @Mock
    private CustomPropertySetService cpsService;
    @Mock
    private Clock clock;
    @Mock
    private Publisher publisher;
    @Mock
    private ReadingType readingTypeWithTimeOfUse;
    @Mock
    private ReadingType readingTypeWithoutTimeOfUse;
    @Mock
    private ReadingTypeDeliverable deliverableWithThinTimeOfUse;
    @Mock
    private ReadingTypeDeliverable deliverableWithThickTimeOfUse;
    @Mock
    private ReadingTypeDeliverable deliverableWithoutTimeOfUse;
    @Mock
    private Formula formulaWithConstants;
    @Mock
    private Formula formulaWithThickTimeOfUseRequirements;
    @Mock
    private ServerMetrologyContract metrologyContract;
    @Mock
    private EventSetOnMetrologyConfiguration eventSetOnMetrologyConfiguration;
    @Mock
    private EventSet eventSet;
    @Mock
    private Event peak;
    @Mock
    private Event offpeak;
    @Mock
    private Thesaurus thesaurus;

    private MetrologyConfigurationImpl metrologyConfiguration;

    @Before
    public void setup() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        this.metrologyConfiguration = new MetrologyConfigurationImpl(this.dataModel, this.metrologyConfigurationService, this.eventService, this.clock, this.publisher);

        ConstantNode constantNode = new ConstantNodeImpl(BigDecimal.ONE);
        when(this.formulaWithConstants.getExpressionNode()).thenReturn(constantNode);
        when(this.deliverableWithThinTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithTimeOfUse);
        when(this.deliverableWithThinTimeOfUse.getFormula()).thenReturn(this.formulaWithConstants);
        when(this.deliverableWithoutTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithoutTimeOfUse);
        when(this.deliverableWithoutTimeOfUse.getFormula()).thenReturn(this.formulaWithConstants);
        ReadingTypeRequirement peakRequirement = mock(ReadingTypeRequirement.class);
        when(peakRequirement.getTou()).thenReturn(PEAK_CODE);
        ReadingTypeRequirementNode requirementNode = new ReadingTypeRequirementNodeImpl(peakRequirement);
        when(this.formulaWithThickTimeOfUseRequirements.getExpressionNode()).thenReturn(requirementNode);
        when(this.deliverableWithThickTimeOfUse.getFormula()).thenReturn(this.formulaWithThickTimeOfUseRequirements);
        when(this.deliverableWithThickTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithTimeOfUse);
        when(this.eventSetOnMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(this.metrologyConfiguration);
        when(this.eventSetOnMetrologyConfiguration.getEventSet()).thenReturn(this.eventSet);
        when(this.peak.getCode()).thenReturn((long) PEAK_CODE);
        when(this.offpeak.getCode()).thenReturn((long) OFF_PEAK_CODE);
    }

    @Before
    public void initializeReadingTypeWithTimeOfUse() {
        this.initializeReadingType(this.readingTypeWithTimeOfUse, PEAK_CODE);
    }

    @Before
    public void initializeReadingTypeWithoutTimeOfUse() {
        this.initializeReadingType(this.readingTypeWithoutTimeOfUse, 0);
    }

    private void initializeReadingType(ReadingType readingType, int tou) {
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(readingType.getTou()).thenReturn(tou);
        when(readingType.getMRID()).thenReturn("0.0.2.1.4.2.12.0.0.0.0." + tou + ".0.0.0.3.72.0");
    }

    @Test
    public void noEventSetsNoDeliverablesDoesNotProduceConstraintViolations() {
        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void noEventSetsNoDeliverablesWithTimeOfUseDoesNotProduceConstraintViolations() {
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void noEventSetsButOneDeliverableWithThickTimeOfUseDoesNotProducesConstraintViolations() {
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithThickTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    @ExpectedConstraintViolation(property = "deliverables[0.0.2.1.4.2.12.0.0.0.0.3.0.0.0.3.72.0].tou", messageId = "Translation not supported in unit testing", strict = true)
    public void noEventSetsButOneDeliverableWithTimeOfUseProducesOneConstraintViolation() {
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithThinTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 1);

        // Asserts: see expected constraint violation rule
        this.thesaurus.getFormat(PrivateMessageSeeds.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "Translation not supported in unit testing", strict = false)
    public void noEventSetsButMultipleDeliverablesWithTimeOfUseProducesMultipleConstraintViolations() {
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        this.initializeReadingType(readingType, 5);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        when(readingTypeDeliverable.getFormula()).thenReturn(this.formulaWithConstants);
        when(this.metrologyContract.getDeliverables()).thenReturn(Arrays.asList(this.deliverableWithThinTimeOfUse, readingTypeDeliverable));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 2);

        // Asserts: see expected constraint violation rule
        this.thesaurus.getFormat(PrivateMessageSeeds.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET);
    }

    @Test
    public void oneEventsSetButNoDeliverablesWithTimeOfUseDoesNotProduceConstraintViolations() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void oneEventSetsButNoDeliverablesWithTimeOfUseDoesNotProduceConstraintViolations() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void oneEventSetsAndOneCompatibleDeliverableWithTimeOfUseDoesNotProducesConstraintViolations() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);
        when(this.eventSet.getEvents()).thenReturn(Arrays.asList(this.peak, this.offpeak));
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithThinTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 0);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(property = "deliverables[0.0.2.1.4.2.12.0.0.0.0.33.0.0.0.3.72.0].tou", messageId = "Translation not supported in unit testing", strict = true)
    public void oneEventSetsButOneDeliverableWithIncompatibleTimeOfUseProducesOneConstraintViolation() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);
        when(this.eventSet.getEvents()).thenReturn(Arrays.asList(this.peak, this.offpeak));
        this.initializeReadingType(this.readingTypeWithTimeOfUse, 33);
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithThinTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 1);

        // Asserts: see expected constraint violation rule
        this.thesaurus.getFormat(PrivateMessageSeeds.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET);
    }

    private void testValidation(MetrologyConfiguration configuration) {
        this.testValidation(configuration, 0);
    }

    private void testValidation(MetrologyConfiguration configuration, int expectedViolations) {
        Set<ConstraintViolation<MetrologyConfiguration>> failures = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(new ConstraintValidatorFactoryImpl())
                .buildValidatorFactory()
                .getValidator()
                .validate(configuration, MetrologyConfigurationImpl.Activation.class);
        assertThat(failures).hasSize(expectedViolations);
        if (!failures.isEmpty()) {
            throw new VerboseConstraintViolationException(failures);
        }
    }

    private class ConstraintValidatorFactoryImpl implements ConstraintValidatorFactory {

        @Override
        public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> aClass) {
            if (aClass.equals(DeliverableTimeOfUseBucketsBackedByEventSetValidator.class)) {
                return (T) new DeliverableTimeOfUseBucketsBackedByEventSetValidator(thesaurus);
            } else {
                return null;
            }
        }

        @Override
        public void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {
        }
    }
}