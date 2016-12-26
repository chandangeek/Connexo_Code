package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.properties.TariffCalender;

import java.util.Optional;

/**
 * Finds {@link TariffCalender}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-26 (15:29)
 */
public interface TariffCalendarFinder {
    Optional<TariffCalender> from(String identifier);
}