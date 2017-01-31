/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    UNDERLYING_IO_EXCEPTION(1000, "io.failed", "Underlying IO Exception"),
    ILLEGAL_MRID_FORMAT(1001, "mrid.illegalformat", "Supplied MRID ''{0}'' is not the correct format.", Level.SEVERE),
    ILLEGAL_CURRENCY_CODE(1002, "currency.illegalcode", "Invalid currency code : ''{0}''", Level.SEVERE),

    METER_EVENT_IGNORED(2001, "meter.event.ignored", "Ignored event {0} on meter {1}, since it is not defined in the system", Level.INFO),
    READINGTYPE_IGNORED(2002, "readingtype.ignored", "Ignored data for reading type {0} on meter {1}, since reading type is not defined int the system", Level.INFO),
    NOMETERACTIVATION(2003, "meter.nometeractivation", "No meter activation found for meter {0} on {1}", Level.INFO),
    READINGTYPE_ADDED(2004, "readingtype.added", "Added reading type {0} for meter {1}", Level.INFO),
    CANNOT_DELETE_METER_METER_ACTIVATIONS_EXIST(2005, "meter.cannot.delete.with.activations", "Cannot delete meter {0} because meter activations are linked to the meter", Level.SEVERE),
    READING_TIMESTAMP_NOT_IN_MEASUREMENT_PERIOD(2006, "reading.timesatmp.not.in.measurement.period", "Measurement time should be in measurement period", Level.SEVERE),
    METER_ALREADY_ACTIVE(2007, "meter.alreadyactive", "Meter {0} is already active at {1}", Level.SEVERE),
    METER_ALREADY_LINKED_TO_USAGEPOINT(2008, "meter.alreadyhasusagepoint", "Meter ''{0}'' is already linked to usage point ''{1}'' as ''{2}'' meter", Level.SEVERE),
    TOO_MANY_READINGTYPES(2009, "readingtype.tooManyReadingTypes", "You are going to add {0} reading types. The limit is 1000.", Level.SEVERE),
    READINGTYPE_ALREADY_EXISTS(2010, "readingtype.alreadyExists", "Reading type {0} already exists.", Level.SEVERE),
    READINGTYPE_CREATING_FAIL(2011, "readingtype.creatingFail", "Failed creating reading types.", Level.SEVERE),
    FIELD_NOT_FOUND(2012, "readingtype.fieldNotFound", "Cannot find field {0}", Level.SEVERE),
    FIELD_TOO_LONG(2013, Constants.FIELD_TOO_LONG, "Field length must not exceed {max} characters", Level.SEVERE),
    INVALID_VALUE(2014, Constants.INVALID_VALUE, "Value must be between {min} and {max}", Level.SEVERE),
    INVALID_MULTIPLIER(2015, Constants.INVALID_MULTIPLIER, "Multiplier must be between {min} and {max}", Level.SEVERE),
    INVALID_UNIT(2016, Constants.INVALID_UNIT, "Invalid unit", Level.SEVERE),

    DUPLICATE_USAGE_POINT_MRID(3001, Constants.DUPLICATE_USAGE_POINT_MRID, "MRID must be unique", Level.SEVERE),
    NO_USAGE_POINT_WITH_NAME(3002, Constants.NO_USAGE_POINT_WITH_NAME, "No usage point with name {0}"),
    NO_CHANNEL_WITH_ID(3003, Constants.NO_CHANNEL_WITH_ID, "No channel with id {0}"),
    NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT(3004, Constants.NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT, "No effective metrology configuration on usage point {0}"),
    NO_READING_FOUND(3005, Constants.NO_READING_FOUND, "No reading found"),
    METER_ROLE_NOT_IN_CONFIGURATION(3006, Constants.METER_ROLE_NOT_IN_CONFIGURATION, "Meter role {0} is not part of the metrology configuration that applies to the meter activation period"),
    DUPLICATE_USAGE_POINT_NAME(3007, Constants.DUPLICATE_USAGE_POINT_NAME, "Usage point name must be unique", Level.SEVERE),

    REQUIRED(4001, Constants.REQUIRED, "This field is required"),
    FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION(4002, Constants.FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION, "You cannot manage custom attribute sets because metrology configuration is active."),
    OBJECT_MUST_HAVE_UNIQUE_NAME(4003, Constants.OBJECT_MUST_HAVE_UNIQUE_NAME, "Name must be unique"),
    CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER(4004, Constants.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, "The custom attribute set ''{0}'' is not editable by current user."),
    NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT(4005, Constants.NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT, "The custom attribute set ''{0}'' is not attached to the usage point."),
    CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN(4006, Constants.CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN, "The custom attribute set ''{0}'' has different domain type."),
    CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED(4007, Constants.CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED, "The custom attribute set ''{0}'' is not versioned."),
    READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS(4008, Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS, "This reading type attribute code is not within limits."),
    CAN_NOT_DELETE_METROLOGY_PURPOSE_IN_USE(4009, Constants.CAN_NOT_DELETE_METROLOGY_PURPOSE_IN_USE, "The ''{0}'' is in use and can not be deleted."),
    READING_TYPE_TEMPLATE_UNITS_SHOULD_HAVE_THE_SAME_DIMENSION(4010, Constants.READING_TYPE_TEMPLATE_UNITS_SHOULD_HAVE_THE_SAME_DIMENSION, "All possible values for reading type template must have the same dimension."),
    CAN_NOT_DELETE_METER_ROLE_FROM_METROLOGY_CONFIGURATION(4011, Constants.CAN_NOT_DELETE_METER_ROLE_FROM_METROLOGY_CONFIGURATION, "Meter role ''{0}'' is in use and can not be deleted from ''{1}''."),
    CAN_NOT_ADD_METER_ROLE_TO_METROLOGY_CONFIGURATION(4012, Constants.CAN_NOT_ADD_METER_ROLE_TO_METROLOGY_CONFIGURATION, "Meter role ''{0}'' is not assigned to the ''{1}'' service category."),
    ROLE_IS_NOT_ALLOWED_ON_CONFIGURATION(4013, Constants.ROLE_IS_NOT_ALLOWED_ON_CONFIGURATION, "Meter role ''{0}'' is not allowed on the metrology configuration ''{1}''."),
    DELIVERABLE_MUST_HAVE_THE_SAME_CONFIGURATION(4014, Constants.DELIVERABLE_MUST_HAVE_THE_SAME_CONFIGURATION, "Reading type deliverable must have the same metrology configuration."),
    REQUIREMENT_MUST_HAVE_UNIQUE_RT(4015, Constants.REQUIREMENT_MUST_HAVE_UNIQUE_RT, "Reading type requirement must have unique reading type."),
    CAN_NOT_DELETE_FORMULA_IN_USE(4016, Constants.CAN_NOT_DELETE_FORMULA_IN_USE, "This formula is in use and can not be deleted."),
    READING_TYPE_FOR_DELIVERABLE_ALREADY_USED(4017, Constants.READING_TYPE_FOR_DELIVERABLE_ALREADY_USED, "The readingtype is already used for another deliverable on this metrology configuration."),
    NO_SUCH_LOCATION(4018, Constants.NO_SUCH_LOCATION, "Location not found"),
    DUPLICATE_LOCATION_ENTRY(4019, Constants.DUPLICATE_LOCATION_ENTRY, "You attempted to enter a duplicate location address. Please check again or perform an editing."),
    CAN_NOT_DELETE_READING_TYPE_DELIVERABLE_IN_USE(4020, Constants.CAN_NOT_DELETE_READING_TYPE_DELIVERABLE_IN_USE, "The ''{0}'' is in use and can not be deleted."),
    SEARCHABLE_PROPERTY_NOT_FOUND(4021, Constants.SEARCHABLE_PROPERTY_NOT_FOUND, "The ''{0}'' searchable property can not be used as usage point requirement."),
    BAD_USAGE_POINT_REQUIREMENT_VALUE(4022, Constants.BAD_USAGE_POINT_REQUIREMENT_VALUE, "Bad usage point requirement value: {0}."),
    FAILED_TO_DEACTIVATE_METROLOGY_CONFIGURATION(4023, Constants.FAILED_TO_DEACTIVATE_METROLOGY_CONFIGURATION, "The metrology configuration is still used by at least one usage point. Use search to find the usage points with such metrology configuration."),

    INVALID_DIMENSION(5000, Constants.INVALID_DIMENSION, "Invalid dimension"),
    INVALID_ARGUMENTS_FOR_MULTIPLICATION(5001, Constants.INVALID_ARGUMENTS_FOR_MULTIPLICATION, "Dimensions from multiplication arguments do not result in a valid dimension."),
    INVALID_ARGUMENTS_FOR_DIVISION(5002, Constants.INVALID_ARGUMENTS_FOR_DIVISION, "Dimensions from division arguments do not result in a valid dimension."),
    INVALID_NUMBER_OF_ARGUMENTS_FOR_SAFE_DIVISION(5003, Constants.INVALID_NUMBER_OF_ARGUMENTS_FOR_SAFE_DIVISION, "Safe division requires 3 arguments."),
    SAFE_DIVISION_REQUIRES_NUMERICAL_CONSTANT(5004, Constants.SAFE_DIVISION_REQUIRES_NUMERICAL_CONSTANT, "Safe division argument must be a numerical constant or \"null\"."),
    SAFE_DIVISION_REQUIRES_NON_ZERO_NUMERICAL_CONSTANT(5005, Constants.SAFE_DIVISION_REQUIRES_NON_ZERO_NUMERICAL_CONSTANT, "Zero is not an acceptable alternative division argument for safe division."),
    INVALID_ARGUMENTS_FOR_SUM_OR_SUBSTRACTION(5006, Constants.INVALID_ARGUMENTS_FOR_SUM_OR_SUBSTRACTION, "Only dimensions that are compatible for automatic unit conversion can be summed or substracted."),
    INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED(5007, Constants.INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED, "At least 1 child is required for a function call."),
    INVALID_ARGUMENTS_FOR_FUNCTION_CALL(5008, Constants.INVALID_ARGUMENTS_FOR_FUNCTION_CALL, "Only dimensions that are compatible for automatic unit conversion can be used as children of a function."),
    AGGREGATION_FUNCTION_REQUIRES_AGGREGATION_LEVEL(5009, Constants.AGGREGATION_FUNCTION_REQUIRES_AGGREGATION_LEVEL, "Aggregation functions require an aggregation level argument."),
    INCONSISTENT_LEVELS_IN_AGGREGATION_FUNCTIONS(5010, Constants.INCONSISTENT_LEVELS_IN_AGGREGATION_FUNCTIONS, "All aggregation functions must use the same aggregation level argument."),
    INVALID_METROLOGYCONFIGURATION_FOR_REQUIREMENT(5011, Constants.INVALID_METROLOGYCONFIGURATION_FOR_REQUIREMENT, "The requirement with id ''{0}'' cannot be used because it has a different metrology configuration."),
    INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE(5012, Constants.INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE, "The deliverable with id ''{0}'' cannot be used because it has a different metrology configuration."),
    READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA(5013, Constants.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA, "The readingtype \"{0}\" is not compatible with the dimension of the formula of deliverable \"{2} = {1}\"."),
    FUNCTION_NOT_ALLOWED_IN_AUTOMODE(5014, Constants.FUNCTION_NOT_ALLOWED_IN_AUTOMODE, "Function ''{0}'' is not allowed in auto mode."),
    IRREGULAR_READING_TYPE_DELIVERABLE_ONLY_SUPPORTS_SIMPLE_FORMULAS(5015, Constants.IRREGULAR_READING_TYPE_DELIVERABLE_ONLY_SUPPORTS_SIMPLE_FORMULAS, "Irregular deliverables only support simple formulas that operate on at most one irregular requirement"),
    REGULAR_READING_TYPE_DELIVERABLE_DOES_NOT_SUPPORT_IRREGULAR_REQUIREMENTS(5016, Constants.REGULAR_READING_TYPE_DELIVERABLE_DOES_NOT_SUPPORT_IRREGULAR_REQUIREMENTS, "Regular deliverable does not support irregular requirements"),
    INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS(5017, Constants.INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS, "The interval of the output reading type should be larger or equal to interval of the requirements in the formula."),
    AUTO_AND_EXPERT_MODE_CANNOT_BE_COMBINED(5018, Constants.AUTO_AND_EXPERT_MODE_CANNOT_BE_COMBINED, "Auto mode and export mode cannot be combined."),
    INVALID_READINGTYPE_UNIT_IN_DELIVERABLE(5019, Constants.INVALID_READINGTYPE_UNIT_IN_DELIVERABLE, "The readingtype for the deliverable is not valid, it should represent a numerical value."),
    INVALID_READINGTYPE_UNIT_IN_REQUIREMENT(5020, Constants.INVALID_READINGTYPE_UNIT_IN_REQUIREMENT, "The readingtype for a requirement is not valid, it should represent a numerical value."),
    INCOMPATIBLE_INTERVAL_LENGTHS(5021, Constants.INCOMPATIBLE_INTERVAL_LENGTHS, "''{0}'' values cannot be aggregated to ''{1}'' values."),
    BULK_READINGTYPE_NOT_ALLOWED(5022, Constants.BULK_READINGTYPE_NOT_ALLOWED, "Bulk reading type is not allowed in deliverables with regular reading type"),
    BULK_DELIVERABLES_CAN_ONLY_USE_BULK_READINGTYPES(5023, Constants.BULK_DELIVERABLES_CAN_ONLY_USE_BULK_READINGTYPES, "Deliverables with bulk reading type can only use reading types with bulk reading types"),
    CUSTOM_PROPERTY_SET_NOT_CONFIGURED_ON_METROLOGY_CONFIGURATION(5024, Constants.CUSTOM_PROPERTY_SET_NOT_CONFIGURED_ON_METROLOGY_CONFIGURATION, "The property ''{0}'' cannot be used because the custom property set ''{1}'' is not configured on this metrology configuration."),
    CUSTOM_PROPERTY_SET_NO_LONGER_ACTIVE(5025, Constants.CUSTOM_PROPERTY_SET_NO_LONGER_ACTIVE, "The custom property set ''{0}'' is no longer active."),
    CUSTOM_PROPERTY_SET_NOT_VERSIONED(5026, Constants.CUSTOM_PROPERTY_SET_NOT_VERSIONED, "The custom property set ''{0}'' is not versioned, only versioned sets are supported."),
    CUSTOM_PROPERTY_MUST_BE_NUMERICAL(5027, Constants.CUSTOM_PROPERTY_MUST_BE_NUMERICAL, "The property ''{0}'' of custom property set ''{1}'' must be numerical."),

    CONTRACT_NOT_ACTIVE(6000, Constants.CONTRACT_NOT_ACTIVE, "The metrology contract with purpose {0} is not active on usage point ''{1}'' during the requested data aggregation period ({2})"),
    VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS(6001, Constants.VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS, "Unmeasured usage points only support constants and custom attributes or operations and functions that operate on those"),

    @Deprecated
    USAGE_POINT_INCORRECT_STATE(7001, Constants.USAGE_POINT_INCORRECT_STATE, "Usage point ''{0}'' should be in ''Under construction'' state to activate meters"),
    THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT(7002, Constants.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT, "The same meter can''t be specified for different meter roles."),
    UNSATISFIED_METROLOGY_REQUIREMENT(7003, Constants.UNSATISFIED_METROLOGY_REQUIREMENT, "This meter does not provide reading types matching a {0}."),
    UNSATISFIED_READING_TYPE_REQUIREMENTS(7004, Constants.UNSATISFIED_READING_TYPE_REQUIREMENTS, "Meters don''t provide reading types specified in the metrology configuration."),
    UNSATISFIED_READING_TYPE_REQUIREMENTS_FOR_DEVICE(7005, Constants.UNSATISFIED_READING_TYPE_REQUIREMENTS_FOR_DEVICE, "Devices activated on this usage point in specified period of time don''t provide reading types specified in selected metrology configuration."),
    START_DATE_SHOULD_BE_GREATER_THAN_LATEST_START_DATE(7006, Constants.START_DATE_SHOULD_BE_GREATER_THAN_LATEST_START_DATE, "Start date should be greater than Start date of the latest metrology configuration version."),
    START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE(7007, Constants.START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE, "Start date should be greater than or equal to End date of the latest metrology configuration version."),
    END_DATE_MUST_BE_GREATER_THAN_START_DATE(7008, Constants.END_DATE_MUST_BE_GREATER_THAN_START_DATE, "End date must be greater than Start date."),
    THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION(7009, Constants.THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION, "This date is overlapped by other metrology configuration version."),
    END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION(7010, Constants.END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION, "End date can''t be in the past for current metrology configuration version"),
    CHANNEL_DATA_PRESENT(7011, "ChannelDataIsPresent", "A meter activation could not be created: channel data is already present beyond the meter activation start time"),
    UNSUPPORTED_COMMAND(7012, Constants.UNSPPORTED_COMMAND, "Unsupported Command {0} for end device {1}"),
    CURRENT_EFFECTIVE_METROLOGY_CONFIG_CANT_BE_REMOVED(7013, Constants.CURRENT_EFFECTIVE_METROLOGY_CONFIG_CANT_BE_REMOVED, "Current metrology configuration version can''t be removed"),
    START_DATE_MUST_BE_GRATER_THAN_UP_CREATED_DATE(7014, Constants.START_DATE_MUST_BE_GRATER_THAN_UP_CREATED_DATE, "Start date must be greater than or equal to Created date of usage point"),
    USAGE_POINT_DETAILS_NOT_UNIQUE(7015, Constants.UNIQUE_DETAILS, "The usage point already has details for this interval"),
    ACTIVATION_FAILED_BY_CUSTOM_VALIDATORS(7016, Constants.ACTIVATION_FAILED_BY_CUSTOM_VALIDATORS, "Usage point activation failed by custom validator: {0}"),
    USAGE_POINT_ALREADY_ACTIVE_WITH_GIVEN_ROLE(7017, Constants.USAGE_POINT_ALREADY_ACTIVE_WITH_GIVEN_ROLE, "Usage point already has linked meter {0} for role {1}"),
    USAGE_POINT_INCORRECT_STAGE(7018, Constants.USAGE_POINT_INCORRECT_STAGE, "Incorrect usage point stage, should be preoperational"),
    UNSATISFIED_READING_TYPE_REQUIREMENT_FOR_METER(7019, Constants.UNSATISFIED_READING_TYPE_REQUIREMENT_FOR_METER, "Meter {0} does not provide reading types required by purpose {1}"),

    DENOMINATOR_CANNOT_BE_ZERO(8001, Constants.DENOMINATOR_CANNOT_BE_ZERO, "Denominator cannot be 0"),
    REQUIRED_CAS_MISSING(8002, Constants.REQUIRED_CAS_MISSING, "Required custom property sets are missing"),

    CAN_NOT_DELETE_ACTIVE_LIFE_CYCLE(9001, Constants.CAN_NOT_DELETE_ACTIVE_LIFE_CYCLE, "Can''t remove usage point life cycle because it''s in use by at least one of the usage point."),
    CAN_NOT_DELETE_ACTIVE_STATE(9002, Constants.CAN_NOT_DELETE_ACTIVE_STATE, "This state can''t be removed from this usage point life cycle because one or more usage points use this state.");

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

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public enum Constants {
        ;
        public static final String DUPLICATE_USAGE_POINT_NAME = "usagepoint.name.already.exists";
        public static final String DUPLICATE_USAGE_POINT_MRID = "usagepoint.mrid.already.exists";
        public static final String FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION = "fail.manage.cps.on.active.metrology.configuration";
        public static final String OBJECT_MUST_HAVE_UNIQUE_NAME = "name.must.be.unique";
        public static final String REQUIRED = "isRequired";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER = "custom.property.set.is.not.editable.by.user";
        public static final String NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT = "no.linked.custom.property.set.on.usage.point";
        public static final String CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN = "custom.property.set.has.different.domain";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED = "custom.property.set.is.not.versioned";
        public static final String INVALID_VALUE = "invalidValue";
        public static final String INVALID_MULTIPLIER = "invalidMultiplier";
        public static final String INVALID_UNIT = "invalidUnit";
        public static final String NO_USAGE_POINT_WITH_NAME = "NoUsagePointWithName";
        public static final String NO_CHANNEL_WITH_ID = "no.channel.for.id";
        public static final String NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT = "no.effective.metrology.configuration.on.usage.point";
        public static final String NO_READING_FOUND = "no.reading.found";
        public static final String METER_ROLE_NOT_IN_CONFIGURATION = "meter.role.not.part.of.metrology.configuration";
        public static final String NO_SUCH_LOCATION = "no.such.location";
        public static final String DUPLICATE_LOCATION_ENTRY = "duplicate.address.entry";
        public static final String FAILED_TO_DEACTIVATE_METROLOGY_CONFIGURATION = "failed.to.deactivate.metrology.configuration";

        public static final String INVALID_ARGUMENTS_FOR_MULTIPLICATION = "expression.node.invalid.arguments.multiplication";
        public static final String INVALID_ARGUMENTS_FOR_DIVISION = "expression.node.invalid.arguments.division";
        public static final String INVALID_NUMBER_OF_ARGUMENTS_FOR_SAFE_DIVISION = "expression.node.invalid.argumentcount.safe.division";
        public static final String SAFE_DIVISION_REQUIRES_NUMERICAL_CONSTANT = "expression.node.invalid.arguments.safe.division";
        public static final String SAFE_DIVISION_REQUIRES_NON_ZERO_NUMERICAL_CONSTANT = "expression.node.invalid.arguments.safe.division.notzero";
        public static final String INVALID_ARGUMENTS_FOR_SUM_OR_SUBSTRACTION = "expression.node.invalid.arguments.sum.or.substraction";
        public static final String INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED = "expression.node.invalid.arguments.one.child.required";
        public static final String INVALID_ARGUMENTS_FOR_FUNCTION_CALL = "expression.node.invalid.arguments.functioncall";
        public static final String AGGREGATION_FUNCTION_REQUIRES_AGGREGATION_LEVEL = "expression.node.invalid.arguments.aggregation.functioncall";
        public static final String INCONSISTENT_LEVELS_IN_AGGREGATION_FUNCTIONS = "expression.node.inconsistent.aggregation.levels";
        public static final String INVALID_DIMENSION = "expression.node.invalid.dimension";
        public static final String CONTRACT_NOT_ACTIVE = "metrology.contract.not.active.on.usagepoint";
        public static final String VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS = "virtual.usagepoint.only.support.constant.expressions";

        public static final String READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS = "reading.type.attribute.code.is.not.within.limits";
        public static final String READING_TYPE_TEMPLATE_UNITS_SHOULD_HAVE_THE_SAME_DIMENSION = "reading.type.template.all.units.have.the.same.dimension";
        public static final String CAN_NOT_DELETE_METROLOGY_PURPOSE_IN_USE = "can.not.delete.metrology.purpose.in.use";
        public static final String CAN_NOT_DELETE_METER_ROLE_FROM_METROLOGY_CONFIGURATION = "can.not.delete.meter.role.from.metrology.configuration";
        public static final String CAN_NOT_ADD_METER_ROLE_TO_METROLOGY_CONFIGURATION = "can.not.add.meter.role.in.to.metrology.configuration";
        public static final String ROLE_IS_NOT_ALLOWED_ON_CONFIGURATION = "usagepoint.metrologyconfiguration.role.not.allowed";
        public static final String INVALID_METROLOGYCONFIGURATION_FOR_REQUIREMENT = "invalid.metrologyconfiguration.for.requirement";
        public static final String INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE = "invalid.metrologyconfiguration.for.deliverable";
        public static final String READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA = "readingtype.of.deliverable.incompatible.with.formula";
        public static final String DELIVERABLE_MUST_HAVE_THE_SAME_CONFIGURATION = "deliverable.must.have.the.same.configuration";
        public static final String REQUIREMENT_MUST_HAVE_UNIQUE_RT = "requirement.must.have.unique.rt";
        public static final String FUNCTION_NOT_ALLOWED_IN_AUTOMODE = "no.functions.allowed.in.automode";
        public static final String CAN_NOT_DELETE_FORMULA_IN_USE = "can.not.delete.formula.in.use";
        public static final String IRREGULAR_READING_TYPE_DELIVERABLE_ONLY_SUPPORTS_SIMPLE_FORMULAS = "irregular.readingtype.deliverable";
        public static final String REGULAR_READING_TYPE_DELIVERABLE_DOES_NOT_SUPPORT_IRREGULAR_REQUIREMENTS = "regular.readingtype.deliverable";
        public static final String INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS = "interval.of.readingtype.should.be.greater.or.equal.to.interval.of.requirements";
        public static final String AUTO_AND_EXPERT_MODE_CANNOT_BE_COMBINED = "auto.and.expert.mode.cannot.be.combined";
        public static final String INVALID_READINGTYPE_UNIT_IN_DELIVERABLE = "invalid.readingtype.in.deliverable";
        public static final String INVALID_READINGTYPE_UNIT_IN_REQUIREMENT = "invalid.readingtype.in.requirement";
        public static final String INCOMPATIBLE_INTERVAL_LENGTHS = "incompatible.intervallengths";
        public static final String READING_TYPE_FOR_DELIVERABLE_ALREADY_USED = "reading.type.already.used.for.deliverable.on.same.metrologyconfig";
        public static final String CAN_NOT_DELETE_READING_TYPE_DELIVERABLE_IN_USE = "can.not.delete.reading.type.deliverable.in.use";
        public static final String SEARCHABLE_PROPERTY_NOT_FOUND = "searchable.property.not.found";
        public static final String BAD_USAGE_POINT_REQUIREMENT_VALUE = "bad.usage.point.requirement.value";
        public static final String CUSTOM_PROPERTY_SET_NOT_CONFIGURED_ON_METROLOGY_CONFIGURATION = "cps.not.configured.on.metrologyconfiguration";
        public static final String CUSTOM_PROPERTY_SET_NO_LONGER_ACTIVE = "cps.no.longer.active";
        public static final String CUSTOM_PROPERTY_SET_NOT_VERSIONED = "cps.not.versioned";
        public static final String CUSTOM_PROPERTY_MUST_BE_NUMERICAL = "cps.property.not.numerical";
        public static final String USAGE_POINT_INCORRECT_STATE = "usage.point.incorrect.state";
        public static final String USAGE_POINT_INCORRECT_STAGE = "usage.point.incorrect.stage";
        public static final String UNSATISFIED_READING_TYPE_REQUIREMENT_FOR_METER = "unsatisfied.reading.type.requirement.for.meter";
        public static final String THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT = "the.same.meter.activated.twice.on.usage.point";
        public static final String USAGE_POINT_ALREADY_ACTIVE_WITH_GIVEN_ROLE = "usage.point.already.active.with.given.role";
        public static final String UNSATISFIED_METROLOGY_REQUIREMENT = "unsatisfied.metrology.requirement";
        public static final String UNSATISFIED_READING_TYPE_REQUIREMENTS = "unsatisfied.reading.type.requirements";
        public static final String UNSATISFIED_READING_TYPE_REQUIREMENTS_FOR_DEVICE = "unsatisfied.reading.type.requirements.for.device";
        public static final String START_DATE_SHOULD_BE_GREATER_THAN_LATEST_START_DATE = "start.date.should.be.greater.than.latest.start.date";
        public static final String START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE = "start.date.should.be.greater.than.latest.end.date";
        public static final String END_DATE_MUST_BE_GREATER_THAN_START_DATE = "end.date.must.be.greater.than.start.date";
        public static final String THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION = "this.date.is.overlapped.by.other.metrology.configuration.version";
        public static final String END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION = "End.date.cant.be.in.the.past.for.current.metrology.configuration.version";
        public static final String UNSPPORTED_COMMAND = "Unsupported.Command.for.enddevice";
        public static final String CURRENT_EFFECTIVE_METROLOGY_CONFIG_CANT_BE_REMOVED = "Remove.current.effectve.mc";
        public static final String BULK_READINGTYPE_NOT_ALLOWED = "bulk.readingtype.not.allowed";
        public static final String START_DATE_MUST_BE_GRATER_THAN_UP_CREATED_DATE = "version.start.should.be.greater.than.up.creation.date";
        public static final String BULK_DELIVERABLES_CAN_ONLY_USE_BULK_READINGTYPES = "bulk.deliverable.can.only.use.other.bulk.reading";
        public static final String UNIQUE_DETAILS = "usage.point.details.not.unique.for.interval";
        public static final String DENOMINATOR_CANNOT_BE_ZERO = "denominator.cannot.be.zero";
        public static final String ACTIVATION_FAILED_BY_CUSTOM_VALIDATORS = "activation.failed.by.custom.validators";
        public static final String CAN_NOT_DELETE_ACTIVE_LIFE_CYCLE = "can.not.delete.active.life.cycle";
        public static final String CAN_NOT_DELETE_ACTIVE_STATE = "can.not.delete.active.state";
        public static final String REQUIRED_CAS_MISSING = "required.cas.missing";
    }

}
