package com.energyict.mdc.upl.messages.legacy;

import java.time.Instant;

/**
 * Formats information according to the user's preferences.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (09:12)
 */
public interface Formatter {
    String format(Instant date);
}