/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey {
    //Resources
    RESOURCE_USAGE_POINT("metering.usagePoint", "Usage points"),
    RESOURCE_USAGE_POINT_DESCRIPTION("metering.usagePoint.description", "Manage usage points"),
    RESOURCE_READING_TYPE("metering.readingTypes", "Reading types"),
    RESOURCE_READING_TYPE_DESCRIPTION("metering.readingTypes.description", "Manage reading types"),
    RESOURCE_SERVICE_CATEGORY("metering.serviceCategory", "Service categories"),
    RESOURCE_SERVICE_CATEGORY_DESCRIPTION("metering.serviceCategory.description", "Manage service categories"),
    RESOURCE_METROLOGY_CONFIGURATION("usagePoint.metrologyConfiguration", "Metrology configurations"),
    RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION("usagePoint.metrologyConfiguration.description", "Manage metrology configurations"),

    SWITCH_STATE_MACHINE_SUBSCRIBER(SwitchStateMachineEvent.SUBSCRIBER, SwitchStateMachineEvent.SUBSCRIBER_TRANSLATION),;

    private String key;
    private String format;

    DefaultTranslationKey(String key, String format) {
        this.key = key;
        this.format = format;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }
}