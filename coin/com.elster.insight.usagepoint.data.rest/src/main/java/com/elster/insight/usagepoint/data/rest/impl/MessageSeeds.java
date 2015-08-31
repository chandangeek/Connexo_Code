package com.elster.insight.usagepoint.data.rest.impl;

import java.util.logging.Level;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    NO_DEVICE_FOR_MRID(101, Keys.NO_DEVICE_FOR_MRID, "No device with MRID {0}"),
    NO_USAGE_POINT_FOR_MRID(101, Keys.NO_USAGE_POINT_FOR_MRID, "No usage point with MRID {0}"),
//    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
//    NO_SUCH_PARTIAL_CONNECTION_TASK(16, "NoSuchPartialConnectionTask", "No such connection method on device config"),
//    NO_SUCH_CONNECTION_METHOD(17, "NoSuchConnectionTask" , "Device {0} has no connection method {1}"),
//    NO_SUCH_REGISTER(18, "NoSuchRegister" , "No register with id {0}"),
//    DEVICE_VALIDATION_BULK_MSG(20, "DeviceValidationBulkMessage" , "This bulk operation for {0} schedule on {1} device is invalid"),
//    NO_SUCH_READING(21, "NoSuchReading" , "Register {0} has no reading with id {1}"),
//    INVALID_DATE(22, "InvalidDate", "Date should be less or equal to {0}"),
//    NO_SUCH_LOAD_PROFILE_ON_DEVICE(23, "NoSuchLoadProfile", "Device {0} has no load profile {1}"),
//    NO_SUCH_CHANNEL_ON_LOAD_PROFILE(30, "NoSuchChannel", "Load profile {0} has no channel {1}"),
//    NO_CHANNELS_ON_REGISTER(72, "NoChannelsOnRegister", "Register {0} has no channels"),
//    NO_SUCH_READING_ON_REGISTER(73, "NoSuchReadingOnRegister", "Register {0} has no reading with timestamp {1}"),
//    NO_SUCH_LOG_BOOK_ON_DEVICE(24, "NoSuchLogBook", "Device {0} has no log book {1}"),
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

    }

}
