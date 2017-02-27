/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class CalendarOnUsagePointImplIT {

    public static final String SECONDARY_DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final int PEAK_CODE = 3;
    public static final int OFF_PEAK_CODE = 5;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private TransactionService transactionService;
    private Injector injector;
    private MeteringDataModelService meteringModelService;
    private CalendarService calendarService;
    private MetrologyConfigurationService metrologyConfigurationService;
    private MeteringService meteringService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new CustomPropertySetsModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule(),
                    new PartyModule(),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new EventsModule(),
                    new DataVaultModule(),
                    new UserModule(),
                    new IdsModule(),
                    new TaskModule(),
                    new MeteringModule(SECONDARY_DELTA),
                    new CalendarModule(),
                    new UsagePointLifeCycleConfigurationModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            metrologyConfigurationService = injector.getInstance(MetrologyConfigurationService.class);
            meteringModelService = injector.getInstance(MeteringDataModelService.class);
            meteringService = injector.getInstance(MeteringService.class);
            calendarService = injector.getInstance(CalendarService.class);
            ThreadPrincipalService threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            threadPrincipalService.set(() -> "Test");
            setupDefaultUsagePointLifeCycle();
            return null;
        });
    }

    private void setupDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
        usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testAddCalendar() {

        ZonedDateTime start = ZonedDateTime.now().plusDays(1);
        ZonedDateTime.now().plusDays(1);
        Calendar calendar;
        UsagePoint usagePoint;
        try (TransactionContext context = transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            context.commit();
        }

        UsagePoint.CalendarUsage calendarUsage;
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();

            calendarUsage = usedCalendars.addCalendar(calendar, start.toInstant());

            context.commit();
        }

        assertThat(calendarUsage).isNotNull();

        UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();
        Map<Category, List<UsagePoint.CalendarUsage>> calendars = usedCalendars.getCalendars();

        assertThat(calendars).hasSize(1);
        List<UsagePoint.CalendarUsage> calendarList = calendars.values().iterator().next();
        assertThat(calendarList).hasSize(1);
        UsagePoint.CalendarUsage found = calendarList.get(0);
        assertThat(found.getCalendar()).isEqualTo(calendar);
        assertThat(found.getRange()).isEqualTo(Range.atLeast(start.toInstant()));
    }

    @Test
    public void testAddCalendarOfSameCategory() {

        ZonedDateTime start = ZonedDateTime.now().plusDays(1);
        ZonedDateTime secondStart = start.plusMonths(1);

        Calendar calendar;
        UsagePoint usagePoint;
        try (TransactionContext context = transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();

            usedCalendars.addCalendar(calendar, start.toInstant());

            context.commit();
        }

        UsagePoint.CalendarUsage newCalendarUsage;
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();

            newCalendarUsage = usedCalendars.addCalendar(calendar, secondStart.toInstant());

            context.commit();
        }

        assertThat(newCalendarUsage).isNotNull();

        UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();
        Map<Category, List<UsagePoint.CalendarUsage>> calendars = usedCalendars.getCalendars();

        assertThat(calendars).hasSize(1);
        List<UsagePoint.CalendarUsage> calendarList = calendars.values().iterator().next();
        assertThat(calendarList).hasSize(2);
        UsagePoint.CalendarUsage found = calendarList.get(0);
        assertThat(found.getCalendar()).isEqualTo(calendar);
        assertThat(found.getRange()).isEqualTo(Range.closedOpen(start.toInstant(), secondStart.toInstant()));

        found = calendarList.get(1);
        assertThat(found.getCalendar()).isEqualTo(calendar);
        assertThat(found.getRange()).isEqualTo(Range.atLeast(secondStart.toInstant()));
    }

    @Test
    public void testAddCalendarOfSameCategoryTooEarly() {

        ZonedDateTime start = ZonedDateTime.now().plusDays(6);
        ZonedDateTime secondStart = start.minusDays(OFF_PEAK_CODE);

        Calendar calendar;
        UsagePoint usagePoint;
        try (TransactionContext context = transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();

            usedCalendars.addCalendar(calendar, start.toInstant());

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();

            usedCalendars.addCalendar(calendar, secondStart.toInstant());

            throw new AssertionError("Expected exception");
        } catch (LocalizedFieldValidationException e) {
            // expected
            assertThat(e.getMessageSeed()).isEqualTo(PrivateMessageSeeds.CANNOT_START_PRIOR_TO_LATEST_CALENDAR_OF_SAME_CATEGORY);
        }
    }

    /**
     * Tests the removal of a Calendar when the metrology configuration needs a Calendar.
     */
    @Test
    public void testRemoveCalendarNeededByTheMetrologyConfiguration() {
        UsagePointMetrologyConfiguration metrologyConfiguration = this.createConfigurationWithPeakOffPeakEvents();

        // Create UsagePoint, apply the configuration and add a Calendar
        Calendar calendar;
        UsagePoint usagePoint;
        try (TransactionContext context = this.transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();
            usedCalendars.addCalendar(calendar);
            usagePoint.apply(metrologyConfiguration);

            // Attempting to remove the Calendar again, should throw UnsatisfiedTimeOfUseBucketsException
            try {
                usedCalendars.removeCalendar(calendar, this.meteringModelService.getClock().instant().plusSeconds(10));
            } catch (UnsatisfiedTimeOfUseBucketsException e) {
                /* Expected but cannot add it to the @Test annotation
                 * because it is being thrown by other business methods as well
                 * and we are not expecting those to fail. */
            }
        }
    }

    /**
     * Tests the removal of a Calendar when the metrology configuration
     * no longer needs a Calendar.
     */
    @Test
    public void testRemoveCalendarNoLongerNeededByTheConfiguration() {
        UsagePointMetrologyConfiguration peakOffPeakConfiguration = this.createConfigurationWithPeakOffPeakEvents();
        UsagePointMetrologyConfiguration metrologyConfiguration = this.createConfigurationWithoutEvents();

        // Create UsagePoint, apply the configuration and add a Calendar
        Calendar calendar;
        UsagePoint usagePoint;
        Instant tenSecondsFromNow;
        try (TransactionContext context = this.transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();
            usedCalendars.addCalendar(calendar);
            usagePoint.apply(peakOffPeakConfiguration);

            tenSecondsFromNow = this.meteringModelService.getClock().instant().plusSeconds(10);
            usagePoint.apply(metrologyConfiguration, tenSecondsFromNow);

            // Business method
            usedCalendars.removeCalendar(calendar, tenSecondsFromNow);
            context.commit();
        }

        // Asserts
        Optional<Calendar> calendarInUse = usagePoint.getUsedCalendars().getCalendar(tenSecondsFromNow, calendar.getCategory());
        assertThat(calendarInUse).isEmpty();
    }

    /**
     * Tests the removal of a Calendar that was not need by the metrology configuration.
     */
    @Test
    public void testRemoveCalendarNotNeededByTheConfiguration() {
        UsagePointMetrologyConfiguration metrologyConfiguration = this.createConfigurationWithoutEvents();

        // Create UsagePoint, apply the configuration, add a Calendar and remote it in the future
        Calendar calendar;
        UsagePoint usagePoint;
        Instant tenSecondsFromNow;
        try (TransactionContext context = this.transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();
            usedCalendars.addCalendar(calendar);
            usagePoint.apply(metrologyConfiguration);

            tenSecondsFromNow = this.meteringModelService.getClock().instant().plusSeconds(10);

            // Business method
            usedCalendars.removeCalendar(calendar, tenSecondsFromNow);
            context.commit();
        }

        // Asserts
        Optional<Calendar> calendarInUse = usagePoint.getUsedCalendars().getCalendar(tenSecondsFromNow, calendar.getCategory());
        assertThat(calendarInUse).isEmpty();
    }

    /**
     * Tests that a gap can be created in the Calendar timeline as long
     * as the gap is for a metrology configuration that does not need events.
     */
    @Test
    public void testCreateGap() {
        UsagePointMetrologyConfiguration peakOffPeakConfiguration = this.createConfigurationWithPeakOffPeakEvents();
        UsagePointMetrologyConfiguration metrologyConfiguration = this.createConfigurationWithoutEvents();

        Calendar calendar;
        UsagePoint usagePoint;
        Instant now;
        Instant tenSecondsFromNow;
        Instant twentySecondsFromNow;
        try (TransactionContext context = this.transactionService.getContext()) {
            calendar = createCalendar("Test");
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("MRID", Instant.now()).create();
            UsagePoint.UsedCalendars usedCalendars = usagePoint.getUsedCalendars();
            usedCalendars.addCalendar(calendar);
            usagePoint.apply(peakOffPeakConfiguration);

            now = this.meteringModelService.getClock().instant();
            tenSecondsFromNow = now.plusSeconds(10);
            twentySecondsFromNow = now.plusSeconds(20);

            usagePoint.apply(metrologyConfiguration, tenSecondsFromNow);
            usagePoint.apply(peakOffPeakConfiguration, twentySecondsFromNow);
            usagePoint.getUsedCalendars().addCalendar(calendar, twentySecondsFromNow);

            // Business method
            usedCalendars.removeCalendar(calendar, tenSecondsFromNow);
            context.commit();
        }

        // Asserts
        assertThat(usagePoint.getUsedCalendars().getCalendar(now, calendar.getCategory())).contains(calendar);
        assertThat(usagePoint.getUsedCalendars().getCalendar(tenSecondsFromNow, calendar.getCategory())).isEmpty();
        assertThat(usagePoint.getUsedCalendars().getCalendar(twentySecondsFromNow, calendar.getCategory())).contains(calendar);
    }

    private UsagePointMetrologyConfiguration createConfigurationWithPeakOffPeakEvents() {
        MetrologyPurpose billingPurpose = this.metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        UsagePointMetrologyConfiguration peakOffPeakConfiguration;
        try (TransactionContext context = this.transactionService.getContext()) {
            // Create reading types for peak and offpeak 15 min kWh consumption
            ReadingType peakReadingType = this.meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0." + PEAK_CODE + ".0.0.0.0.72.0", "Peak Consumption");
            ReadingType offPeakReadingType = this.meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0." + OFF_PEAK_CODE + ".0.0.0.0.72.0", "Offpeak Consumption");
            ServiceCategory serviceCategory = this.meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            EventSet eventSet =
                    this.calendarService
                            .newEventSet("Test")
                            .addEvent("Peak").withCode(PEAK_CODE)
                            .addEvent("Offpeak").withCode(OFF_PEAK_CODE)
                            .add();
            peakOffPeakConfiguration = this.metrologyConfigurationService.newUsagePointMetrologyConfiguration("Peak_OffPeak", serviceCategory).create();
            peakOffPeakConfiguration.addEventSet(eventSet);
            ReadingTypeDeliverableBuilder peakBuilder = peakOffPeakConfiguration.newReadingTypeDeliverable("PEAK", peakReadingType, Formula.Mode.AUTO);
            ReadingTypeDeliverable peakConsumption = peakBuilder.build(peakBuilder.constant(BigDecimal.TEN));
            ReadingTypeDeliverableBuilder offPeakBuilder = peakOffPeakConfiguration.newReadingTypeDeliverable("OFFPEAK", offPeakReadingType, Formula.Mode.AUTO);
            ReadingTypeDeliverable offpeakConsumption = offPeakBuilder.build(offPeakBuilder.constant(BigDecimal.TEN));
            peakOffPeakConfiguration
                    .addMandatoryMetrologyContract(billingPurpose)
                    .addDeliverable(peakConsumption)
                    .addDeliverable(offpeakConsumption);
            peakOffPeakConfiguration.activate();
            context.commit();
        }

        return peakOffPeakConfiguration;
    }

    private UsagePointMetrologyConfiguration createConfigurationWithoutEvents() {
        MetrologyPurpose billingPurpose = this.metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        UsagePointMetrologyConfiguration metrologyConfiguration;
        try (TransactionContext context = this.transactionService.getContext()) {
            ServiceCategory serviceCategory = this.meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            metrologyConfiguration = this.metrologyConfigurationService.newUsagePointMetrologyConfiguration("Flat", serviceCategory).create();
            ReadingType kWh_15min = this.meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "Consumption");
            ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("PEAK", kWh_15min, Formula.Mode.AUTO);
            ReadingTypeDeliverable consumption = builder.build(builder.constant(BigDecimal.TEN));
            metrologyConfiguration
                    .addMandatoryMetrologyContract(billingPurpose)
                    .addDeliverable(consumption);
            metrologyConfiguration.activate();
            context.commit();
        }
        return metrologyConfiguration;
    }

    private Calendar createCalendar(String name) {
        EventSet eventSet = calendarService.newEventSet(name)
                .addEvent("On peak").withCode(PEAK_CODE)
                .addEvent("Off peak").withCode(OFF_PEAK_CODE)
                .addEvent("Demand response").withCode(97)
                .add();
        return calendarService.newCalendar(name, Year.of(2017), eventSet)
                .category(calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name()).orElseThrow(AssertionError::new))
                .description("Description remains to be completed :-)")
                .mRID("Sample-TOU-rates")
                .newDayType("Summer weekday")
                .eventWithCode(PEAK_CODE).startsFrom(LocalTime.of(13, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(20, 0, 0))
                .add()
                .newDayType("Weekend")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
                .newDayType("Holiday")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
                .newDayType("Winter day")
                .event("On peak").startsFrom(LocalTime.of(OFF_PEAK_CODE, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(21, 0, 0))
                .add()
                .newDayType("Demand response")
                .eventWithCode(97).startsFrom(LocalTime.MIDNIGHT)
                .add()
                .addPeriod("Summer", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
                .addPeriod("Winter", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
                .on(MonthDay.of(OFF_PEAK_CODE, 1)).transitionTo("Summer")
                .on(MonthDay.of(11, 1)).transitionTo("Winter")
                .except("Holiday")
                .occursOnceOn(LocalDate.of(2016, 1, 18))
                .occursOnceOn(LocalDate.of(2016, 2, 15))
                .occursOnceOn(LocalDate.of(2016, OFF_PEAK_CODE, 30))
                .occursAlwaysOn(MonthDay.of(7, 4))
                .occursOnceOn(LocalDate.of(2016, 9, OFF_PEAK_CODE))
                .occursOnceOn(LocalDate.of(2016, 10, 10))
                .occursAlwaysOn(MonthDay.of(11, 11))
                .occursOnceOn(LocalDate.of(2016, 11, 24))
                .occursAlwaysOn(MonthDay.of(12, 25))
                .occursAlwaysOn(MonthDay.of(12, 26))
                .add()
                .add();
    }

}