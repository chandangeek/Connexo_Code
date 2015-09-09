package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceMessageUserAction;

import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (14:59)
 */
public enum DeviceMessageExecutionLevelTranslationKeys implements TranslationKey {

    LEVEL_1(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, "Level 1"),
    LEVEL_2(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2, "Level 2"),
    LEVEL_3(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3, "Level 3"),
    LEVEL_4(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4, "Level 4");

    private final DeviceMessageUserAction level;
    private final String defaultFormat;

    DeviceMessageExecutionLevelTranslationKeys(DeviceMessageUserAction level, String defaultFormat) {
        this.level = level;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return this.level.getPrivilege();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static DeviceMessageExecutionLevelTranslationKeys from(String privilege) {
        return Stream
                .of(values())
                .filter(k -> k.level.getPrivilege().equals(privilege))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown or unsupported device message execution privilege level:" + privilege));
    }

}