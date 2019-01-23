/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.task.ReadMeterChangeMessageHandlerFactory;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME("serviceCall", "Service call"),
    SOURCE("source", "Source"),
    CALLBACK_URL("callbackUrl", "Callback URL"),
    TIME_PERIOD_START("timePeriodStart", "Time period start"),
    TIME_PERIOD_END("timePeriodEnd", "Time period end"),
    READING_TYPES("readingTypes", "Reading Types"),
    END_DEVICES("endDevices", "End Devices"),
    READ_METER_CHANGE_MESSAGE_HANDLER(ReadMeterChangeMessageHandlerFactory.TASK_SUBSCRIBER,
            ReadMeterChangeMessageHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME),
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
