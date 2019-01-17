/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME("serviceCall", "Service call"),
    SOURCE("source", "Source"),
    CALLBACK_URL("callbackUrl", "Callback URL"),
    TIME_PERIOD_START("timePeriodStart", "Time period start"),
    TIME_PERIOD_END("timePeriodEnd", "Time period end"),
    READING_TYPES("readingTypes", "Reading Types"),
    PARENT_SERVICE_CALL("parentServiceCall", "Parent service call"),
    END_DEVICE_MRID("endDeviceMRID", "EndDevice mRID"),
    END_DEVICE_NAME("endeDeviceName", "EndDevice name"),
    CHANNELS("channels", "Channels"),
    REGISTERS("registers", "Registers")
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
