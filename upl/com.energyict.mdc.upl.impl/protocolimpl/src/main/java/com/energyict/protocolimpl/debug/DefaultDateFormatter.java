package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.DateFormatter;

import java.util.Date;

/**
 * Provides an implementation for the {@link DateFormatter} interface
 * that simply uses {@link Date#toString()}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (09:32)
 */
public class DefaultDateFormatter implements DateFormatter {
    @Override
    public String format(Date date) {
        return date.toString();
    }
}