/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import ch.iec.tc57._2011.schema.message.ErrorType;

import java.text.DecimalFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EMPTY_LIST(1, "NoElementsInList", "The list of ''{0}'' cannot be empty."),
    UNSUPPORTED_BULK_OPERATION(2, "UnsupportedBulkOperation", "Bulk operation isn''t supported for ''{0}'', only first element is processed.", Level.WARNING),
    MISSING_ELEMENT(3, "NoRequiredElement", "Element ''{0}'' is required."),
    ELEMENT_BY_REFERENCE_NOT_FOUND(4, "ElementByRefNotFound", "Element by reference ''{0}'' is not found in ''{1}''."),
    UNSUPPORTED_LIST_SIZE(5, "UnsupportedListSize", "The list of ''{0}'' has unsupported size. Must be of size {1}."),
    EMPTY_ELEMENT(6, "EmptyElement", "Element ''{0}'' is empty or contains only white spaces."),
    MISSING_MRID_OR_NAME_FOR_ELEMENT(7, "MissingMridOrNameForElement", "Either element ''mRID'' or ''Names'' is required under ''{0}'' for identification purpose."),
    MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT(8, "MissingMridOrNameWithTypeForElement",
            "Either element ''mRID'' or ''Names'' with ''NameType.name'' = ''{0}'' is required under ''{1}'' for identification purpose."),
    UNSUPPORTED_ELEMENT(9, "UnsupportedElement", "Element ''{0}'' under ''{1}'' is not supported."),
    UNSUPPORTED_VALUE(10, "UnsupportedValue", "Element ''{0}'' contains unsupported value ''{1}''. Must be one of: {2}."),
    THIS_FIELD_IS_REQUIRED(11, Keys.THIS_FIELD_IS_REQUIRED, "This field is required"),
    FIELD_TOO_LONG(12, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),

    // meter config
    UNABLE_TO_CREATE_DEVICE(1001, "UnableToCreateDevice", "Unable to create device"),
    DEVICE_IDENTIFIER_MISSING(1002, "DeviceIdentifierMissing", "''Name'' or ''serialNumber'' or ''mRID'' must be specified in the payload."),
    NO_SUCH_DEVICE_TYPE(1003, "NoSuchDeviceType", "No such device type: ''{0}''."),
    NO_SUCH_DEVICE_CONFIGURATION(1004, "NoSuchDeviceConfiguration", "No such device configuration: ''{0}''."),

    // usage point config
    UNABLE_TO_CREATE_USAGE_POINT(2001, "UnableToCreateUsagePoint", "Unable to create usage point"),
    NO_SERVICE_CATEGORY_FOUND(2002, "NoServiceCategoryFound", "No active service category is found for ''{0}''."),
    NO_SERVICE_KIND_FOUND(2003, "NoServiceKindFound", "No service kind is found for ''{0}''."),
    NO_METROLOGY_CONFIGURATION_WITH_NAME(2004, "NoMetrologyConfigWithName", "No metrology configuration suitable for usage point is found by name ''{0}''."),
    UNABLE_TO_UPDATE_USAGE_POINT(2005, "UnableToUpdateUsagePoint", "Unable to update usage point"),
    NO_USAGE_POINT_WITH_MRID(2006, "NoUsagePointWithMRID", "No usage point is found by MRID ''{0}''."),
    NO_USAGE_POINT_WITH_NAME(2007, "NoUsagePointWithName", "No usage point is found by name ''{0}''."),
    NO_USAGE_POINT_STATE_WITH_NAME(2008, "NoUsagePointStateWithName", "No usage point state ''{0}'' is found in current life cycle."),
    NO_AVAILABLE_TRANSITION_TO_STATE(2009, "NoAvailableTransitionToState", "No transition is available to state ''{0}''."),
    USAGE_POINT_IS_ALREADY_IN_STATE(2010, "UsagePointIsAlreadyInState", "Usage point is already in state ''{0}''."),
    TRANSITION_CHECK_FAILED(2011, "TransitionCheckFailed", "Transition can''t be performed due to failed transition check ''{0}'': {1}"),
    TRANSITION_ACTION_FAILED(2012, "TransitionActionFailed", "Transition can''t be performed due to failed transition action ''{0}'': {1}"),
    NO_CONNECTION_STATE_FOUND(2013, "NoConnectionStateFound", "No supported connection state is found for ''{0}''."),
    NO_STATE_FOUND_IN_LIFE_CYCLE(2014, "StateNotFound", "The life cycle: ''{0}'' doesn''t contain usage point state: ''{1}''"),
    LIFE_CYCLE_NOT_FOUND(2015, "LifeCycleNotFound", "The life cycle: ''{0}'' doesn''t exist."),
    UNABLE_TO_GET_USAGE_POINT(2016, "UnableToGetUsagePoint", "Unable to get usage point"),
    INVALID_METROLOGY_CONTRACT_REQUIRMENT(2017, "UnableToInactivateMandatoryPurpose", "Mandatory purpose can''t be inactive."),
    UNABLE_TO_CHANGE_STATE_OPTIONAL_CONTRACT(2018, "UnableToChangeStateOptionalContract", "Unable to change state of optional contract."),
    NO_METROLOGYCONFIG_FOR_USAGEPOINT(2019, "NoMetrologyConfigForUsagePoint", "Usage point {0} doesn''t have a link to metrology configuration."),
    UNSATISFIED_READING_TYPE_REQUIREMENTS(2020, "UnsatisfiedReadingTypeRequirments", "Meters don''t provide reading types specified in the metrology contract."),
    MORE_THAN_ONE_METROLOGY_CONFIGURATION_SPECIFIED(2021, "MoreThanOneMetrologyConfigurationSpecified", "Metrology requirement can''t accept more than one metrology configuration."),
    INVALID_RANGE(2022, "InvalidRange", "Start time exceeds end time of custom attribute set {0}."),
    START_DATE_LOWER_CREATED_DATE(2023, "StartDateLowerCreatedDate", "Start date of custom attribute set {0} must be greater than or equal to creation date of usage point {1}."),
    NO_CUSTOMATTRIBUTE_VERSION(2024, "NoCustomAttributeVersion", "Custom attribute set version with start date {0} isn''t found."),
    NO_CUSTOMATTRIBUTE(2025, "NoCustomAttribute", "Can''t find custom attribute set {0}."),
    WRONG_QUANTITY_FORMAT(2026, "WrongQuantitiyFormat", "Wrong unit format for attribute {0}. Supported multipliers: {1}, supported units: {2}."),
    WRONG_ENUM_FORMAT(2027, "WrongEnumFormat", "Wrong enumeration value for attribute {0}. Possible values: {1}."),
    MISSING_REQUIRED_CUSTOMATTRIBUTE_VALUE(2028, "MissingRequiredCustomAttributeValue", "Attribute {0} is required on custom attribute set {1}."),
    CUSTOMPROPERTYSET_VALUES_ON_REQUIRED_RANGE(2029, "CustomPropertySetValuesOnRequiredRange", "Custom property set {0} must have values on the required range {1}."),
    NO_ACTIVE_METROLOGY_CONFIGURATION_WITH_NAME(2030, "NoActiveMetrologyConfigWithName", "Metrology configuration with name ''{0}'' isn''t active."),
    DUPLICATE_USAGE_POINT_NAME(2031,"usagepoint.name.already.exists", "Usage point name must be unique."),

    // async
    COULD_NOT_FIND_SERVICE_CALL_TYPE(5001, "CouldNotFindServiceCallType", "Could''t find service call type {0} with version {1}"),
    NO_END_POINT_WITH_URL(5002, "NoEndPointConfiguredWithURL", "No end point configuration is found by URL ''{0}''."),
    NO_PUBLISHED_END_POINT_WITH_URL(5003, "NoPublishedEndPointConfiguredWithURL", "No published end point configuration is found by URL ''{0}''."),
    ;

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
        return CIMInboundSoapEndpointsActivator.COMPONENT_NAME;
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

    public String translate(Thesaurus thesaurus, Object... args) {
        return thesaurus.getSimpleFormat(this).format(args);
    }

    public String getErrorCode() {
        return getModule() + new DecimalFormat("0000").format(number);
    }

    public ErrorType.Level getErrorTypeLevel() {
        if (Level.SEVERE.equals(getLevel())) {
            return ErrorType.Level.FATAL;
        } else if (Level.WARNING.equals(getLevel())) {
            return ErrorType.Level.WARNING;
        } else {
            return ErrorType.Level.INFORM;
        }
    }

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String THIS_FIELD_IS_REQUIRED = "ThisFieldIsRequired";
    }
}
