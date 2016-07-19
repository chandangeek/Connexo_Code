package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    NO_DEVICE_FOR_MRID(1, Keys.NO_DEVICE_FOR_MRID, "No meter with MRID {0}"),
    NO_USAGE_POINT_FOR_MRID(2, Keys.NO_USAGE_POINT_FOR_MRID, "No usage point with MRID {0}"),
    NO_READING_TYPE_FOR_MRID(3, Keys.NO_READING_TYPE_FOR_MRID, "No reading type with MRID {0}"),
    NO_SUCH_CUSTOM_PROPERTY_SET(11, Keys.NO_SUCH_CUSTOM_PROPERTY_SET, "Custom property set with id ''{0}'' not found."),
    END_DATE_MUST_BE_AFTER_START_DATE(13, Keys.END_DATE_MUST_BE_AFTER_START_DATE, "End date must be after start date"),
    NO_SUCH_SERVICE_CATEGORY(14, Keys.NO_SUCH_SERVICE_CATEGORY, "Service category not found"),
    NO_SUCH_TECHNICAL_INFO(15, Keys.NO_SUCH_TECHNICAL_INFO, "Technical information for {0} should be provided"),
    NO_USAGE_POINT_FOR_ID(16, Keys.NO_USAGE_POINT_FOR_ID, "No usage point with ID {0}"),
    BAD_REQUEST(17, Keys.BAD_REQUEST, "Bad request"),
    NO_METROLOGYCONFIG_FOR_ID(18, Keys.NO_METROLOGYCONFIG_FOR_ID, "No metrology configuration with id {0}"),
    USAGE_POINT_LINKED_EXCEPTION(19, Keys.USAGE_POINT_LINKED_EXCEPTION, "Failed to link metrology configuration to ''{0}''."),
    USAGE_POINT_LINKED_EXCEPTION_MSG(20, Keys.USAGE_POINT_LINKED_EXCEPTION_MSG, "{0} is already linked to a metrology configuration."),
    NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT(21, Keys.NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT, "Not possible to link inactive metrology configuration ''{0}'' to usage point"),
    NO_METER_ROLE_FOR_KEY(22, Keys.NO_METER_ROLE_FOR_KEY, "No meter role with key {0}"),
    INVALID_COORDINATES(23, "invalidCoordinates", "All coordinates fields must contain valid values"),
    THIS_FIELD_IS_REQUIRED(24, "ThisFieldIsRequired", "This field is required"),
    NO_METROLOGYCONFIG_FOR_USAGEPOINT(25, "NoMetrologyConfigForUsagePoint", "Usage point with MRID {0} doesn''t have a link to metrology configuration."),
    METROLOGYPURPOSE_IS_NOT_LINKED_TO_USAGEPOINT(26, "MetrologyPurposeNotLinkedToUsagePoint", "Metrology purpose with id {0} is not found on usage point with MRID {1}."),
    NO_SUCH_OUTPUT_FOR_USAGEPOINT(27, "NoSuchOutputForUsagePoint", "Usage point with MRID {0} doesn't have an output with id {1}"),
    THIS_OUTPUT_IS_IRREGULAR(27, "ThisOutputIsIrregular", "Usage point output with id {0} is irregular and can''t provide interval data."),
    THIS_OUTPUT_IS_REGULAR(28, "ThisOutputIsRegular", "Usage point output with id {0} is regular and provides only interval data."),;

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
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
        public static final String NO_USAGE_POINT_FOR_ID = "NoUsagePointForID";
        public static final String NO_READING_TYPE_FOR_MRID = "NoReadingTypeForMRID";
        public static final String NO_SUCH_CUSTOM_PROPERTY_SET = "NoSuchCustomPropertySet";
        public static final String END_DATE_MUST_BE_AFTER_START_DATE = "EndDateMusBeAfterStartDate";
        public static final String NO_SUCH_SERVICE_CATEGORY = "NoSuchServiceCtegory";
        public static final String NO_SUCH_TECHNICAL_INFO = "NoSuchTechInfo";
        public static final String BAD_REQUEST = "BadRequest";
        public static final String NO_METROLOGYCONFIG_FOR_ID = "NoMetrologyConfigWithId";
        public static final String USAGE_POINT_LINKED_EXCEPTION = "UsagePointLinkedException";
        public static final String USAGE_POINT_LINKED_EXCEPTION_MSG = "UsagePointLinkedExceptionMsg";
        public static final String NO_METER_ROLE_FOR_KEY = "NoMeterRoleForKey";
        public static final String NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT = "UsagePointLinkedInactiveMetrologyConfigurationExceptionMsg";
    }

}
