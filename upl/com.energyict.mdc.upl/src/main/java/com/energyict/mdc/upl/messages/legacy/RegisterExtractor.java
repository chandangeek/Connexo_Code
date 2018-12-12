package com.energyict.mdc.upl.messages.legacy;

import java.util.Optional;

/**
 * Extracts information that pertains to {@link com.energyict.mdc.upl.meterdata.Register}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (15:06)
 */
public interface RegisterExtractor {
    /**
     * Extracts the last reading from the {@link com.energyict.mdc.upl.meterdata.Register}.
     *
     * @param register The Register
     * @return The last registered reading or an empty Optional not readings have been registered yet
     */
    Optional<RegisterReading> lastReading(com.energyict.mdc.upl.meterdata.Register register);

    interface RegisterReading {
        String text();
    }
}