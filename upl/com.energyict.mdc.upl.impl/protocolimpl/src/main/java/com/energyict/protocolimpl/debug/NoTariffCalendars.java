package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalender;

import java.util.Optional;

/**
 * Provides an implementation for the {@link TariffCalendarFinder} interface
 * that never returns any {@link TariffCalender}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (11:35)
 */
final class NoTariffCalendars implements TariffCalendarFinder {
    @Override
    public Optional<TariffCalender> from(String identifier) {
        return Optional.empty();
    }
}