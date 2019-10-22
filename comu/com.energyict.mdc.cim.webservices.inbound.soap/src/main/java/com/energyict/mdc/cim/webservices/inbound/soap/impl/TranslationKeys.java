/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;

public enum TranslationKeys implements TranslationKey {
    LOGBOOK_OBIS_CODE("endDeviceEvents.obisCode", "Logbook OBIS code"),
    ALARM_CLOSURE_COMMENT("alarmClosureComment", "Alarm closed on {0} call"),

    DOMAIN_NAME("serviceCall", "Service call"),
    CALL_BACK_URL("callbackUrl", "Callback url"),
    METER_CONFIG("meterConfig", "Meter config"),
    METER_MRID("meterMrid", "Meter mrid"),
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
    GENERAL_ATTRIBUTES("GeneralAttributes", "General attributes"),
    SOURCE("source", "Source"),
    CORRELATION_ID("corelationId", "Correlation id"),
    TIME_PERIOD_START("timePeriodStart", "Time period start"),
    TIME_PERIOD_END("timePeriodEnd", "Time period end"),
    READING_TYPES("readingTypes", "Reading types"),
    LOAD_PROFILES("loadProfiles", "Load profiles"),
    REGISTER_GROUP("registerGroup", "Register group"),
    SCHEDULE_STRAGEGY("scheduleStrategy", "Schedule strategy"),
    CONNECTION_METHOD("connectionMethod", "Connection method name"),
    COMMUNICATION_TASK("communicationTask", "Communication task name"),
    TRIGGER_DATE("triggerDate", "Trigger date"),
    ACTUAL_START_DATE("actualStartDate", "Actual start date"),
    ACTUAL_END_DATE("actualEndDate", "Actual end date"),
    END_DEVICE_NAME("endDevice.name", "Device name"),
    END_DEVICE_MRID("endDevice.mrid", "Device mrid"),
    PGMR_CPS("GM1.name", "Parent get meter readings custom property set"),
    SGMR_CPS("GM2.name", "SubParent get meter readings custom property set"),
    CGMR_CPS("GM3.name", "Child get meter readings custom property set"),
    FUTURE_COM_TASK_EXECUTION_NAME(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_SUBSCRIBER,
                                               FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DISPLAYNAME),
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
