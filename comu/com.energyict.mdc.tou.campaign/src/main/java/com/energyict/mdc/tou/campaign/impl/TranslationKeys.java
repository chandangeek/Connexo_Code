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
    CALENDAR("calendar", "Time of use calendar"),
    ACTIVATION_OPTION("activationOption", "Activation option"),
    ACTIVATION_DATE("activationDate", "Activation date"),
    UPDATE_TYPE("updateType", "Update"),
    VALIDATION_TIMEOUT("validationTimeout", "Timeout before validation(sec)"),
    DEVICE("device", "Device"),
    FULL_CALENDAR("fullCalendar", "Full calendar"),
    SPECIAL_DAYS("specialDays", "Special days"),
    IMMEDIATELY("immediately", "Immediately"),
    WITHOUT_ACTIVATION("withoutActivation", "Without activation"),
    DEVICE_MESSAGE_ID("deviceMessageId", "Device message id"),
    TIME_OF_USE_CAMPAIGN_CPS("TimeOfUseCampaignCustomPropertySet", "Time of use campaign custom property set"),
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
