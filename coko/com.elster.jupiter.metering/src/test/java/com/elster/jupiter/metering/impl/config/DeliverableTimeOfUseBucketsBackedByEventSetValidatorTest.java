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
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Publisher;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
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
    private ReadingTypeDeliverable deliverableWithTimeOfUse;
    @Mock
    private ReadingTypeDeliverable deliverableWithoutTimeOfUse;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private EventSetOnMetrologyConfiguration eventSetOnMetrologyConfiguration;
    @Mock
    private EventSet eventSet;
    @Mock
    private Event peak;
    @Mock
    private Event offpeak;

    private MetrologyConfigurationImpl metrologyConfiguration;

    @Before
    public void setup() {
        this.metrologyConfiguration = new MetrologyConfigurationImpl(this.dataModel, this.metrologyConfigurationService, this.eventService, this.cpsService, this.clock, this.publisher);

        when(deliverableWithTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithTimeOfUse);
        when(deliverableWithoutTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithoutTimeOfUse);
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
    public void noEventsSetsNoDeliverablesDoesNotProduceConstraintViolations() {
        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void noEventsSetsNoDeliverablesWithTimeOfUseDoesNotProduceConstraintViolations() {
        this.metrologyConfiguration.doAddReadingTypeDeliverable(this.deliverableWithoutTimeOfUse);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    @ExpectedConstraintViolation(property = "deliverables[0.0.2.1.4.2.12.0.0.0.0.3.0.0.0.3.72.0].tou", messageId = "{" + MessageSeeds.Constants.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET + "}", strict = true)
    public void noEventsSetsButOneDeliverableWithTimeOfUseProducesOneConstraintViolation() {
        this.metrologyConfiguration.doAddReadingTypeDeliverable(this.deliverableWithTimeOfUse);
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 1);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET + "}", strict = false)
    public void noEventsSetsButMultipleDeliverablesWithTimeOfUseProducesMultipleConstraintViolations() {
        this.metrologyConfiguration.doAddReadingTypeDeliverable(this.deliverableWithTimeOfUse);
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        this.initializeReadingType(readingType, 5);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        this.metrologyConfiguration.doAddReadingTypeDeliverable(readingTypeDeliverable);
        when(this.metrologyContract.getDeliverables()).thenReturn(Arrays.asList(this.deliverableWithTimeOfUse, readingTypeDeliverable));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 2);

        // Asserts: see expected constraint violation rule
    }

    @Test
    public void oneEventsSetButNoDeliverablesWithTimeOfUseDoesNotProduceConstraintViolations() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void oneEventsSetsButNoDeliverablesWithTimeOfUseDoesNotProduceConstraintViolations() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);
        this.metrologyConfiguration.doAddReadingTypeDeliverable(this.deliverableWithoutTimeOfUse);

        // Business method
        this.testValidation(this.metrologyConfiguration);

        // Asserts: not expecting any constraint violations
    }

    @Test
    public void oneEventsSetsAndOneCompatibleDeliverableWithTimeOfUseDoesNotProducesConstraintViolations() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);
        when(this.eventSet.getEvents()).thenReturn(Arrays.asList(this.peak, this.offpeak));
        this.metrologyConfiguration.doAddReadingTypeDeliverable(this.deliverableWithTimeOfUse);
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 0);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(property = "deliverables[0.0.2.1.4.2.12.0.0.0.0.33.0.0.0.3.72.0].tou", messageId = "{" + MessageSeeds.Constants.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET + "}", strict = true)
    public void oneEventsSetsButOneDeliverableWithIncompatibleTimeOfUseProducesOneConstraintViolation() {
        this.metrologyConfiguration.doAddEventSet(this.eventSetOnMetrologyConfiguration);
        when(this.eventSet.getEvents()).thenReturn(Arrays.asList(this.peak, this.offpeak));
        this.initializeReadingType(this.readingTypeWithTimeOfUse, 33);
        this.metrologyConfiguration.doAddReadingTypeDeliverable(this.deliverableWithTimeOfUse);
        when(this.metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyContract.isMandatory()).thenReturn(true);
        this.metrologyConfiguration.doAddMetrologyContract(this.metrologyContract);

        // Business method
        this.testValidation(this.metrologyConfiguration, 1);

        // Asserts: see expected constraint violation rule
    }

    private void testValidation(MetrologyConfiguration configuration) {
        this.testValidation(configuration, 0);
    }

    private void testValidation(MetrologyConfiguration configuration, int expectedViolations) {
        Set<ConstraintViolation<MetrologyConfiguration>> failures = Validation.byDefaultProvider()
                .configure()
                .buildValidatorFactory()
                .getValidator()
                .validate(configuration, MetrologyConfigurationImpl.Activation.class);
        assertThat(failures).hasSize(expectedViolations);
        if (!failures.isEmpty()) {
            throw new VerboseConstraintViolationException(failures);
        }
    }

}