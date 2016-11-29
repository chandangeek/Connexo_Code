package com.energyict.mdc.device.alarms.rest.i18n;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by albertv on 11/29/2016.
 */
public enum DeviceAlarmTranslationKeys implements TranslationKey {

    ALARM_DOES_NOT_EXIT("AlarmDoesNotExist", "Alarm doesn't exist")
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
}
