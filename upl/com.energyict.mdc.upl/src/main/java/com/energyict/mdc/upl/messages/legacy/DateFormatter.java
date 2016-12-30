package com.energyict.mdc.upl.messages.legacy;

import java.util.Date;

/**
 * Formats a date according to the user's preferences.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (09:12)
 */
public interface DateFormatter {
    String format(Date date);
}