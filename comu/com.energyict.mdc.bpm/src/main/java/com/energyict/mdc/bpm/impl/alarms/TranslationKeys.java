package com.energyict.mdc.bpm.impl.alarms;

import com.elster.jupiter.nls.TranslationKey;


public enum TranslationKeys implements TranslationKey {
    DEVICE_ALARM_ASSOCIATION_PROVIDER(DeviceAlarmProcessAssociationProvider.ASSOCIATION_TYPE, "Device alarm"),
    DEVICE_ALARM_REASON_TITLE("alarmReasons", "Device alarm reasons"),
    DEVICE_ALARM_REASON_COLUMN("alarmReason", "Device alarm reason");

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
