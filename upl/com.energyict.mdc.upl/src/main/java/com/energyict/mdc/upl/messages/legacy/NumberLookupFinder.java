package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;

import java.util.Optional;

/**
 * Finds {@link DeviceMessageFile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (08:46)
 */
public interface NumberLookupFinder {
    Optional<NumberLookup> from(String identifier);
}