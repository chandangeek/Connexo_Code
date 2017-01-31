/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.alarms;

import com.elster.jupiter.nls.TranslationKey;


public enum TranslationKeys implements TranslationKey {
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
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
