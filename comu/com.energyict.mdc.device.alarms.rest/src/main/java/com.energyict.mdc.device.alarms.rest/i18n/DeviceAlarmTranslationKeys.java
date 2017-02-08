/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.i18n;

import com.elster.jupiter.nls.TranslationKey;


public enum DeviceAlarmTranslationKeys implements TranslationKey {

    ALARM_ACTION_PHASE_CREATE("DeviceAlarmActionPhaseCreation", "Alarm creation"),
    ALARM_ACTION_PHASE_OVERDUE("DeviceAlarmActionPhaseOverdue", "Alarm overdue"),
    ALARM_ACTION_PHASE_CREATE_DESCRIPTION("DeviceAlarmActionPhaseCreationDescription", "The action will be performed at the alarm creation time"),
    ALARM_ACTION_PHASE_OVERDUE_DESCRIPTION("DeviceAlarmActionPhaseOverdueDescription", "The action will be performed when the alarm becomes overdue"),
    ALARM_ASSIGNEE_UNASSIGNED ("AlarmAssigneeUnassigned", "Unassigned"),
    ALARM_DOES_NOT_EXIT("AlarmDoesNotExist", "Alarm doesn't exist"),
    ALARM_ALREADY_CLOSED("AlarmAlreadyClosed", "Alarm already closed")
            ;

    private final String value;
    private final String defaultFormat;

    DeviceAlarmTranslationKeys(String value, String defaultFormat) {
        this.value = value;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public String getKey() {
        return this.value;
    }

    public static DeviceAlarmTranslationKeys from(String key) {
        if (key != null) {
            for (DeviceAlarmTranslationKeys translationKey : DeviceAlarmTranslationKeys.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }
}
