package com.energyict.mdc.device.data.exception;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;

import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 03/03/14
 * Time: 16:01
 */
public enum MessageSeeds implements MessageSeed {

    DEVICE_NAME_IS_REQUIRED(1001, "device.name.required", "The name of a device is required", Level.SEVERE),
    DUPLICATE_DEVICE_EXTERNAL_NAME(1002, Constants.DUPLICATE_DEVICE_EXTERNAL_KEY, "All device must have a unique external name", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return DeviceDataService.COMPONENTNAME;
    }

    public static class Constants {
        public static final String NAME_REQUIRED_KEY = "DDC.X.name.required";
        public static final String DUPLICATE_DEVICE_EXTERNAL_KEY = "DDC.device.duplicateExternalName";

    }
}
