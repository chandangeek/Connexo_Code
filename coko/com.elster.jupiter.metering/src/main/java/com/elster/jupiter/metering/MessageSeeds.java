/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    ILLEGAL_MRID_FORMAT(1001, "mrid.illegalformat", "Supplied MRID ''{0}'' is not the correct format."),

    NO_CHANNEL_WITH_ID(3003, Constants.NO_CHANNEL_WITH_ID, "No channel with id {0}"),
    NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT(3004, Constants.NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT, "No effective metrology configuration on usage point {0}"),
    NO_READING_FOUND(3005, Constants.NO_READING_FOUND, "No reading found"),
    METER_ROLE_NOT_IN_CONFIGURATION(3006, Constants.METER_ROLE_NOT_IN_CONFIGURATION, "Meter role {0} is not part of the metrology configuration that applies to the meter activation period"),

    CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER(4004, Constants.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, "The custom attribute set ''{0}'' is not editable by current user."),
    NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT(4005, Constants.NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT, "The custom attribute set ''{0}'' is not attached to the usage point."),
    CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN(4006, Constants.CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN, "The custom attribute set ''{0}'' has different domain type."),
    CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED(4007, Constants.CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED, "The custom attribute set ''{0}'' is not versioned."),

    CONTRACT_NOT_ACTIVE(6000, Constants.CONTRACT_NOT_ACTIVE, "The metrology contract with purpose {0} is not active on usage point ''{1}'' during the requested data aggregation period ({2})"),
    VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS(6001, Constants.VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS, "Unmeasured usage points only support constants and custom attributes or operations and functions that operate on those"),

    UNSATISFIED_READING_TYPE_REQUIREMENTS(7004, Constants.UNSATISFIED_READING_TYPE_REQUIREMENTS, "Meters don''t provide reading types specified in the metrology configuration."),
    START_DATE_SHOULD_BE_GREATER_THAN_LATEST_START_DATE(7006, Constants.START_DATE_SHOULD_BE_GREATER_THAN_LATEST_START_DATE, "Start date should be greater than Start date of the latest metrology configuration version."),
    START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE(7007, Constants.START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE, "Start date should be greater than or equal to End date of the latest metrology configuration version."),
    END_DATE_MUST_BE_GREATER_THAN_START_DATE(7008, Constants.END_DATE_MUST_BE_GREATER_THAN_START_DATE, "End date must be greater than Start date."),
    THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION(7009, Constants.THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION, "This date is overlapped by other metrology configuration version."),
    END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION(7010, Constants.END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION, "End date can''t be in the past for current metrology configuration version"),
    UNSUPPORTED_COMMAND(7012, Constants.UNSPPORTED_COMMAND, "Unsupported Command {0} for end device {1}");

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return MeteringService.COMPONENTNAME;
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

    public enum Constants {
        ;
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER = "custom.property.set.is.not.editable.by.user";
        public static final String NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT = "no.linked.custom.property.set.on.usage.point";
        public static final String CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN = "custom.property.set.has.different.domain";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED = "custom.property.set.is.not.versioned";
        public static final String NO_CHANNEL_WITH_ID = "no.channel.for.id";
        public static final String NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT = "no.effective.metrology.configuration.on.usage.point";
        public static final String NO_READING_FOUND = "no.reading.found";
        public static final String METER_ROLE_NOT_IN_CONFIGURATION = "meter.role.not.part.of.metrology.configuration";

        public static final String CONTRACT_NOT_ACTIVE = "metrology.contract.not.active.on.usagepoint";
        public static final String VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS = "virtual.usagepoint.only.support.constant.expressions";

        public static final String UNSATISFIED_READING_TYPE_REQUIREMENTS = "unsatisfied.reading.type.requirements";
        public static final String START_DATE_SHOULD_BE_GREATER_THAN_LATEST_START_DATE = "start.date.should.be.greater.than.latest.start.date";
        public static final String START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE = "start.date.should.be.greater.than.latest.end.date";
        public static final String END_DATE_MUST_BE_GREATER_THAN_START_DATE = "end.date.must.be.greater.than.start.date";
        public static final String THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION = "this.date.is.overlapped.by.other.metrology.configuration.version";
        public static final String END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION = "End.date.cant.be.in.the.past.for.current.metrology.configuration.version";
        public static final String UNSPPORTED_COMMAND = "Unsupported.Command.for.enddevice";
    }

}