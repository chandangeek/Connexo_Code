/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.time.DayOfWeek;

public enum TranslationKeys implements TranslationKey {

    MONDAY(DayOfWeek.MONDAY.name(), "Monday"),
    TUESDAY(DayOfWeek.TUESDAY.name(), "Tuesday"),
    WEDNESDAY(DayOfWeek.WEDNESDAY.name(), "Wednesday"),
    THURSDAY(DayOfWeek.THURSDAY.name(), "Thursday"),
    FRIDAY(DayOfWeek.FRIDAY.name(), "Friday"),
    SATURDAY(DayOfWeek.SATURDAY.name(), "Saturday"),
    SUNDAY(DayOfWeek.SUNDAY.name(), "Sunday"),
    SUMMER("Summer", "Summer"),
    WINTER("Winter", "Winter"),
    SUMMER_WEEKDAY("summer weekday", "summer weekday"),
    WEEKEND("weekend", "weekend"),
    WINTER_DAY("winter day", "winter day"),
    HOLIDAY("holiday", "holiday"),
    ON_PEAK("On Peak (3)", "On Peak (3)"),
    OFF_PEAK("Off Peak (5)", "Off Peak (5)"),
    DEMAND_RESPOND("Demand response", "Demand response")
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static TranslationKeys from(String key) {
        if (key != null) {
            for (TranslationKeys translationKey : TranslationKeys.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }
}
