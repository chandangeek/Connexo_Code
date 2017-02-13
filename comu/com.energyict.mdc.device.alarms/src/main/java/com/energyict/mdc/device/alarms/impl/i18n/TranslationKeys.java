/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.i18n;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.actions.CloseDeviceAlarmAction;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

public enum TranslationKeys implements TranslationKey {
    AQ_DEVICE_ALARM_EVENT_SUBSC(ModuleConstants.AQ_DEVICE_ALARM_EVENT_SUBSC, ModuleConstants.AQ_DEVICE_ALARM_EVENT_DISPLAYNAME),
    END_DEVICE_EVENT_CREATED(ModuleConstants.END_DEVICE_EVENT_CREATED, "EndDeviceEvent"),
    BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION(ModuleConstants.BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION, ModuleConstants.BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION),
    BASIC_TEMPLATE_DEVICE_ALARM_NAME(ModuleConstants.BASIC_TEMPLATE_DEVICE_ALARM_NAME, "Device Alarm Template"),
    PARAMETER_NAME_EVENT_TYPE(BasicDeviceAlarmRuleTemplate.EVENTTYPE, "Event"),
    ISSUE_TYPE_DEVICE_ALARM(ModuleConstants.ISSUE_TYPE_DEVICE_ALARM, "Device Alarm"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseDeviceAlarmAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseDeviceAlarmAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ALARM_WAS_CLOSED("action.alarm.was.closed", "Alarm was closed"),
    CLOSE_ACTION_ALARM_ALREADY_CLOSED("action.alarm.already.closed", "Alarm already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("alarm.action.closeAlarm", "Close alarm"),
    ALARM_REASON(ModuleConstants.ALARM_REASON, "Alarm reason"),
    ALARM_REASON_DESCRIPTION(ModuleConstants.ALARM_REASON_DESCRIPTION, "Alarm reason {0}"),
    ACTION_ASSIGN_ALARM("alarm.action.assignAlarm", "Assign alarm"),
    EVENT_TEMPORAL_THRESHOLD(ModuleConstants.EVENT_TEMPORAL_THRESHOLD, "Event time threshold"),
    RAISE_EVENT_PROPS(ModuleConstants.RAISE_EVENT_PROPS, "On new raise event"),
    TRIGGERING_EVENTS(ModuleConstants.TRIGGERING_EVENTS, "Alarm triggering events"),
    CLEARING_EVENTS(ModuleConstants.CLEARING_EVENTS, "Alarm clearing events"),
    EVENT_OCCURENCE_COUNT(ModuleConstants.EVENT_OCCURENCE_COUNT, "Event occurence count"),
    DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES(ModuleConstants.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, "Device lifecycle state in device type "),
    ALARM_RELATIVE_PERIOD_CATEGORY(ModuleConstants.ALARM_RELATIVE_PERIOD_CATEGORY, "Alarms")
    ;
    //TODO - update list

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