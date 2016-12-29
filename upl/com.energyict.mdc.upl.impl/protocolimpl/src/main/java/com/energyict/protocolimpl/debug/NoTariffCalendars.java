package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalendar;

import java.util.Optional;

/**
 * Provides an implementation for the {@link TariffCalendarFinder} interface
 * that never returns any {@link TariffCalendar}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (11:35)
 */
final class NoTariffCalendars implements TariffCalendarFinder {
    @Override
    public Optional<TariffCalendar> from(String identifier) {
        return Optional.empty();
    }
}