/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.aggregation.CalendarTimeSeriesCacheHandlerFactory;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyConfigurationOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.MetrologyContractChannelsContainerImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.impl.config.ServerReadingTypeDeliverable;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresentReferenceValidator;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the validation of calendar information when a
 * {@link MetrologyConfiguration} is applied to a {@link UsagePointImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-21 (13:39)
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateCalendarWhenMetrologyConfigurationIsAppliedToUsagePointTest {
    @Mock
    private Clock clock;
    @Mock
    private DataModel dataModel;
    @Mock
    private UserService userService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private IdsService idsService;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ServerDataAggregationService dataAggregationService;
    @Mock
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract mandatoryContract;
    @Mock
    private MetrologyContract optionalContract;
    @Mock
    private ServerReadingTypeDeliverable deliverableWithTimeOfUse;
    @Mock
    private IReadingType readingTypeWithTimeOfUse;
    @Mock
    private ServerReadingTypeDeliverable deliverableWithoutTimeOfUse;
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
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private LicenseService licenseService;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;

    private Injector injector;

    @Before
    public void initializeThesaurus() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @Before
    public void initializeMetrologyConfigurationMocks() {
        int tou = 11;
        when(this.mandatoryContract.isMandatory()).thenReturn(true);
        when(this.optionalContract.isMandatory()).thenReturn(false);
        when(this.readingTypeWithTimeOfUse.getTou()).thenReturn(tou);
        when(this.readingTypeWithTimeOfUse.getMRID()).thenReturn("Reading type with TOU: " + tou);
        when(this.deliverableWithTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithTimeOfUse);
        when(this.deliverableWithTimeOfUse.getRequiredTimeOfUse()).thenReturn(Optional.of((long) tou));
        when(this.readingTypeWithoutTimeOfUse.getTou()).thenReturn(0);
        when(this.readingTypeWithoutTimeOfUse.getMRID()).thenReturn("Reading type without TOU");
        when(this.deliverableWithoutTimeOfUse.getReadingType()).thenReturn(this.readingTypeWithoutTimeOfUse);
        when(this.deliverableWithoutTimeOfUse.getRequiredTimeOfUse()).thenReturn(Optional.empty());

        when(this.category.getName()).thenReturn(OutOfTheBoxCategory.TOU.name());
        when(this.category.getDisplayName()).thenReturn("Time of use in testing context");
        Event compatibleEvent = mock(Event.class);
        when(compatibleEvent.getCode()).thenReturn((long) tou);
        when(this.calendarWithCompatibleEvents.getEvents()).thenReturn(Collections.singletonList(compatibleEvent));
        when(this.calendarWithCompatibleEvents.getCategory()).thenReturn(this.category);

        Event otherEvent = mock(Event.class);
        when(otherEvent.getCode()).thenReturn(Long.MAX_VALUE);
        when(this.calendarWithOtherEvents.getEvents()).thenReturn(Collections.singletonList(otherEvent));
        when(this.calendarWithOtherEvents.getCategory()).thenReturn(this.category);
    }

    @Before
    public void initializeLifecycleMocks() {
        when(this.clock.instant()).thenReturn(LocalDate.ofYearDay(2017, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
        UsagePointLifeCycle lifeCycle = mock(UsagePointLifeCycle.class);
        State state = mock(State.class);
        when(state.isInitial()).thenReturn(true);
        Stage stage = mock(Stage.class);
        when(stage.getName()).thenReturn(UsagePointStage.PRE_OPERATIONAL.getKey());
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(lifeCycle.getStates()).thenReturn(Collections.singletonList(state));
        when(this.usagePointLifeCycleConfigurationService.getDefaultLifeCycle()).thenReturn(lifeCycle);
    }

    @Before
    public void initializeMessagingMocks() {
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        when(this.destinationSpec.message(anyString())).thenReturn(messageBuilder);
    }

    @Before
    public void setupGuiceInjection() {
        Module module = this.getModule();
        this.injector = Guice.createInjector(module);
        when(this.dataModel.getInstance(UsagePointStateTemporalImpl.class)).thenReturn(this.injector.getInstance(UsagePointStateTemporalImpl.class));
        when(this.dataModel.getInstance(EffectiveMetrologyConfigurationOnUsagePointImpl.class)).thenReturn(this.injector.getInstance(EffectiveMetrologyConfigurationOnUsagePointImpl.class));
        when(this.dataModel.getInstance(EffectiveMetrologyContractOnUsagePointImpl.class)).thenReturn(this.injector.getInstance(EffectiveMetrologyContractOnUsagePointImpl.class));
        when(this.dataModel.getInstance(UsagePointMeterActivatorImpl.class)).thenReturn(this.injector.getInstance(UsagePointMeterActivatorImpl.class));
        when(this.dataModel.getInstance(MetrologyContractChannelsContainerImpl.class)).thenReturn(this.injector.getInstance(MetrologyContractChannelsContainerImpl.class));
        when(this.dataModel.getInstance(CalendarUsageImpl.class)).thenReturn(this.injector.getInstance(CalendarUsageImpl.class));
        when(this.dataModel.getInstance(SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurationsValidator.class)).thenReturn(this.injector.getInstance(SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurationsValidator.class));
        when(this.dataModel.getInstance(IsPresentReferenceValidator.class)).thenReturn(this.injector.getInstance(IsPresentReferenceValidator.class));
        when(this.dataModel.getValidatorFactory()).thenReturn(this.getValidatorFactory());
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
                return dataModel.getInstance(clazz);
            }
        };
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(SimpleChannelContract.class).toInstance(channel);
                bind(Clock.class).toInstance(clock);
                bind(DataModel.class).toInstance(dataModel);
                bind(UserService.class).toInstance(userService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(IdsService.class).toInstance(idsService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(ServerMeteringService.class).toInstance(meteringService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(ServerMetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(ServerDataAggregationService.class).toInstance(dataAggregationService);
                bind(UsagePointLifeCycleConfigurationService.class).toInstance(usagePointLifeCycleConfigurationService);
                bind(MeterActivation.class).to(MeterActivationImpl.class);
                bind(UsagePointAccountability.class).to(UsagePointAccountabilityImpl.class);
                bind(DestinationSpec.class)
                        .annotatedWith(Names.named(CalendarTimeSeriesCacheHandlerFactory.TASK_DESTINATION))
                        .toInstance(destinationSpec);
                bind(LicenseService.class).toInstance(licenseService);
                bind(MessageService.class).toInstance(messageService);
                bind(JsonService.class).toInstance(jsonService);
            }
        };
    }

    @Test
    public void applyWithoutCalendarNoDeliverables() {
        when(this.metrologyConfiguration.getContracts()).thenReturn(Collections.emptyList());
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.metrologyConfiguration, atLeastOnce()).getContracts();
    }

    @Test
    public void applyWithoutCalendarAndBothOptionalAndMandatoryContractsWithoutTimeOfUseDeliverables() {
        IReadingType otherReadingTypeWithoutTimeOfUse = mock(IReadingType.class);
        when(otherReadingTypeWithoutTimeOfUse.getTou()).thenReturn(0);
        when(otherReadingTypeWithoutTimeOfUse.getMRID()).thenReturn("Another reading type without TOU");
        ReadingTypeDeliverable otherDeliverableWithoutTOU = mock(ReadingTypeDeliverable.class);
        when(otherDeliverableWithoutTOU.getReadingType()).thenReturn(otherReadingTypeWithoutTimeOfUse);
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(otherDeliverableWithoutTOU));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test
    public void applyWithoutCalendar_OptionalContractWithTimeOfUse_MandatoryContractsWithoutTimeOfUseDeliverables() {
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test(expected = UnsatisfiedTimeOfUseBucketsException.class)
    public void applyWithoutCalendar_OptionalContractWithoutTimeOfUse_MandatoryContractsWithTimeOfUseDeliverables() {
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts: see expected exception rule
    }

    @Test
    public void applyWithoutCalendar_OptionalContractWithoutTimeOfUse_MandatoryContractsWithTimeOfUseDeliverables_ThickRequirements() {
        when(this.deliverableWithTimeOfUse.getRequiredTimeOfUse()).thenReturn(Optional.empty());
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test
    public void applyWithCalendarNoDeliverables() {
        when(this.metrologyConfiguration.getContracts()).thenReturn(Collections.emptyList());
        UsagePointImpl usagePoint = this.getTestUsagePoint();
        usagePoint.getUsedCalendars().addCalendar(this.calendarWithCompatibleEvents);

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.metrologyConfiguration, atLeastOnce()).getContracts();
    }

    @Test
    public void applyWithCompatibleCalendarAndBothOptionalAndMandatoryContractsWithoutTimeOfUseDeliverables() {
        IReadingType otherReadingTypeWithoutTimeOfUse = mock(IReadingType.class);
        when(otherReadingTypeWithoutTimeOfUse.getTou()).thenReturn(0);
        when(otherReadingTypeWithoutTimeOfUse.getMRID()).thenReturn("Another reading type without TOU");
        ReadingTypeDeliverable otherDeliverableWithoutTOU = mock(ReadingTypeDeliverable.class);
        when(otherDeliverableWithoutTOU.getReadingType()).thenReturn(otherReadingTypeWithoutTimeOfUse);
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(otherDeliverableWithoutTOU));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();
        usagePoint.getUsedCalendars().addCalendar(this.calendarWithCompatibleEvents);

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test
    public void applyWithCompatibleCalendar_OptionalContractWithTimeOfUse_MandatoryContractsWithoutTimeOfUseDeliverables() {
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();
        usagePoint.getUsedCalendars().addCalendar(this.calendarWithCompatibleEvents);

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test
    public void applyWithInCompatibleCalendar_OptionalContractWithTimeOfUse_MandatoryContractsWithoutTimeOfUseDeliverables() {
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();
        usagePoint.getUsedCalendars().addCalendar(this.calendarWithOtherEvents);

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithoutTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test
    public void applyWithCompatibleCalendar_OptionalContractWithoutTimeOfUse_MandatoryContractsWithTimeOfUseDeliverables() {
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();
        usagePoint.getUsedCalendars().addCalendar(this.calendarWithCompatibleEvents);

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.mandatoryContract, atLeastOnce()).isMandatory();
        verify(this.mandatoryContract, atLeastOnce()).getDeliverables();
        verify(this.optionalContract, atLeastOnce()).isMandatory();
        verify(this.optionalContract, never()).getDeliverables();
        verify(this.deliverableWithTimeOfUse, atLeastOnce()).getRequiredTimeOfUse();
    }

    @Test(expected = UnsatisfiedTimeOfUseBucketsException.class)
    public void applyWithInCompatibleCalendar_OptionalContractWithoutTimeOfUse_MandatoryContractsWithTimeOfUseDeliverables() {
        when(this.mandatoryContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithTimeOfUse));
        when(this.optionalContract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverableWithoutTimeOfUse));
        when(this.metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(this.mandatoryContract, this.optionalContract));
        UsagePointImpl usagePoint = this.getTestUsagePoint();
        usagePoint.getUsedCalendars().addCalendar(this.calendarWithOtherEvents);

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts: see expected exception rule
    }

    private UsagePointImpl getTestUsagePoint() {
        UsagePointImpl usagePoint = this.injector.getInstance(UsagePointImpl.class);
        usagePoint.setInstallationTime(this.clock.instant());
        usagePoint.setInitialState();
        return usagePoint;
    }

}