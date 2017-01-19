package com.energyict.mdc.engine.impl;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.upl.properties.TariffCalendar;

/**
 * Adapts the {@link Calendar} interface to the upl {@link TariffCalendar} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (15:11)
 */
public class TariffCalendarAdapter implements TariffCalendar {
    private final Calendar actual;

    public TariffCalendarAdapter(Calendar actual) {
        this.actual = actual;
    }

    public Calendar getActual() {
        return actual;
    }
}