package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.RegisterExtractor;

import java.util.Optional;

/**
 * Provides an dummy implementation for the {@link Extractor} interface
 * that returns empty values in all of its methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (09:49)
 */
final class DummyRegisterExtractor implements RegisterExtractor {
    @Override
    public Optional<RegisterReading> lastReading(com.energyict.mdc.upl.meterdata.Register register) {
        return Optional.empty();
    }
}