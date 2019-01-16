/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME("serviceCall", "Service call"),
    NAME_OF_CAMPAIGN("name", "Name"),
    DEVICE_TYPE("deviceType", "Device type"),
    DEVICE_GROUP("deviceGroup", "Device group"),
    ACTIVATION_START("activationStart", "Time boundary start"),
    ACTIVATION_END("activationEnd", "Time boundary end"),
    CALENDAR("calendar", "ToU calendar"),
    ACTIVATION_OPTION("activationOption", "Activation Option"),
    ACTIVATION_DATE("activationDate", "Activation Date"),
    UPDATE_TYPE("updateType", "Update"),
    TIME_VALIDATION("timeValidation", "Timeout before validation(sec)"),
    DEVICE("device", "Device"),
    FULL_CALENDAR("fullCalendar", "Full Calendar"),
    SPECIAL_DAYS("specialDays", "Special Days"),
    IMMEDIATELY("immediately", "Immediately"),
    WITHOUT_ACTIVATION("withoutActivation", "Without Activation");


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
