package com.energyict.mdc.engine.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalendar;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TariffCalendarFinderImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (15:21)
 */
@RunWith(MockitoJUnitRunner.class)
public class TariffCalendarFinderImplTest {

    private static final long CALENDAR_ID = 97;

    @Mock
    private CalendarService service;
    @Mock
    private Calendar calendar;

    @Before
    public void intializeMocks() {
        when(this.service.findCalendar(CALENDAR_ID)).thenReturn(Optional.of(this.calendar));
        when(this.calendar.getId()).thenReturn(CALENDAR_ID);
    }

    @After
    public void clearServices() {
        Services.tariffCalendarFinder(null);
    }

    @Test
    public void activate() {
        // Make sure that no service is registered
        Services.tariffCalendarFinder(null);

        // Business methods
        this.getInstance().activate();

        // Asserts
        assertThat(Services.tariffCalendarFinder()).isNotNull();
    }

    @Test
    public void deactivate() {
        // Make sure that a service is registered
        Services.tariffCalendarFinder(mock(TariffCalendarFinder.class));

        // Business methods
        this.getInstance().deactivate();

        // Asserts
        assertThat(Services.tariffCalendarFinder()).isNull();
    }

    @Test
    public void nonNumericalIdentifierReturnEmptyOptional() {
        TariffCalendarFinderImpl finder = this.getInstance();

        // Business method
        Optional<TariffCalendar> calendar = finder.from(TariffCalendarFinderImplTest.class.getSimpleName());

        // Asserts
        assertThat(calendar).isEmpty();
    }

    @Test
    public void nonExistingCalendar() {
        TariffCalendarFinderImpl finder = this.getInstance();

        // Business method
        Optional<TariffCalendar> calendar = finder.from("101");

        // Asserts
        assertThat(calendar).isEmpty();
        verify(this.service.findCalendar(101L));
    }

    @Test
    public void existingCalendar() {
        TariffCalendarFinderImpl finder = this.getInstance();

        // Business method
        Optional<TariffCalendar> calendar = finder.from(Long.toString(CALENDAR_ID));

        // Asserts
        verify(this.service.findCalendar(CALENDAR_ID));
    }

    private TariffCalendarFinderImpl getInstance() {
        return new TariffCalendarFinderImpl(this.service);
    }

}