/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.config.ConnectionStrategy;

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
    ON_DATE("onDate", "On date"),
    WITHOUT_ACTIVATION("withoutActivation", "Without activation"),
    DEVICE_MESSAGE_ID("deviceMessageId", "Device message id"),
    TIME_OF_USE_CAMPAIGN_CPS("TimeOfUseCampaignCustomPropertySet", "Time of use campaign custom property set"),
    TIME_OF_USE_ITEM_CPS("TimeOfUseCampaignItemCustomPropertySet", "Time of use campaign item custom property set"),
    WITH_UNIQUE_CALENDAR_NAME("withUniqueCalendarName", "With unique calendar name"),
    VALIDATION_COMTASK_ID("validationComTaskId", "Validation communication task id"),
    CALENDAR_UPLOAD_COMTASK_ID("calendarUploadComTaskId", "Calendar upload communication task id"),
    VALIDATION_CONNECTIONSTRATEGY("validationConnectionStrategy", "Validation connection strategy"),
    CALENDAR_UPLOAD_CONNECTIONSTRATEGY("calendarUploadConnectionStrategy", "Calendar upload connection strategy"),
    MANUALLY_CANCELLED("manuallyCancelled", "Manually cancelled"),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS.name(), "Minimize connections"),
    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name(), "As soon as possible"),
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
