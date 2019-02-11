/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    STATUS_SUCCESSFUL("successful", "Successful"),
    STATUS_FAILED("failed", "Failed"),
    STATUS_CONFIGURATION_ERROR("configurationError", "Configuration Error"),
    STATUS_ONGOING("ongoing", "Ongoing"),
    STATUS_PENDING("pending", "Pending"),
    STATUS_CANCELED("canceled", "Canceled"),
    STATUS_COMPLETED("completed", "Completed"),
    FULL_CALENDAR("fullCalendar", "Full Calendar"),
    SPECIAL_DAYS("specialDays", "Special Days"),
    IMMEDIATELY("immediately", "Immediately"),
    WITHOUT_ACTIVATION("withoutActivation", "Without Activation"),
    ON_DATE("onDate", "On Date");


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
