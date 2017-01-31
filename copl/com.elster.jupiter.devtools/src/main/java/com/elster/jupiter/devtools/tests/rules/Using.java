/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.rules;

import org.joda.time.DateTimeZone;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Factory for LocaleNeutral and TimeZoneNeutral rules
 */
public enum Using {

    ;

    public static LocaleNeutral locale(String language, String country) {
        return new LocaleNeutral(new Locale(language, country));
    }

    public static LocaleNeutral locale(Locale locale) {
        return new LocaleNeutral(locale);
    }

    public static LocaleNeutral localeOfMalta() {
        return LocaleNeutral.DEFAULT_SUBSTITUTE;
    }

    public static TimeZoneNeutral timeZoneOfMcMurdo() {
        return TimeZoneNeutral.DEFAULT_TIMEZONE_NEUTRAL;
    }

    public static TimeZoneNeutral timeZone(TimeZone timeZone) {
        return new TimeZoneNeutral(timeZone);
    }

    public static TimeZoneNeutral timeZone(DateTimeZone timeZone) {
        return new TimeZoneNeutral(timeZone);
    }

    public static TimeZoneNeutral timeZone(String timeZone) {
        return new TimeZoneNeutral(timeZone);
    }
}
