package com.elster.insight.usagepoint.data.rest.impl;

import java.util.logging.Level;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    NO_DEVICE_FOR_MRID(101, Keys.NO_DEVICE_FOR_MRID, "No device with MRID {0}"),
    NO_USAGE_POINT_FOR_MRID(102, Keys.NO_USAGE_POINT_FOR_MRID, "No usage point with MRID {0}"), 
    NO_READING_TYPE_FOR_MRID(103, Keys.NO_READING_TYPE_FOR_MRID, "No reading type with MRID {0}"),
    NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID(104, Keys.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, "No current meter activation for usage point with MRID {0}"), 
    NO_REGISTER_FOR_USAGE_POINT_FOR_MRID(105, Keys.NO_REGISTER_FOR_USAGE_POINT_FOR_MRID, "No register for usage point with MRID {0} with reading type {1}"),
    NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID(106, Keys.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, "No channel for usage point with MRID {0} with reading type {1}"),
    NULL_DATE(107, Keys.NULL_DATE, "Date must be filled in"),
    NO_ACTIVATION_FOR_USAGE_POINT_FOR_MRID_AT_TIME(108, Keys.NO_ACTIVATION_FOR_USAGE_POINT_FOR_MRID_AT_TIME, "No meter activation for usage point with MRID {0} at instant {1}"),
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(109, Keys.DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE, "Deactivate of validation rule set {0} is currently not possible."),
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
        public static final String NULL_DATE = "NullDate";
        public static final String DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE = "DeactivateValidationRuleSetNotPossible";
        public static final String NO_DEVICE_FOR_MRID = "NoDeviceForMRID";
        public static final String NO_USAGE_POINT_FOR_MRID = "NoUsagePointForMRID";
        public static final String NO_READING_TYPE_FOR_MRID = "NoReadingTypeForMRID";
        public static final String NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID="NoCurrentActivationForUsagePointForMRID";
        public static final String NO_REGISTER_FOR_USAGE_POINT_FOR_MRID = "NoRegisterForUsagePointForMRID";
        public static final String NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID = "NoChannelForUsagePointForMRID";
        public static final String NO_ACTIVATION_FOR_USAGE_POINT_FOR_MRID_AT_TIME="NoActivationForUsagePointForMRIDAtTime";
        
    }

}
