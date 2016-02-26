package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    NO_DEVICE_FOR_MRID(1, Keys.NO_DEVICE_FOR_MRID, "No device with MRID {0}"),
    NO_USAGE_POINT_FOR_MRID(2, Keys.NO_USAGE_POINT_FOR_MRID, "No usage point with MRID {0}"),
    NO_READING_TYPE_FOR_MRID(3, Keys.NO_READING_TYPE_FOR_MRID, "No reading type with MRID {0}"),
    NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID(4, Keys.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, "No current meter activation for usage point with MRID {0}"),
    NO_REGISTER_FOR_USAGE_POINT_FOR_MRID(5, Keys.NO_REGISTER_FOR_USAGE_POINT_FOR_MRID, "No register for usage point with MRID {0} with reading type {1}"),
    NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID(6, Keys.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, "No channel for usage point with MRID {0} with reading type {1}"),
    NULL_DATE(7, Keys.NULL_DATE, "Date must be filled in"),
    NO_ACTIVATION_FOR_USAGE_POINT_FOR_MRID_AT_TIME(8, Keys.NO_ACTIVATION_FOR_USAGE_POINT_FOR_MRID_AT_TIME, "No meter activation for usage point with MRID {0} at instant {1}"),
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(9, Keys.DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE, "Deactivate of validation rule set {0} is currently not possible."),
    NO_SUCH_READING_ON_REGISTER(10, Keys.NO_SUCH_READING_ON_REGISTER, "Register for reading type {0} has no reading with timestamp {1}"),
    NO_SUCH_CUSTOM_PROPERTY_SET(11, Keys.NO_SUCH_CUSTOM_PROPERTY_SET, "Custom property set with id ''{0}'' not found."),
    CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED(12, Keys.CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED, "Custom property set ''{0}'' is not versioned."),
    END_DATE_MUST_BE_AFTER_START_DATE(13, Keys.END_DATE_MUST_BE_AFTER_START_DATE, "End date must be after start date"),
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
        public static final String NO_SUCH_READING_ON_REGISTER = "NoSuchReadingOnRegister";
        public static final String NO_SUCH_CUSTOM_PROPERTY_SET = "NoSuchCustomPropertySet";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED = "CustomPropertySetIsNotVersioned";
        public static final String END_DATE_MUST_BE_AFTER_START_DATE = "EndDateMusBeAfterStartDate";

    }

}
