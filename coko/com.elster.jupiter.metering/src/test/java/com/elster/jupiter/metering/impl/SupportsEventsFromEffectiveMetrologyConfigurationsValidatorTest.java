/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresentReferenceValidator;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SupportsEventsFromEffectiveMetrologyConfigurationsValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-23 (13:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class SupportsEventsFromEffectiveMetrologyConfigurationsValidatorTest {

    private static final Instant CALENDAR_USAGE_START = LocalDate.of(2017, Month.FEBRUARY, 23).atStartOfDay(ZoneOffset.UTC).toInstant();
    private static final long PEAK_CODE = 97L;
    private static final long OFF_PEAK_CODE = 101L;

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private ServerUsagePoint usagePoint;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract mandatoryContract;
    @Mock
    private MetrologyContract optionalContract;
    @Mock
    private ReadingTypeDeliverable deliverableWithTimeOfUse;
    @Mock
    private IReadingType readingTypeWithTimeOfUse;
    @Mock
    private ReadingTypeDeliverable deliverableWithoutTimeOfUse;
    @Mock
    private IReadingType readingTypeWithoutTimeOfUse;
    @Mock
    private SimpleChannelContract channel;
    @Mock
    private Category category;
    @Mock
    private Calendar calendarWithCompatibleEvents;
    @Mock
    private Calendar calendarWithOtherEvents;

    @Before
    public void setupThesaurus() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
    }

    @Before
    public void initializeMetrologyConfigurationMocks() {
        when(this.mandatoryContract.isMandatory()).thenReturn(true);
        when(this.optionalContract.isMandatory()).thenReturn(false);
        when(this.readingTypeWithTimeOfUse.getTou()).thenReturn((int) PEAK_CODE);
        when(this.readingTypeWithTimeOfUse.getMRID()).thenReturn("Reading type with TOU: " + (int) PEAK_CODE);
        when(this.deliverableWithTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithTimeOfUse);
        when(this.readingTypeWithoutTimeOfUse.getTou()).thenReturn(0);
        when(this.readingTypeWithoutTimeOfUse.getMRID()).thenReturn("Reading type without TOU");
        when(this.deliverableWithoutTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithoutTimeOfUse);

        Event compatibleEvent = mock(Event.class);
        when(compatibleEvent.getCode()).thenReturn(PEAK_CODE);
        when(this.calendarWithCompatibleEvents.getEvents()).thenReturn(Collections.singletonList(compatibleEvent));
        when(this.calendarWithCompatibleEvents.getCategory()).thenReturn(this.category);

        Event otherEvent = mock(Event.class);
        when(otherEvent.getCode()).thenReturn(Long.MAX_VALUE);
        when(this.calendarWithOtherEvents.getEvents()).thenReturn(Collections.singletonList(otherEvent));
        when(this.calendarWithOtherEvents.getCategory()).thenReturn(this.category);
    }

    @Before
    public void setupDataModel() {
        when(this.dataModel.getInstance(CalendarUsageImpl.class)).thenReturn(new CalendarUsageImpl(this.dataModel));
    }

    @Test
    public void addCalendarWithoutEffectiveMetrologyConfigurations() {
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.emptyList());
        CalendarUsageImpl calendarUsage = CalendarUsageImpl.create(this.dataModel, CALENDAR_USAGE_START, this.usagePoint, this.calendarWithOtherEvents);

        // Business method
        this.testValidationOk(calendarUsage);

        // Asserts
        verify(this.calendarWithOtherEvents).getEvents();
        verify(this.usagePoint).getEffectiveMetrologyConfigurations(Range.atLeast(CALENDAR_USAGE_START));
    }

    @Test
    public void addCalendarWithEffectiveMetrologyConfigurationWithoutDeliverables() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.metrologyConfiguration);
        when(this.metrologyConfiguration.getContracts()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        CalendarUsageImpl calendarUsage = CalendarUsageImpl.create(this.dataModel, CALENDAR_USAGE_START, this.usagePoint, this.calendarWithCompatibleEvents);

        // Business method
        this.testValidationOk(calendarUsage);

        // Asserts
        verify(this.calendarWithCompatibleEvents).getEvents();
        verify(this.usagePoint).getEffectiveMetrologyConfigurations(Range.atLeast(CALENDAR_USAGE_START));
        verify(effectiveMetrologyConfigurationOnUsagePoint).getMetrologyConfiguration();
        verify(this.metrologyConfiguration).getContracts();
    }

    @Test
    public void addCalendar_OptionalAndMandatoryContractsWithoutTimeOfUseDeliverables() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.metrologyConfiguration);
        IReadingType readingTypeWithOtherTimeOfUse = mock(IReadingType.class);
        when(readingTypeWithOtherTimeOfUse.getTou()).thenReturn(0);
        when(readingTypeWithOtherTimeOfUse.getMRID()).thenReturn("Another reading type without TOU");
        ReadingTypeDeliverable deliverableWithOtherTOU = mock(ReadingTypeDeliverable.class);
        when(deliverableWithOtherTOU.getReadingType()).thenReturn(readingTypeWithOtherTimeOfUse);
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(deliverableWithOtherTOU));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        when(this.metrologyConfiguration.getDeliverables()).thenReturn(Arrays.asList(this.deliverableWithoutTimeOfUse, deliverableWithOtherTOU));
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        CalendarUsageImpl calendarUsage = CalendarUsageImpl.create(this.dataModel, CALENDAR_USAGE_START, this.usagePoint, this.calendarWithCompatibleEvents);

        // Business method
        this.testValidationFails(calendarUsage);

        // Asserts
        verify(this.calendarWithCompatibleEvents).getEvents();
        verify(this.usagePoint).getEffectiveMetrologyConfigurations(Range.atLeast(CALENDAR_USAGE_START));
        verify(effectiveMetrologyConfigurationOnUsagePoint).getMetrologyConfiguration();
        verify(this.metrologyConfiguration).getContracts();
        verify(this.mandatoryContract).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse).getReadingType();
        verify(this.readingTypeWithoutTimeOfUse).getTou();
        verify(this.optionalContract, never()).getDeliverables();
        verify(deliverableWithOtherTOU, never()).getReadingType();
        verify(readingTypeWithOtherTimeOfUse, never()).getTou();
    }

    @Test
    public void addCalendar_OptionalContractWithTimeOfUse_MandatoryContractsWithoutTimeOfUseDeliverables() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.metrologyConfiguration);
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        when(this.metrologyConfiguration.getDeliverables()).thenReturn(Arrays.asList(this.deliverableWithoutTimeOfUse, this.deliverableWithTimeOfUse));
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        CalendarUsageImpl calendarUsage = CalendarUsageImpl.create(this.dataModel, CALENDAR_USAGE_START, this.usagePoint, this.calendarWithCompatibleEvents);

        // Business method
        this.testValidationFails(calendarUsage);

        // Asserts
        verify(this.calendarWithCompatibleEvents).getEvents();
        verify(this.usagePoint).getEffectiveMetrologyConfigurations(Range.atLeast(CALENDAR_USAGE_START));
        verify(effectiveMetrologyConfigurationOnUsagePoint).getMetrologyConfiguration();
        verify(this.metrologyConfiguration).getContracts();
        verify(this.mandatoryContract).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse).getReadingType();
        verify(this.readingTypeWithoutTimeOfUse).getTou();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithTimeOfUse, never()).getReadingType();
        verify(this.readingTypeWithTimeOfUse, never()).getTou();
    }

    @Test
    public void addCalendar_OptionalContractWithoutTimeOfUse_MandatoryContractsWithTimeOfUseDeliverables() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.metrologyConfiguration);
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        when(this.metrologyConfiguration.getDeliverables()).thenReturn(Arrays.asList(this.deliverableWithoutTimeOfUse, this.deliverableWithTimeOfUse));
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        CalendarUsageImpl calendarUsage = CalendarUsageImpl.create(this.dataModel, CALENDAR_USAGE_START, this.usagePoint, this.calendarWithCompatibleEvents);

        // Business method
        this.testValidationOk(calendarUsage);

        // Asserts
        verify(this.calendarWithCompatibleEvents).getEvents();
        verify(this.usagePoint).getEffectiveMetrologyConfigurations(Range.atLeast(CALENDAR_USAGE_START));
        verify(effectiveMetrologyConfigurationOnUsagePoint).getMetrologyConfiguration();
        verify(this.metrologyConfiguration).getContracts();
        verify(this.mandatoryContract).getDeliverables();
        verify(this.deliverableWithTimeOfUse).getReadingType();
        verify(this.readingTypeWithTimeOfUse).getTou();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse, never()).getReadingType();
        verify(this.readingTypeWithoutTimeOfUse, never()).getTou();
    }

    @Test
    public void addCalenda_OptionalContractWithoutTimeOfUse_MandatoryContractsWithOtherTimeOfUseDeliverables() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        IReadingType readingTypeWithOtherTimeOfUse = mock(IReadingType.class);
        when(readingTypeWithOtherTimeOfUse.getTou()).thenReturn((int) OFF_PEAK_CODE);
        when(readingTypeWithOtherTimeOfUse.getMRID()).thenReturn("Another reading type without TOU");
        ReadingTypeDeliverable deliverableWithOtherTimeOfUse = mock(ReadingTypeDeliverable.class);
        when(deliverableWithOtherTimeOfUse.getReadingType()).thenReturn(readingTypeWithOtherTimeOfUse);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.metrologyConfiguration);
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(deliverableWithOtherTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        when(this.metrologyConfiguration.getDeliverables()).thenReturn(Arrays.asList(this.deliverableWithoutTimeOfUse, this.deliverableWithTimeOfUse));
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        CalendarUsageImpl calendarUsage = CalendarUsageImpl.create(this.dataModel, CALENDAR_USAGE_START, this.usagePoint, this.calendarWithOtherEvents);

        // Business method
        this.testValidationFails(calendarUsage);

        // Asserts
        verify(this.calendarWithOtherEvents).getEvents();
        verify(this.usagePoint).getEffectiveMetrologyConfigurations(Range.atLeast(CALENDAR_USAGE_START));
        verify(effectiveMetrologyConfigurationOnUsagePoint).getMetrologyConfiguration();
        verify(this.metrologyConfiguration).getContracts();
        verify(this.mandatoryContract).getDeliverables();
        verify(deliverableWithOtherTimeOfUse).getReadingType();
        verify(readingTypeWithOtherTimeOfUse).getTou();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithTimeOfUse, never()).getReadingType();
        verify(this.readingTypeWithTimeOfUse, never()).getTou();
    }

    private SupportsEventsFromEffectiveMetrologyConfigurationsValidator getTestInstance() {
        return new SupportsEventsFromEffectiveMetrologyConfigurationsValidator(this.thesaurus);
    }

    private void testValidationOk(CalendarUsageImpl calendarUsage) {
        Set<ConstraintViolation<CalendarUsageImpl>> violations = this.getValidatorFactory().getValidator().validate(calendarUsage, Save.Create.class);
        assertThat(violations).isEmpty();
    }

    private void testValidationFails(CalendarUsageImpl calendarUsage) {
        Set<ConstraintViolation<CalendarUsageImpl>> violations = this.getValidatorFactory().getValidator().validate(calendarUsage, Save.Create.class);
        assertThat(violations).hasSize(1);
    }

    public ValidatorFactory getValidatorFactory() {
        return Validation.byDefaultProvider()
                    .configure()
                    .constraintValidatorFactory(this.getConstraintValidatorFactory())
                    .messageInterpolator(this.thesaurus)
                    .buildValidatorFactory();
    }

    private ConstraintValidatorFactory getConstraintValidatorFactory() {
        return new ConstraintValidatorFactory() {

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> arg0) {
            }

            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> clazz) {
                if (clazz.equals(SupportsEventsFromEffectiveMetrologyConfigurationsValidator.class)) {
                    return (T) getTestInstance();
                } else if (clazz.equals(IsPresentReferenceValidator.class)) {
                    return (T) new IsPresentReferenceValidator();
                } else {
                    throw new IllegalArgumentException("Constraint validator factory does not (yet) support class " + clazz.getName());
                }
            }
        };
    }

}