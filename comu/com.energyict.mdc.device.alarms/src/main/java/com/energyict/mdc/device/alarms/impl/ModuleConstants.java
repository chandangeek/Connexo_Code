package com.energyict.mdc.device.alarms.impl;

public final class ModuleConstants {
    private ModuleConstants() {
    }

    public static final String REASON_UNKNOWN_INBOUND_DEVICE = "reason.unknown.inbound.device";
    public static final String REASON_UNKNOWN_OUTBOUND_DEVICE = "reason.unknown.outbound.device";
    public static final String REASON_FAILED_TO_COMMUNICATE = "reason.failed.to.communicate";
    public static final String REASON_CONNECTION_SETUP_FAILED = "reason.connection.setup.failed";
    public static final String REASON_CONNECTION_FAILED = "reason.connection.failed";
    public static final String REASON_POWER_OUTAGE = "reason.power.outage";
    public static final String REASON_TYME_SYNC_FAILED = "reason.tyme.sync.failed";

    public static final String END_DEVICE_EVENT_CREATED = "end.device.event.created";
    public static final String AQ_DEVICE_ALARM_EVENT_SUBSC = "DeviceAlarmCreation";
    public static final String AQ_DEVICE_ALARM_EVENT_DISPLAYNAME = "Create device alarms";

    public static final String DEVICE_IDENTIFIER = "deviceIdentifier";
    public static final String MASTER_DEVICE_IDENTIFIER = "masterDeviceId";
    public static final String SKIPPED_TASK_IDS = "skippedTaskIDs";
    public static final String EVENT_TIMESTAMP = "timestamp";
    public static final String BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION = "basicTemplateDeviceAlarmDescription";
    public static final String PARAMETER_NAME_EVENT_TYPE = "parameterNameEventType";
    public static final String BASIC_TEMPLATE_DEVICE_ALARM_NAME = "basicTemplateDeviceAlarmName";
    public static final String ISSUE_TYPE_DEVICE_ALARM = "issueTypeDeviceAlarm";

    public static final long MDC_AMR_SYSTEM_ID = 1L;

}
