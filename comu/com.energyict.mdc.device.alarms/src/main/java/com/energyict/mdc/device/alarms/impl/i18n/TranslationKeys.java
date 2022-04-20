/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.i18n;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmProcessAssociationProvider;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.actions.CloseDeviceAlarmAction;

public enum TranslationKeys implements TranslationKey {
    AQ_DEVICE_ALARM_EVENT_SUBSC(ModuleConstants.AQ_DEVICE_ALARM_EVENT_SUBSC, ModuleConstants.AQ_DEVICE_ALARM_EVENT_DISPLAYNAME),
    DEVICE_TYPES_CHANGES_EVENT_SUBSC(ModuleConstants.DEVICE_TYPES_CHANGES_SUBSC, ModuleConstants.DEVICE_TYPES_CHANGES_DISPLAYNAME),
    END_DEVICE_EVENT_CREATED(ModuleConstants.END_DEVICE_EVENT_CREATED, "EndDeviceEvent"),
    BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION(ModuleConstants.BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION, "Create an alarm based on device events"),
    BASIC_TEMPLATE_DEVICE_ALARM_NAME(ModuleConstants.BASIC_TEMPLATE_DEVICE_ALARM_NAME, "Device alarm template"),
    ISSUE_TYPE_DEVICE_ALARM(ModuleConstants.ISSUE_TYPE_DEVICE_ALARM, "Device alarm"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseDeviceAlarmAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseDeviceAlarmAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ALARM_CLOSED("action.alarm.closed", "Alarm closed"),
    CLOSE_ACTION_ALARM_ALREADY_CLOSED("action.alarm.already.closed", "Alarm already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("alarm.action.closeAlarm", "Close alarm"),
    ALARM_REASON_DESCRIPTION(ModuleConstants.ALARM_REASON_DESCRIPTION, "Alarm reason {0}"),
    ACTION_ASSIGN_ALARM("alarm.action.assignAlarm", "Assign alarm"),
    ACTION_MAIL_NOTIFY("alarm.action.email", "Email"),
    ACTION_MAIL_TO("alarm.action.mail.to","To"),
    ACTION_ALARM_ASSIGNED("action.alarm.assigned", "Alarm assigned"),
    ACTION_ALARM_UNASSIGNED("action.alarm.unassigned", "Alarm unassigned"),
    ACTION_START_ALARM_START_PROCESS("alarm.action.startProcess", "Start process"),
    ACTION_START_ALARM_PROCESS("alarm.action.process", "Process"),
    EVENT_TEMPORAL_THRESHOLD(ModuleConstants.EVENT_TEMPORAL_THRESHOLD, "Event time threshold"),
    RAISE_EVENT_PROPS(ModuleConstants.RAISE_EVENT_PROPS, "On new raise event"),
    TRIGGERING_EVENTS(ModuleConstants.TRIGGERING_EVENTS, "Raised on event types"),
    CLEARING_EVENTS(ModuleConstants.CLEARING_EVENTS, "Cleared on event types"),
    DEVICE_IN_GROUP(ModuleConstants.DEVICE_IN_GROUP, "Device group"),
    DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES(ModuleConstants.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, "Device lifecycle state in device type "),
    ALARM_RELATIVE_PERIOD_CATEGORY(ModuleConstants.ALARM_RELATIVE_PERIOD_CATEGORY, "Device alarm"),
    ALARM_REASON_UNKNOWN(ModuleConstants.ALARM_REASON_UNKNOWN, "Alarm Reason Unknown"),
    ACTION_WEBSERVICE_NOTIFICATION_CALLED("alarm.action.webServiceNotificationCalled", "Web service notification called"),
    ACTION_WEBSERVICE_NOTIFICATION("alarm.action.webServiceNotification", "Web service notification"),
    ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ALARM("alarm.action.webServiceNotificationAlarm.closeAlarm", "Close alarm"),
    ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ALARM_DESCRIPTION("alarm.action.webServiceNotification.close.description", "Select to remove the alarm from operational screens in Connexo (e.g. dashboard, overviews, what''s going on). It will receive the status ''Forwarded''."),
    UNASSIGNED(DatabaseConst.UNASSIGNED, "Unassigned"),
    ACTION_WEBSERVICE_NOTIFICATION_CALLED_FAILED("alarm.action.webServiceNotification.call.failed", "Web service notification call failed"),
    ACTION_WEBSERVICE_NOTIFICATION_ENDPOINT_DOES_NOT_EXIST("alarm.action.webServiceNotification.endpoint.empty", "Web service does not exist in the system"),
    ACTION_WEBSERVICE_NOTIFICATION_ENDPOINT_CONFIGURATION_DOES_NOT_EXIST("alarm.action.webServiceNotification.endpoint.configuration", "Web service configuration does not exist in the systme"),
    PROCESS_ACTION("issue.action.processAction", "Process action"),
    PROCESS_ACTION_SUCCESS("issue.action.processAction.success", "Process successufuly called"),
    PROCESS_ACTION_FAIL("issue.action.processAction.fail", "Process call failed"),
    PROCESS_ACTION_PROCESS_IS_ABSENT("issue.action.processAction.process.absent", "Process that you called does not exist in system"),
    PROCESS_ACTION_PROCESS_COMOBOX_IS_ABSENT("issue.action.processAction.combobox.absent", "Process combobox is absent"),

    DEVICE_ALARM_ASSOCIATION_PROVIDER(DeviceAlarmProcessAssociationProvider.ASSOCIATION_TYPE, "Alarm"),
    DEVICE_ALARM_REASON_TITLE("alarmReasons", "Alarm reasons"),
    DEVICE_ALARM_REASON_COLUMN("alarmReason", "Alarm reason");


    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}
