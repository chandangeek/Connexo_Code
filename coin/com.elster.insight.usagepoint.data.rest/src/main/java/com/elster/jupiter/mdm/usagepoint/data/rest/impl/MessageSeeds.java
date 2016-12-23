package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_DEVICE_WITH_NAME(1, Keys.NO_DEVICE_WITH_NAME, "No meter with name {0}"),
    NO_USAGE_POINT_WITH_NAME(2, Keys.NO_USAGE_POINT_WITH_NAME, "No usage point with name {0}"),
    NO_READING_TYPE_FOR_MRID(3, Keys.NO_READING_TYPE_FOR_MRID, "No reading type with MRID {0}"),
    NO_SUCH_CUSTOM_PROPERTY_SET(11, Keys.NO_SUCH_CUSTOM_PROPERTY_SET, "Custom property set with id ''{0}'' not found."),
    END_DATE_MUST_BE_AFTER_START_DATE(13, Keys.END_DATE_MUST_BE_AFTER_START_DATE, "End date must be after start date"),
    NO_SUCH_SERVICE_CATEGORY(14, Keys.NO_SUCH_SERVICE_CATEGORY, "Service category not found"),
    NO_SUCH_TECHNICAL_INFO(15, Keys.NO_SUCH_TECHNICAL_INFO, "Technical information for {0} should be provided"),
    NO_USAGE_POINT_FOR_ID(16, Keys.NO_USAGE_POINT_FOR_ID, "No usage point with ID {0}"),
    BAD_REQUEST(17, Keys.BAD_REQUEST, "Bad request"),
    NO_METROLOGYCONFIG_FOR_ID(18, Keys.NO_METROLOGYCONFIG_FOR_ID, "No metrology configuration with id {0}"),
    USAGE_POINT_LINKED_EXCEPTION(19, Keys.USAGE_POINT_LINKED_EXCEPTION, "Failed to link metrology configuration to usage point ''{0}''."),
    USAGE_POINT_LINKED_EXCEPTION_MSG(20, Keys.USAGE_POINT_LINKED_EXCEPTION_MSG, "Usage point {0} is already linked to a metrology configuration."),
    NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT(21, Keys.NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT,
            "Not possible to link inactive metrology configuration ''{0}'' to usage point"),
    NO_METER_ROLE_FOR_KEY(22, Keys.NO_METER_ROLE_FOR_KEY, "No meter role with key {0}"),
    INVALID_COORDINATES(23, "invalidCoordinates", "All coordinates fields must contain valid values"),
    THIS_FIELD_IS_REQUIRED(24, "ThisFieldIsRequired", "This field is required"),
    NO_METROLOGYCONFIG_FOR_USAGEPOINT(25, "NoMetrologyConfigForUsagePoint", "Usage point {0} doesn''t have a link to metrology configuration."),
    METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT(26, "MetrologyPurposeNotLinkedToUsagePoint", "Metrology contract with id {0} is not found on usage point {1}."),
    NO_SUCH_OUTPUT_FOR_USAGEPOINT(27, "NoSuchOutputForUsagePoint", "Usage point {0} doesn''t have an output with id {1}"),
    THIS_OUTPUT_IS_IRREGULAR(28, "ThisOutputIsIrregular", "Usage point output with id {0} is irregular and can''t provide interval data."),
    THIS_OUTPUT_IS_REGULAR(29, "ThisOutputIsRegular", "Usage point output with id {0} is regular and provides only interval data."),
    NO_RELATIVEPERIOD_FOR_ID(30, "NoRelativePeriodForId", "Relative period with id {0} is not found."),
    RELATIVEPERIOD_IS_IN_THE_FUTURE(31, "RelativePeriodIsInTheFuture", "Cannot gather validation statistics for relative period with id {0}: it is in the future."),
    START_DATE_MUST_BE_GRATER_THAN_UP_CREATED_DATE(32, "version.start.should.be.greater.than.up.creation.date", "Start date must be greater or equal to Created date of usage point"),
    PROCESS_STATUS_PENDING(33, Keys.PROCESS_STATUS_PENDING, "Pending"),
    PROCESS_STATUS_ACTIVE(34, Keys.PROCESS_STATUS_ACTIVE, "Active"),
    PROCESS_STATUS_COMPLETED(35, Keys.PROCESS_STATUS_COMPLETED, "Completed"),
    PROCESS_STATUS_ABORTED(36, Keys.PROCESS_STATUS_ABORDED, "Aborted"),
    PROCESS_STATUS_SUSPENDED(37, Keys.PROCESS_STATUS_SUSPENDED, "Suspended"),
    USAGE_POINT_SEARCH_DOMAIN_NOT_REGISTERED(38, "UsagePointSearchDomainNotRegistered", "Usage point search domain is not registered"),
    AT_LEAST_ONE_SEARCH_CRITERION(39, "AtLeastOneCriterion" , "At least one search criterion has to be provided"),
    SEARCHABLE_PROPERTY_INVALID_VALUE(40, "SearchablePropertyInvalidValue", "Invalid value"),
    NO_SUCH_METROLOGY_PURPOSE(41, Keys.NO_SUCH_METROLOGY_PURPOSE, "No such metrology purpose with ID {0}"),
    CANNOT_ACTIVATE_METROLOGY_PURPOSE(42, Keys.CANNOT_ACTIVATE_METROLOGY_PURPOSE, "Can''t activate metrology purpose with ID {0}"),
    UNSATISFIED_READING_TYPE_REQUIREMENTS(43, Keys.UNSATISFIED_READING_TYPE_REQUIREMENTS, "Meters don''t provide reading types specified in the metrology contract."),
    NO_SUCH_CALENDAR(44, "NoSuchCalendar", "No such calendar."),
    NO_APPSERVER(45, "usagepoint.bulk.no.appserver.to.serve", "There is currently no active application server that can handle this request"),
    NO_SUCH_MESSAGE_QUEUE(46, "usagepoint.bulk.no.such.messagequeue", "Unable to queue command: no message queue was found"),
    BAD_ACTION(47, "usagepoint.bulk.no.such.action", "Unexpected or non existing action : {0}"),
    FLAG_AS_FAVORITE_CONFLICT_TITLE(48, "FlagAsFavoriteConflictTitle", "Failed to flag ''{0}'' as favorite"),
    REMOVE_FROM_FAVORITES_CONFLICT_TITLE(49, "RemoveFromFavoritesConflictTitle", "Failed to remove ''{0}'' from the favorites");

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
        public static final String NO_DEVICE_WITH_NAME = "NoDeviceWithName";
        public static final String NO_USAGE_POINT_WITH_NAME = "NoUsagePointWithName";
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
        public static final String PROCESS_STATUS_PENDING = "ProcessStatusPending";
        public static final String PROCESS_STATUS_ACTIVE = "ProcessStatusActive";
        public static final String PROCESS_STATUS_COMPLETED = "ProcessStatusCompleted";
        public static final String PROCESS_STATUS_ABORDED = "ProcessStatusAborted";
        public static final String PROCESS_STATUS_SUSPENDED = "ProcessStatusSuspended";
        public static final String NO_SUCH_METROLOGY_PURPOSE = "NoSuchMetrologyPurpose";
        public static final String CANNOT_ACTIVATE_METROLOGY_PURPOSE = "CannotActivateMetrologyPurpose";
        public static final String UNSATISFIED_READING_TYPE_REQUIREMENTS = "UnsatisfiedReadingTypeRequirements";
    }

}
