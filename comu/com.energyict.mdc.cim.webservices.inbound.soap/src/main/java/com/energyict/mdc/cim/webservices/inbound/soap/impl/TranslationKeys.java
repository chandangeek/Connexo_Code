/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.cim.webservices.inbound.soap.task.EndDeviceControlsCancellationHandlerFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;

public enum TranslationKeys implements TranslationKey {
    LOGBOOK_OBIS_CODE("endDeviceEvents.obisCode", "Logbook OBIS code"),
    ALARM_CLOSURE_COMMENT("alarmClosureComment", "Alarm closed on {0} call"),

    DOMAIN_NAME("serviceCall", "Service call"),
    CALL_BACK_URL("callbackUrl", "Callback url"),
    METER_CONFIG("meterConfig", "Meter config"),
    METER_MRID("meterMrid", "Meter mrid"),
    METER_NAME("meterName", "Meter name"),
    METERS("meters", "Meters"),
    DEVICE_GROUPS("deviceGroups", "Device groups"),
    EVENT_TYPES("eventTypes", "Event types"),
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
    RESPONSE_STATUS("responseStatus", "Response status"),
    PGMR_CPS("GM1.name", "Parent get meter readings custom property set"),
    SGMR_CPS("GM2.name", "SubParent get meter readings custom property set"),
    CGMR_CPS("GM3.name", "Child get meter readings custom property set"),
    FUTURE_COM_TASK_EXECUTION_NAME(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_SUBSCRIBER,
            FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DISPLAYNAME),
    END_DEVICE_CONTROLS_CANCELLATION_NAME(EndDeviceControlsCancellationHandlerFactory.END_DEVICE_CONTROLS_CANCELLATION_TASK_SUBSCRIBER,
            EndDeviceControlsCancellationHandlerFactory.END_DEVICE_CONTROLS_CANCELLATION_TASK_DISPLAYNAME),
    COMMAND_CODE("commandCode", "Command code"),
    COMMAND_ATTRIBUTES("commandAttributes", "Command attributes"),
    DEVICE_NAME("deviceName", "Device name"),
    DEVICE_MRID("deviceMrid", "Device MRID"),
    CANCELLATION_REASON("cancellationReason", "Cancellation reason"),
    ERROR("error", "Error"),
    MAX_EXEC_TIMEOUT("maxExecTimeout", "Maximum execution timeout"),

    CARD_FORMAT("property.cardformat", "Card format"),
    CARD_FORMAT_FULL_SIZE("property.cardformat.fullsize", "Full-size (1FF)"),
    CARD_FORMAT_MINI("property.cardformat.mini", "Mini (2FF)"),
    CARD_FORMAT_MICRO("property.cardformat.micro", "Micro (3FF)"),
    CARD_FORMAT_NANO("property.cardformat.nano", "Nano (4FF)"),
    CARD_FORMAT_EMBEDDED("property.cardformat.embedded", "Embedded (e-SIM)"),
    CARD_FORMAT_SW("property.cardformat.sw", "SW SIM (software SIM)"),

    STATUS("property.status", "Status"),
    STATUS_ACTIVE("property.status.active", "Active"),
    STATUS_DEMOLISHED("property.status.demolished", "Demolished"),
    STATUS_INACTIVE("property.status.inactive", "Inactive"),
    STATUS_PRE_ACTIVE("property.status.pre.active", "Pre-active"),
    STATUS_TEST("property.status.test", "Test"),

    MEDC_CPS("servicecall.cps.master.end.device.controls", "Master end device controls custom property set"),
    SEDC_CPS("servicecall.cps.submaster.end.device.controls", "SubMaster end device controls custom property set"),
    CEDC_CPS("servicecall.cps.child.end.device.controls", "Child end device controls custom property set"),
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
