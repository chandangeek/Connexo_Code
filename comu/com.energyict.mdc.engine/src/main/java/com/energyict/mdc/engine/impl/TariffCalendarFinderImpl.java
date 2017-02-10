package com.energyict.mdc.engine.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalendar;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides an implementation for the {@link TariffCalendarFinder} interface
 * that redirects to the {@link CalendarService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (15:06)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.tariff.calendar.finder", service = {TariffCalendarFinder.class}, immediate = true)
@SuppressWarnings("unused")
public class TariffCalendarFinderImpl implements TariffCalendarFinder {
    private volatile CalendarService calendarService;

    // For OSGi purposes
    public TariffCalendarFinderImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public TariffCalendarFinderImpl(CalendarService calendarService) {
        this();
        this.setCalendarService(calendarService);
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Activate
    public void activate() {
        Services.tariffCalendarFinder(this);
    }

    @Deactivate
    public void deactivate() {
        Services.tariffCalendarFinder(null);
    }

    @Override
    public Optional<TariffCalendar> from(String identifier) {
        try {
            return this.calendarService
                    .findCalendar(Long.parseLong(identifier))
                    .map(TariffCalendarAdapter::new);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}