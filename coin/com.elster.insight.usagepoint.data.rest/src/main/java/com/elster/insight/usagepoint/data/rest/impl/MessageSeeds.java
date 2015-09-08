package com.elster.insight.usagepoint.data.rest.impl;

import java.util.logging.Level;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    NO_DEVICE_FOR_MRID(101, Keys.NO_DEVICE_FOR_MRID, "No device with MRID {0}"),
    NO_USAGE_POINT_FOR_MRID(102, Keys.NO_USAGE_POINT_FOR_MRID, "No usage point with MRID {0}"), 
    NO_READING_TYPE_FOR_MRID(103, Keys.NO_READING_TYPE_FOR_MRID, "No reading type with MRID {0}"),
//    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    ;
    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return UsagePointApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static class Keys {

        public static final String NO_DEVICE_FOR_MRID = "NoDeviceForMRID";
        public static final String NO_USAGE_POINT_FOR_MRID = "NoUsagePointForMRID";
        public static final String NO_READING_TYPE_FOR_MRID = "NoReadingTypeForMRID";

    }

}
