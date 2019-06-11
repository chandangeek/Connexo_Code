/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    STATUS_COMPLETED("completed", "Completed"),
    STATUS_CONFIGURATION_ERROR("configurationError", "Configuration error"),
    FULL_CALENDAR("fullCalendar", "Full calendar"),
    SPECIAL_DAYS("specialDays", "Special days"),
    IMMEDIATELY("immediately", "Immediately"),
    WITHOUT_ACTIVATION("withoutActivation", "Without activation"),
    ON_DATE("onDate", "On date"),
    MINIMIZE_CONNECTIONS("MinimizeConnections", "Minimize connections"),
    AS_SOON_AS_POSSIBLE("AsSoonAsPossible", "As soon as possible"),
    ;


    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
