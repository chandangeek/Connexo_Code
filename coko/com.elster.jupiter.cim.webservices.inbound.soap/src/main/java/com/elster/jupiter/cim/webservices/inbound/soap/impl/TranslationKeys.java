/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.task.ReadMeterChangeMessageHandlerFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME("serviceCall", "Service call"),
    CALLBACK_URL("callbackUrl", "Callback URL"),
    METER_INFO("meterInfo", "Meter info"),
    USAGE_POINT_INFO("usagePointInfo", "Usage point info"),
    CONFIGURATION_EVENT("configurationEvent", "Configuration event"),
    PARENT_SERVICE_CALL("parentServiceCall", "Parent service call"),
    REQUEST_TIMESTAMP("requestTimestamp", "Request timestamp"),
    ERROR_MESSAGE("errorMessage", "Error message"),
    ERROR_CODE("errorCode", "Error code"),
    OPERATION("operation", "Operation"),
    CALLS_SUCCESS("callsSuccess", "Success calls counter"),
    CALLS_ERROR("callsError", "Error calls counter"),
    CALLS_EXPECTED("callsExpected", "Expected calls counter"),
    FROM_DATE("fromDate", "From date"),
    TO_DATE("toDate", "To date"),
    SOURCE("source", "Source"),
    TIME_PERIOD_START("timePeriodStart", "Time period start"),
    TIME_PERIOD_END("timePeriodEnd", "Time period end"),
    READING_TYPES("readingTypes", "Reading Types"),
    END_DEVICES("endDevices", "End Devices"),
    GMR_NAME(ParentGetMeterReadingsCustomPropertySet.PREFIX + ".name", "Get meter readings"),
    READ_METER_CHANGE_MESSAGE_HANDLER(
            ReadMeterChangeMessageHandlerFactory.TASK_SUBSCRIBER,
            ReadMeterChangeMessageHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME);

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
