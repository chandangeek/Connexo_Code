/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import ch.iec.tc57._2011.schema.message.ErrorType;

import java.text.DecimalFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EMPTY_LIST(1, "NoElementsInList", "The list of ''{0}'' cannot be empty"),
    UNSUPPORTED_BULK_OPERATION(2, "UnsupportedBulkOperation", "Bulk operation is not supported on ''{0}'', only first element is processed", Level.WARNING),
    MISSING_ELEMENT(3, "NoRequiredElement", "Element ''{0}'' is required"),
    ELEMENT_BY_REFERENCE_NOT_FOUND(4, "ElementByRefNotFound", "Element by reference ''{0}'' is not found in ''{1}''"),
    UNSUPPORTED_LIST_SIZE(5, "UnsupportedListSize", "The list of ''{0}'' has unsupported size. Must be of size {1}"),
    EMPTY_ELEMENT(6, "EmptyElement", "Element ''{0}'' is empty or contains only white spaces"),
    MISSING_MRID_OR_NAME_FOR_ELEMENT(7, "MissingMridOrNameForElement", "Either element ''mRID'' or ''Names'' is required under ''{0}'' for identification purpose"),
    MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT(8, "MissingMridOrNameWithTypeForElement",
            "Either element ''mRID'' or ''Names'' with ''NameType.name'' = ''{0}'' is required under ''{1}'' for identification purpose"),
    UNSUPPORTED_ELEMENT(9, "UnsupportedElement", "Element ''{0}'' under ''{1}'' is not supported"),
    UNSUPPORTED_VALUE(10, "UnsupportedValue", "Element ''{0}'' contains unsupported value ''{1}''. Must be one of: {2}"),
    THIS_FIELD_IS_REQUIRED(11, Keys.THIS_FIELD_IS_REQUIRED, "This field is required"),
    FIELD_TOO_LONG(12, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),

    // meter config
    UNABLE_TO_CREATE_DEVICE(1001, "UnableToCreateDevice", "Unable to create device"),
    UNABLE_TO_CHANGE_DEVICE(1002, "UnableToChangeDevice", "Unable to change device"),
    DEVICE_IDENTIFIER_MISSING(1003, "CreateDeviceIdentifierMissing", "''Name'' or ''serialNumber'' or ''mRID'' must be specified in the payload"),
    NO_SUCH_DEVICE_TYPE(1005, "NoSuchDeviceType", "No such device type: ''{0}''"),
    NO_SUCH_DEVICE_CONFIGURATION(1006, "NoSuchDeviceConfiguration", "No such device configuration: ''{0}''"),
    NO_DEVICE_WITH_NAME(1007, "NoDeviceWithName", "No device found with name ''{0}''"),
    NO_DEVICE_WITH_MRID(1008, "NoDeviceWithMRID", "No device found with mrid ''{0}''"),
    NOT_VALID_MULTIPLIER_REASON(1009, "NotValidMultiplierReason", "''{0}'' is not a valid multiplier reason"),
    NOT_VALID_STATUS_REASON(1010, "NotValidStatusReason", "''{0}'' is not a valid status reason"),
    UNABLE_TO_CHANGE_DEVICE_STATE(1011, "UnableToChangeDeviceState", "Cannot update the device with ''{0}'' payload state"),
    NOT_VALID_CONFIGURATION_REASON(1012, "NotValidConfigurationReason", "''{0}'' is not a valid configuration event reason"),
    DUPLICATED_ATTRIBUTE_NAME_IN_CUSTOM_ATTRIBUTE_SET(1013, "DuplicatedAttributeNameInCustomAttributeSet", "Custom attribute set ''{1}'' contains several attributes with name ''{0}''"),
    CANT_FIND_CUSTOM_ATTRIBUTE_SET(1014, "CantFindCustomAttributeSet", "Can''t find custom attribute set ''{0}''"),
    CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE(1015, "CantConvertValueOfCustomAttribute", "Can''t convert value ''{0}'' of attribute ''{1}'' for custom attribute set ''{2}''"),
    CANT_FIND_CUSTOM_ATTRIBUTE(1016, "CantFindCustomAttribute", "Can''t find attribute ''{0}'' for custom attribute set ''{1}''"),
    CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET(1017, "CantAssignValuesForCustomAttributeSet", "Can''t assign values for custom attribute set ''{0}''"),
    ASSIGNED_VALUES_FOR_CUSTOM_ATTRIBUTE_SET(1018, "AssignedValuesForCustomAttributeSet", "Assigned values for custom attribute set ''{0}''", Level.INFO),
    CANNOT_IMPORT_KEY_TO_HSM(1019, "CannotImportKeyToHsm", "Cannot import key to HSM, exception occurred during import for device ''{0}'' and security accessor ''{1}''"),
    BOTH_PUBLIC_AND_SYMMETRIC_KEYS_SHOULD_BE_SPECIFIED(1020, "BothPublicAndSymmetricKeysShouldBeSpecified", "Both, public key and symmetric key should be specified or none"),
    NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE(1021, "NoSuchKeyAccessorTypeOnDeviceType", "Can''t process device ''{0}'': Security accessor ''{1}'' is not available on the device type"),
    ACTUAL_VALUE_ALREADY_EXISTS(1022, "ActualValueAlreadyExists", "Can''t process device ''{0}'': security accessor ''{1}'' already as an 'active' value"),
    IMPORTING_SECURITY_KEY_FOR_DEVICE(1023, "ImportingSecurityKeysForDevice", "Importing security key for device ''{0}'' and security accessor ''{1}''"),
    EXCEPTION_OCCURRED_DURING_KEY_IMPORT(1024, "ExceptionOccurredDuringKeyImport", "Exception occurred during key import for device ''{0}'' and security accessor ''{1}''"),
    NO_DEFAULT_DEVICE_CONFIGURATION(1025, "NoDefaultDeviceConfiguration", "No default device configuration"),
    NO_DEVICE_WITH_SERIAL_NUMBER(1026, "NoDeviceWithSerialNumber", "No device found with serial number ''{0}''"),
	SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS(1027, "SecurityKeyUpdateForbidden", "Security key update is forbidden for device ''{0}'' because it is in state ''{1}''"),
    NO_CUSTOM_ATTRIBUTE_VERSION(1028, "NoCustomAttributeVersion", "Custom attribute set version with start date {0} is not found"),
    START_DATE_LOWER_CREATED_DATE(1029, "StartDateLowerCreatedDate", "Start date must be greater or equal to created date of device {0}"),
    GET_DEVICE_IDENTIFIER_MISSING(1030, "GetDeviceIdentifierMissing", "At least one of ''mRID'' or ''Name'' must be specified in the request."),
    UNABLE_TO_GET_METER_CONFIG_EVENTS(1031, "UnableToGetMeterConfigEvents", "Unable to get meter config events"),
    NO_DEVICE(1032, "NoDevice", "Device(s) not found"),
    SHIPMENT_DATE_NOT_IN_STOCK(1033, "shipmentdateNotInStock", "Unable to edit shipment date when device isn''t in state ''In stock''."),

    // get end device events
    UNABLE_TO_GET_END_DEVICE_EVENTS(2001, "UnableToGetEndDeviceEvents", "Unable to get end device events"),
    END_DEVICE_IDENTIFIER_MISSING(2002, "EndDeviceIdentifierMissing", "At least one of ''mRID'' or ''Name'' must be specified in the request."),
    INVALID_OR_EMPTY_TIME_PERIOD(2003, "InvalidOrEmptyTimePeriod",
            "Can''t construct a valid time period: provided start ''{0}'' is after or coincides with the end ''{1}''."),

    // created/closed end device events
    INVALID_CREATED_END_DEVICE_EVENTS(3001, "InvalidCreatedEndDeviceEvents", "Invalid CreatedEndDeviceEvents is received"),
    INVALID_CLOSED_END_DEVICE_EVENTS(3002, "InvalidClosedEndDeviceEvents", "Invalid ClosedEndDeviceEvents is received"),
    INVALID_SEVERITY(3003, "InvalidSeverity", "EndDeviceEvent.Severity should be equal to 'alarm'"),
    NO_LOGBOOK_WITH_OBIS_CODE_AND_DEVICE(3004, "NoLogBookWithObisCodeAndDevice", "No LogBook is found by obis code ''{0}'' and meter's ID ''{1}''."),
    NO_END_DEVICE_EVENT_TYPE_WITH_REF(3005, "NoEndDeviceEventTypeWithRef", "No end device event type is found with ref ''{0}''"),
    NO_OBIS_CODE_CONFIGURED(3006, "NoObisCodeConfigured", "Obis code is not configured for the web service end point"),
    NO_END_POINT_WITH_WEBSERVICE_NAME(3007, "NoEndPointConfigured", "No end point configuration is found by web service name ''{0}''."),

    // async
    COULD_NOT_FIND_SERVICE_CALL_TYPE(4001, "CouldNotFindServiceCallType", "Could not find service call type {0} having version {1}"),
    NO_REPLY_ADDRESS(4002, "NoReplyAddress", "Reply address is required"),
    NO_END_POINT_WITH_URL(4003, "NoEndPointConfiguredWithURL", "No end point configuration is found by URL ''{0}''."),
    SYNC_MODE_NOT_SUPPORTED(4004, "SyncModeNotSupported", "Synchronous mode is not supported for multiple objects"),
    NO_PUBLISHED_END_POINT_WITH_URL(4005, "NoPublishedEndPointConfiguredWithURL", "No published end point configuration is found by URL ''{0}''."),

    NAME_MUST_BE_UNIQUE(5001, "NameMustBeUnique", "Name and serial number must be unique."),
    ELEMENT_BY_REFERENCE_NOT_FOUND_OR_EMPTY(5002, "ElementByRefNotFoundOrEmpty", "Element by reference ''{0}'' not found or has an empty value"),
    IS_NOT_ALLOWED_TO_HAVE_DUPLICATED_ZONE_TYPES(5003, "DuplicatedZoneType", "Is not allowed to send the same zone type more than once, a device can be assigned to only one zone from a zone type"),

    // meter readings
    UNABLE_TO_GET_READINGS(6001, "UnableToGetReadings", "Unable to get readings."),
    NO_PURPOSES_WITH_NAMES(6002, "NoPurposesWithNames", "No metrology purposes are found for names: {0}."),
    END_DEVICES_WITH_MRID_NOT_FOUND(6004, "DevicesWithMridNotFound", "Couldn''t find device(s) with MRID(s) ''{0}''.", Level.WARNING),
    END_DEVICES_WITH_NAME_NOT_FOUND(6005, "DevicesWithNamesNotFound", "Couldn''t find device(s) with name(s) ''{0}''.", Level.WARNING),
    END_DEVICES_NOT_FOUND(6006, "DevicesNotFound", "Couldn''t find device(s) with MRID(s) ''{0}'' and name(s) ''{1}''.", Level.WARNING),
    NO_END_DEVICES(6007, "NoDevices", "No devices have been found."),
    NO_READING_TYPES(6008, "NoReadingTypes", "No reading types have been found."),
    READING_TYPES_WITH_MRID_NOT_FOUND(6009, "ReadingTypesWithMridNotFound", "Reading type(s) with MRID(s) ''{0}'' is(are) not found in the system.", Level.WARNING),
    READING_TYPES_WITH_NAME_NOT_FOUND(6010, "ReadingTypesWithNameNotFound", "Reading type(s) with name(s) ''{0}'' is(are) not found in the system.", Level.WARNING),
    READING_TYPES_NOT_FOUND_IN_THE_SYSTEM(6011, "ReadingTypesNotFoundInTheSystem", "Reading type(s) with MRID(s) ''{0}'' and name(s) ''{1}'' is(are) not found in the system.", Level.WARNING),
    READING_TYPES_NOT_FOUND_ON_DEVICE(6012, "ReadingTypesNotFoundOnDevice", "Reading type(s) is(are) not found on device ''{0}'': ''{1}''.", Level.WARNING),
    NO_USAGE_POINT_WITH_MRID(6013, "NoUsagePointWithMRID", "No usage point is found by MRID ''{0}''."),
    NO_USAGE_POINT_WITH_NAME(6014, "NoUsagePointWithName", "No usage point is found by name ''{0}''."),
    WRONG_TIME_PERIOD_COMBINATION(6015, "WrongTimePeriodCombination", "Can''t construct a valid time period with provided start ''{0}'' and the end ''{1}''.", Level.WARNING),
    DIFFERENT_DATA_SOURCES(6016, "DifferentDataSources", "The request contains data sources of different types under element ''{0}''."),
    SCHEDULE_STRATEGY_NOT_SUPPORTED(6017, "ScheduleStrategyNotSupported", "Schedule strategy ''{0}'' is not supported. The possible values are: ''Run now'' and ''Use schedule''"),
    NO_CONNECTION_TASK(6018, "NoConnectionTask", "No connection task has been found for communication task execution of communication task ''{0}''."),
    CONNECTION_METHOD_NOT_FOUND_ON_DEVICE(6019, "ConnectionMethodNotFoundOnDevice", "The required connection method ''{0}'' wasn''t found on the device ''{1}''.", Level.WARNING),
    CONNECTION_METHOD_NOT_FOUND_FOR_COM_TASK(6020, "ConnectionMethodNotFoundForComTask", "The required connection method ''{0}'' wasn''t found for communication task ''{1}'' of device ''{2}''.", Level.WARNING),
    DATA_SOURCE_NAME_TYPE_NOT_FOUND(6021, "DataSourceNameTypeNotFound", "Data source name type ''{0}'' is not found in the element ''{1}''. Possible values: Load Profile or Register Group."),
    NO_DATA_SOURCES(6022, "NoDataSources", "At least one correct ''GetMeterReadings.ReadingType'' or ''GetMeterReadings.Reading.dataSource'' must be specified in the request under element ''{0}''", Level.WARNING),
    REGISTER_GROUP_NOT_FOUND(6023, "RegisterGroupNotFound", "Couldn''t find register group with name ''{0}'' under element ''{1}''.", Level.WARNING),
    LOAD_PROFILE_NOT_FOUND(6024, "LoadProfileNotFound", "Couldn''t find load profile with name ''{0}'' under element ''{1}''.", Level.WARNING),
    NO_COM_TASK_EXECUTION(6025, "NoComTaskExecution", "The required communication task execution of type {0} wasn't found on the device {1}.", Level.WARNING),
    REGISTER_GROUP_WRONG_TIME_PERIOD(6026, "RegisterGroupWrongTimePeriod", "Wrong time period for register group data source is provided under element ''{0}'': start ''{1}'', end ''{2}''.", Level.WARNING),
    REGISTER_GROUP_EMPTY_TIME_PERIOD(6027, "RegisterGroupEmptyTimePeriod", "Empty time period for register group data source is provided under element ''{0}''.", Level.WARNING),
    REGISTER_EMPTY_TIME_PERIOD(6028, "RegisterEmptyTimePeriod", "Empty time period for register is provided under element ''{0}''."),
    SYSTEM_SOURCE_EMPTY_TIME_PERIOD(6029, "SystemSourceEmptyTimePeriod", "Empty time period for ''System'' source is provided.", Level.WARNING),
    SYSTEM_SOURCE_DOESNT_SUPPORT_LOAD_PROFILES(6030, "SystemSourceDoesntSupportLoadProfiles", "''System'' source doesn''t support ''Load profile'' data source. Element ''{0}''.", Level.WARNING),
    NO_COM_TASK_EXECUTION_FOR_READING_TYPES(6031, "NoComTaskExecutionForReadingTypes", "No communication task execution has been found on device ''{0}'' for readingTypes ''{1}''.", Level.WARNING),
    NO_COM_TASK_EXECUTION_FOR_LOAD_PROFILES(6032, "NoComTaskExecutionForLoadProfiles", "No communication task execution has been found for load profile device messages on device ''{0}''.", Level.WARNING),
    NO_COM_TASK_EXECUTION_ON_DEVICE(6033, "NoComTaskExecutionOnDevice", "No proper communication task execution has been found for device ''{0}'' under element ''{1}''.", Level.WARNING),
    NO_COM_TASK_EXECUTION_FOUND(6034, "NoComTaskExecutionFound", "No proper communication task execution has been found."),
    LOAD_PROFILES_NOT_FOUND_ON_DEVICE(6035, "LoadProfilesNotFoundOnDevice", "LoadProfile(s) is(are) not found on device ''{0}'': ''{1}''.", Level.WARNING),
    NO_READINGS_TO_READ(6036, "NoReadingsToRead", "Couldn''t find any correct  ''GetMeterReadings.Reading'' to read."),
    READING_NOT_APPLICABLE(6037, "ReadingNotApplicable", "''{0}'' will be skipped due to issue in the element ''{1}''.", Level.WARNING),
    READING_NOT_APPLICABLE_DUE_TO_COM_TASKS(6038, "ReadingNotApplicableDueToComTasks", "''{0}'' will be skipped due to inability to find proper communication tasks.", Level.WARNING),
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
        return InboundSoapEndpointsActivator.COMPONENT_NAME;
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