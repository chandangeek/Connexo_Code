/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    LOGBOOK_OBIS_CODE("endDeviceEvents.obisCode", "Logbook OBIS code"),
    ALARM_CLOSURE_COMMENT("alarmClosureComment", "Alarm closed on {0} call"),

    DOMAIN_NAME("serviceCall", "Service call"),
    CALL_BACK_URL("callbackUrl", "Callback URL"),
    CORRELATION_ID("correlationId", "Correlation id"),
    METER_CONFIG("meterConfig", "Meter config"),
    METER_MRID("meterMrid", "Meter MRID"),
    METER_NAME("meterName", "Meter name"),
    PARENT_SERVICE_CALL("parentServiceCall", "Parent service call"),
    ERROR_MESSAGE("errorMessage", "Error message"),
    ERROR_CODE("errorCode", "Error code"),
    OPERATION("operation", "Operation"),
    CALLS_SUCCESS("callsSuccess", "Success calls counter"),
    CALLS_ERROR("callsError", "Error calls counter"),
    CALLS_EXPECTED("callsExpected", "Expected calls counter"),
    FROM_DATE("fromDate", "From date"),
    TO_DATE("toDate", "To date"),
    GENERAL_ATTRIBUTES("GeneralAttributes", "General attributes")
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
