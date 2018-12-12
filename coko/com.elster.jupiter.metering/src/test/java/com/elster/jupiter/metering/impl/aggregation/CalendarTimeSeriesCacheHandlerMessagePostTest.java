/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

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
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.CalendarUsageImpl;
import com.elster.jupiter.metering.impl.MeterActivationImpl;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.SimpleChannelContract;
import com.elster.jupiter.metering.impl.SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurationsValidator;
import com.elster.jupiter.metering.impl.UsagePointAccountabilityImpl;
import com.elster.jupiter.metering.impl.UsagePointImpl;
import com.elster.jupiter.metering.impl.UsagePointMeterActivatorImpl;
import com.elster.jupiter.metering.impl.UsagePointStateTemporalImpl;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyConfigurationOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.MetrologyContractChannelsContainerImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that an appropriate messge is posted on the {@link com.elster.jupiter.metering.impl.aggregation.CalendarTimeSeriesCacheHandlerFactory}'s queue
 * when a {@link MetrologyConfiguration} is applied to a {@link UsagePointImpl}
 * or when a {@link Calendar} is linked to a UsagePoint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-14 (10:10)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesCacheHandlerMessagePostTest {
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
    private SimpleChannelContract channel;
    @Mock
    private Category category;
    @Mock
    private Calendar calendar;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private LicenseService licenseService;

    private Injector injector;
    private Instant now;

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

        when(this.category.getName()).thenReturn(OutOfTheBoxCategory.TOU.name());
        when(this.category.getDisplayName()).thenReturn("Time of use in testing context");
        Event compatibleEvent = mock(Event.class);
        when(compatibleEvent.getCode()).thenReturn((long) tou);
        when(this.calendar.getEvents()).thenReturn(Collections.singletonList(compatibleEvent));
        when(this.calendar.getCategory()).thenReturn(this.category);

        Event otherEvent = mock(Event.class);
        when(otherEvent.getCode()).thenReturn(Long.MAX_VALUE);
    }

    @Before
    public void initializeLifecycleMocks() {
        now = LocalDate.ofYearDay(2017, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
        when(this.clock.instant()).thenReturn(now);
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
        when(this.destinationSpec.message(anyString())).thenReturn(this.messageBuilder);
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
            }
        };
    }

    @Test
    public void applyMetrologyConfiguration() {
        when(this.metrologyConfiguration.getContracts()).thenReturn(Collections.emptyList());
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.apply(this.metrologyConfiguration);

        // Asserts
        verify(this.destinationSpec).message("0" + CalendarTimeSeriesCacheHandler.USAGE_POINT_TIMESTAMP_SEPARATOR + Long.toString(this.now.toEpochMilli()));
        verify(this.messageBuilder).send();
    }

    @Test
    public void linkCalendar() {
        UsagePointImpl usagePoint = this.getTestUsagePoint();

        // Business method
        usagePoint.getUsedCalendars().addCalendar(this.calendar);

        // Asserts
        verify(this.destinationSpec).message("0" + CalendarTimeSeriesCacheHandler.USAGE_POINT_TIMESTAMP_SEPARATOR + Long.toString(this.now.toEpochMilli()));
        verify(this.messageBuilder).send();
    }

    private UsagePointImpl getTestUsagePoint() {
        UsagePointImpl usagePoint = this.injector.getInstance(UsagePointImpl.class);
        usagePoint.setInstallationTime(this.clock.instant());
        usagePoint.setInitialState();
        return usagePoint;
    }

}