package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.DeviceDataServices;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the device data module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-06 (14:41)
 */
public enum MessageSeeds implements MessageSeed {
    CODING_RELATION_IS_ALREADY_OBSOLETE(1001, "relation.isAlreadyObsolete", "Cannot remove a property because the relation (of type ''{0}'') that holds it is already obsolete"),
    CODING_NO_PROPERTIES_EXPECTED(1002, "noAttributesExpected", "Was not expecting a value to be added for property ''{0}'' because the pluggable does not have any properties"),
    UNEXPECTED_RELATION_TRANSACTION_ERROR(1003, "unExpectedRelationTransactionError", "Unexpected problem occurred in the relation transaction framework"),
    COMPORT_TYPE_NOT_SUPPORTED(1004, Keys.COMPORT_TYPE_NOT_SUPPORTED, "The communication port type of the communication port pool must be supported by the connection type"),
    CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED(1006, Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED, "A connection task requires a connection type pluggable class"),
    CONNECTION_TASK_COMPORT_POOL_REQUIRED(1007, Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED, "A connection task requires a communication port pool"),
    FIELD_TOO_LONG(1011, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    VETO_COMPORTPOOL_DELETION(1012, "comPortPoolXstillInUseByY", "ComPortPool {0} is still in use by at least one device"),
    VETO_DEVICEGROUP_DELETION(1013, "deviceGroupXstillInUseCollection", "Device group {0} is still in use by a data collection KPI"),
    FIELD_IS_REQUIRED(1014, Keys.FIELD_REQUIRED, "This field is required"),
    CANNOT_CONFIGURE_DEVICE_MULTIPLIER_IN_PAST_WHEN_DATA_EXISTS(1015, Keys.CANNOT_CONFIGURE_DEVICE_MULTIPLIER_IN_PAST_WHEN_DATA_EXISTS, "You can not configure a multiplier in the past when your device already has data"),
    MULTIPLIER_MUST_HAVE_METERACTIVATION(1016, Keys.MULTIPLIER_MUST_HAVE_METERACTIVATION, "You can not configure a multiplier with a start date which doesn't correspond with a meter activation"),
    CONNECTION_TASK_DEVICE_REQUIRED(2000, Keys.CONNECTION_TASK_DEVICE_REQUIRED, "A connection type should be linked to a device"),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED(2001, Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED, "A connection type should be linked to a partial connection task from the device configuration"),
    DUPLICATE_CONNECTION_TASK(2002, Keys.DUPLICATE_CONNECTION_TASK, "The partial connection task {0} is already used by connection task {1} on device {2} and therefore no other connection task with the same partial connection task can be added"),
    CONNECTION_TASK_INCOMPATIBLE_PARTIAL(2003, Keys.CONNECTION_TASK_INCOMPATIBLE_PARTIAL, "The type of the partial connection task of a connection task must be compatible. Expected ''{0}'' but got ''{1}''"),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION(2004, Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION, "A connection task must be added against a partial connection task (id={0}, configuration id={1}) from the same device configuration (id={2})"),
    CONNECTION_TASK_IS_ALREADY_OBSOLETE(2005, Keys.CONNECTION_TASK_IS_ALREADY_OBSOLETE, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}"),
    CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE(2006, Keys.CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is currently being executed by communication server ''{2}''"),
    DEFAULT_CONNECTION_TASK_IS_INUSE_AND_CANNOT_DELETE(2008, Keys.DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE, "The default connection task ''{0}'' on device {1} cannot be removed because it is still in use by communication tasks"),
    CONNECTION_TASK_INVALID_PROPERTY(2009, Keys.CONNECTION_TASK_INVALID_PROPERTY, "Invalid value"),
    CONNECTION_TASK_PROPERTY_NOT_IN_SPEC(2010, Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC, "ConnectionType ''{0}'' does not contain a specification for attribute ''{1}''"),
    CONNECTION_TASK_PROPERTY_INVALID_VALUE(2011, Keys.CONNECTION_TASK_PROPERTY_INVALID_VALUE, "''{0}'' is not a valid value for attribute ''{1}'' of ConnectionType ''{2}''"),
    CONNECTION_TASK_REQUIRED_PROPERTY_MISSING(2012, Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING, "This value is required"),
    CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE(2013, Keys.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE, "An inbound communication port pool can only be used once on the same device"),
    OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED(2014, Keys.OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED, "An outbound connection task requires a connection strategy"),
    OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS(2015, Keys.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS, "Outbound connection task with strategy to minimize connections is not compatible with simultaneous connections"),
    OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED(2016, Keys.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED, "An outbound connection task with strategy to minimize connections requires execution scheduling specifications"),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY(2017, Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY, "The offset of the next execution scheduling specifications should not extend its frequency"),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW(2018, Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW, "The offset of the next execution scheduling specifications is not within the communication window"),
    OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW(2019, Keys.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW, "The offset of the next execution scheduling specifications within a week or month, once calculated back to a daily offset is not within the communication window"),
    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(2020, Keys.PHYSICAL_GATEWAY_STILL_IN_USE, "You can not remove device ''{0}'' because it is still used as a physical gateway for ''{1}''"),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(2021, Keys.COMMUNICATION_GATEWAY_STILL_IN_USE, "You can not remove device ''{0}'' because it is still used as a communication gateway for ''{1}''"),
    DEVICE_PROPERTY_HAS_NO_SPEC(2022, Keys.PROPERTY_SPEC_DOESNT_EXIST, "The property specification for property value ''{0}'' does not exist."),
    DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL(2023, Keys.PROPERTY_NOT_ON_DEVICE_PROTOCOL, "The property ''{0}'' is not defined by the device protocol ''{1}'' of device ''{2}''"),
    PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED(2024, Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED, "The protocol dialect configuration properties are required to add device protocol dialect properties"),
    DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC(2025, Keys.DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC, "The protocol dialect ''{0}'' does not contain a specification for attribute ''{1}''"),
    DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE(2026, Keys.DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE, "''{0}'' is not a valid value for attribute ''{1}'' of device dialect protocol ''{2}''"),
    DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING(2027, Keys.DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING, "This is a required property"),
    DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED(2028, Keys.DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED, "Device protocol dialect properties need to be added against a device"),
    CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2029, Keys.CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not remove connection task {0} because it is not owned by device {1}"),
    COM_TASK_IS_OBSOLETE_AND_CAN_NOT_BE_UPDATED(2030, Keys.COM_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not update comtaskexecution {0} for device {1} because it is obsolete"),
    COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE(2031, Keys.COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it has already been made obsolete on {2}"),
    COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE(2032, Keys.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it is currently executing on comserver {2}"),
    COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2033, Keys.COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not remove comtaskexecution {0} because it is not owned by device {1}"),
    VETO_COM_TASK_ENABLEMENT_DELETION(2034, Keys.VETO_COM_TASK_ENABLEMENT_DELETION, "The communication task ''{0}'' is still used by devices having the configuration ''{1}''"),
    VETO_DEVICE_CONFIGURATION_DEACTIVATION(2035, Keys.VETO_DEVICE_CONFIGURATION_IN_USE_BY_DEVICES, "The device configuration {0} is still used by at least one device"),
    VETO_PROTOCOL_DIALECT_CONFIGURATION_DELETION(2036, Keys.VETO_PROTOCOL_DIALECT_CONFIGURATION_DELETION, "The properties of protocol dialect {0} cannot be deleted because they are still used by at least one device"),
    VETO_PROTOCOL_DIALECT_CONFIGURATION_VALUE_DELETION(2037, Keys.VETO_PROTOCOL_DIALECT_CONFIGURATION_VALUE_DELETION, "The property ''{0}'' of protocol dialect ''{1}'' cannot be deleted because at least one device does not specify a value for it and therefore relies on the configuration level value"),
    VETO_COM_SCHEDULE_DELETION(2041, Keys.VETO_COM_SCHEDULE_DELETION, "The master schedule {0} cannot be removed because it is still used by at least one device"),
    CANNOT_REMOVE_COM_SCHEDULE_BECAUSE_NOT_ON_DEVICE(2042, Keys.CANNOT_REMOVE_COM_SCHEDULE_BECAUSE_NOT_ON_DEVICE, "The master schedule {0} cannot be removed from device {1} because it was not configured on that device"),
    DEVICE_CONFIGURATION_NOT_ACTIVE(2043, Keys.DEVICE_CONFIGURATION_NOT_ACTIVE, "The device configuration must be active"),
    VETO_PARTIAL_CONNECTION_TASK_DELETION(2044, Keys.VETO_PARTIAL_CONNECTION_TASK_DELETION, "The partial connection task {0} of device configuration {1} is still used by at least one connection task on a device of that configuration"),
    VETO_SECURITY_PROPERTY_SET_DELETION(2045, Keys.VETO_SECURITY_PROPERTY_SET_DELETION, "The security property set {0} of device configuration {1} cannot be removed because at least one device of that configuration still has security property values for it"),
    UNIQUE_ADDHOC_COMTASKS_PER_DEVICE(2046, Keys.UNIQUE_ADDHOC_COMTASKS_PER_DEVICE, "The communication task is not unique for device"),
    DUPLICATE_COMTASK_SCHEDULING(2047, Keys.DUPLICATE_COMTASK_SCHEDULING, "One or more communication tasks in the communication schedule are already scheduled on the device with a master communication schedule"),
    COMTASK_CONFIGURATION_INCONSISTENT(2048, Keys.COMTASK_CONFIGURATION_INCONSISTENT, "The communication tasks in the communication schedule doesn''t have the same connection method, security set, protocol dialect and/or urgency"),
    COMTASKS_MUST_BE_ENABLED_BY_CONFIGURATION(2049, Keys.COMTASKS_MUST_BE_ENABLED_BY_CONFIGURATION, "A mismatch between device configuration and the communication schedule (one or more communication tasks defined in the communication schedule is not available on the device configuration of the device)"),
    COM_SCHEDULE_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2050, Keys.COM_SCHEDULE_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not remove communication schedule {0} because it is not owned by device"),
    DEVICE_GROUP_IS_REQUIRED(2051, Keys.DEVICE_GROUP_IS_REQUIRED, "You must specify the devices that are part of the data collection KPI"),
    EMPTY_DATA_COLLECTION_KPI(2052, Keys.EMPTY_DATA_COLLECTION_KPI, "At least one KPI has to be selected"),
    UNKNOWN_DEVICE_MESSAGE_ID_FOR_DEVICE(2053, Keys.UNKNOWN_DEVICE_MESSAGE_ID_FOR_DEVICE, "The deviceMessageId {1} is not known for a device like {0}"),
    DEVICE_MESSAGE_ID_NOT_SUPPORTED(2054, Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED, "The command is not supported for the device"),
    DEVICE_MESSAGE_IS_REQUIRED(2055, Keys.DEVICE_MESSAGE_IS_REQUIRED, "The command is required for a device message attribute"),
    DEVICE_MESSAGE_ATTRIBUTE_NOT_IN_SPEC(2056, Keys.DEVICE_MESSAGE_ATTRIBUTE_NOT_IN_SPEC, "The command attribute is not defined in the specification"),
    DEVICE_MESSAGE_ATTRIBUTE_IS_REQUIRED(2057, Keys.DEVICE_MESSAGE_ATTRIBUTE_IS_REQUIRED, "This field is required"),
    DEVICE_MESSAGE_ATTRIBUTE_INVALID_VALUE(2058, Keys.DEVICE_MESSAGE_ATTRIBUTE_INVALID_VALUE, "This is an invalid value"),
    DEVICE_MESSAGE_NOT_ALLOWED_BY_CONFIG(2059, Keys.DEVICE_MESSAGE_NOT_ALLOWED_BY_CONFIG, "This command is not defined on the device configuration level"),
    DEVICE_MESSAGE_USER_NOT_ALLOWED(2060, Keys.DEVICE_MESSAGE_USER_NOT_ALLOWED, "You are not allowed to create or update this command"),
    DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT(2061, Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT, "You can not update the release date after the command has been sent"),
    DEVICE_MESSAGE_STATUS_INVALID_MOVE(2062, Keys.DEVICE_MESSAGE_STATUS_INVALID_MOVE, "You can not move a device message from status {0} to {1}"),
    DEVICE_MESSAGE_INVALID_REVOKE(2063, Keys.DEVICE_MESSAGE_INVALID_REVOKE, "You can not revoke this command anymore"),
    RELEASE_DATE_IS_REQUIRED(2064, Keys.DEVICE_MESSAGE_RELEASE_DATE_IS_REQUIRED, "The release date is required for a device message"),
    DUPLICATE_FOUND(2065, Keys.DUPLICATE_FOUND, "A duplicate ''{0}'' was found when a unique result was expected for ''{1}''"),
    CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER(2066, Keys.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER, "Could not find a device for identifier ''{0}''"),
    CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER(2067, Keys.CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER, "Could not find a loadprofile for identifier ''{0}''"),
    CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER(2068, Keys.CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER, "Could not find a logbook for identifier ''{0}''"),
    CAN_NOT_FIND_FOR_MESSAGE_IDENTIFIER(2069, Keys.CAN_NOT_FIND_FOR_MESSAGE_IDENTIFIER, "Could not find a message for identifier ''{0}''"),
    CAN_NOT_REPLACE_EXISTING_KPI(2070, Keys.CAN_NOT_REPLACE_EXISTING_KPI, "An existing KPI can not be replaced", Level.SEVERE),
    DUPLICATE_DEVICE_MRID(2071, Keys.DUPLICATE_DEVICE_MRID, "MRID must be unique"),
    FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT(2072, Keys.FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT, "The firmware comtask execution needs to have the 'Firmware management' ComTask"),
    DEVICE_GROUP_MUST_BE_UNIQUE(2073, Keys.DEVICE_GROUP_MUST_BE_UNIQUE, "There is already a KPI for this device group"),
    CAN_NOT_CHANGE_FREQUENCY(2074, Keys.CAN_NOT_CHANGE_FREQUENCY, "The frequency can not be changed"),
    USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES(2075, Keys.USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES, "You are not allowed to edit the security properties"),
    NO_SUCH_COM_SCHEDULE(2076, "NoSuchComSchedule", "No communication schedule with id {0}"),
    NO_SUCH_DEVICE(2077, "NoSuchDevice", "No device with id {0}"),
    NO_METER_ACTIVATION_AT(2078, Keys.NO_METER_ACTIVATION_AT, "There is no meter activation at {0}"),
    LAST_CHECKED_CANNOT_BE_NULL(2079, Keys.LAST_CHECKED_CANNOT_BE_NULL, "The new last checked timestamp cannot be null"),
    LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED(2080, Keys.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED, "The new last checked {2,date,yyyy-MM-dd HH:mm:ss} cannot be after current last checked {1,date,yyyy-MM-dd HH:mm:ss}"),
    CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG(2081, Keys.CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG, "You can not change the configuration of device {0} to the configuration it already has"),
    CANNOT_CHANGE_DEVICE_CONFIG_NOT_ALL_CONFLICTS_SOLVED(2082, Keys.CANNOT_CHANGE_DEVICE_CONFIG_NOT_ALL_CONFLICTS_SOLVED, "You can not change the configuration of device {0} to the configuration {1} because there are still unsolved conflicts"),
    CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE(2083, Keys.CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE, "You can not change the configuration of a device to a configuration of another devicetype"),
    NO_DESTINATION_DEVICE_CONFIG_FOUND_FOR_VERSION(2084, Keys.NO_DESTINATION_DEVICE_CONFIG_FOUND_FOR_VERSION, "No destination device configuration found for id {0} and version {1}"),
    VETO_CONFIG_CHANGE_ACTIVE_NO_NEW_CONFLICTS_ALLOWED(2085, Keys.VETO_CONFIG_CHANGE_ACTIVE_NO_NEW_CONFLICTS_ALLOWED, "There is currently an active configuration change happening, your change would create new conflicts, please wait untill the action has finished."),
    DUPLICATE_COMTASK(2086, Keys.DUPLICATE_COMTASK, "One or more communication tasks in the communication schedule are already scheduled on the device"),
    INVALID_SEARCH_DOMAIN(2087, Keys.INVALID_SEARCH_DOMAIN, "You are trying to look for an invalid search domain : {0}"),
    NO_DESTINATION_SPEC_FOUND(2088, Keys.NO_DESTINATION_SPEC_FOUND, "No destination spec found for : {0}"),
    NO_DEVICE_CONFIG_CHANGE_BUSINESS_LOCK_FOUND(2089, Keys.NO_DEVICE_CONFIG_CHANGE_BUSINESS_LOCK_FOUND, "No device config change business lock found for id {0}"),
    INCORRECT_DEVICE_VERSION(2091, Keys.INCORRECT_DEVICE_VERSION, "No device found for id {0} and version {1}"),
    NO_DEVICE_CONFIG_CHANGE_SINGLE_DEVICE_BUSINESS_LOCK_FOUND(2092, Keys.NO_DEVICE_CONFIG_CHANGE_SINGLE_DEVICE_BUSINESS_LOCK_FOUND, "No device config change business lock for single device found for id {0}"),
    BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_CONFIG(2093, Keys.BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_CONFIG, "You need to search a specific device configuration in order to use the bulk action for change device configuration"),
    BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_UNIQUE_CONFIG(2094, Keys.BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_UNIQUE_CONFIG, "You need to search on a unique device configuration in order to use the bulk action for change device configuration"),
    BULK_CHANGE_CONFIG_INVALID_SEARCH_VALUE(2095, Keys.BULK_CHANGE_CONFIG_INVALID_SEARCH_VALUE, "You provided an invalid search value for the property {0}"),
    PRIORITY_NOT_IN_RANGE(2096, Keys.PRIORITY_NOT_IN_RANGE, "Value must be between {min} and {max}"),
    CHANGE_CONFIG_WRONG_DEVICE_STATE(2097, Keys.CHANGE_CONFIG_WRONG_DEVICE_STATE, "Device configuration cannot be changed on device with name: {0} as this device has the {1} state."),
    MULTIPLIER_SHOULD_BE_LARGER_THAN_ZERO(2098, Keys.MULTIPLIER_OF_ZERO_IS_NOT_ALLOWED, "The multiplier should be larger than zero"),
    MULTIPLIER_VALUE_EXCEEDS_MAX_VALUE(2099, Keys.MULTIPLIER_VALUE_EXCEEDS_MAX_VALUE, "The multiplier exceeds the max value " + Integer.MAX_VALUE),
    READING_OVERFLOW_DETECTED(2100, Keys.READING_OVERFLOW_DETECTED, "Reading of device {0} and reading type {1} overflowed at {2}, corrected value from {3} to {4}.", Level.WARNING),
    COM_TASK_ENABLEMENT_UPDATE_RESTRICTED(2101, Keys.COM_TASK_ENABLEMENT_UPDATE_RESTRICTED, "The communication task configuration is part of shared communication schedule on some devices" +
            " and all communication tasks in that shared communication schedule should have the same connection method, security set, protocol dialect and/or urgency", Level.SEVERE),
    NO_SUCH_COMTASK_ENABLEMENT(2102, Keys.NO_SUCH_COM_TASK_ENABLEMENT, "No comtask enablement with id {0}"),
    TRACKING_ID_MISSING(2103, Keys.DEVICE_MESSAGE_TRACKING_ID_MISSING, "Tracking id is missing"),
    TRACKING_CATEGORY_MISSING(2104, Keys.DEVICE_MESSAGE_TRACKING_CATEGORY_MISSING, "Tracking category is missing"),
    NO_SUCH_DEVICE_MESSAGE(2105, Keys.NO_SUCH_DEVICE_MESSAGE, "No such device message"),
    NO_SUCH_USAGE_POINT(2106, Keys.NO_SUCH_USAGE_POINT, "No such usage point"),
    NO_CURRENT_METER_ACTIVATION(2107, Keys.NO_CURRENT_METER_ACTIVATION, "The requested meter does not seem to be active at the moment (no current meter activation)"),
    NO_METER_IN_ACTIVATION(2108, Keys.NO_METER_IN_ACTIVATION, "The meter activation does not seem to contain a meter"),
    NO_COMTASK_FOR_COMMAND(2110, Keys.NO_COMTASK_FOR_COMMAND, "A comtask to execute the device messages could not be located"),
    UNKNOWN_STATUS(2111, Keys.UNKNOWN_STATUS, "The requested contactor status is not supported at this time"),
    UNKNOWN_UNIT_CODE(2112, Keys.UNKNOWN_UNIT_CODE, "The requested load limit unit is not supported at this time"),
    INCOMPLETE_LOADLIMIT(2113, Keys.INCOMPLETE_LOADLIMIT, "Received incomplete load limit - please make sure to specify both the limit and the unit."),
    UNKNOWN_READING_TYPE(2114, Keys.UNKNOWN_READING_TYPE, "The requested load limit reading type is not supported at this time"),
    NO_DESTINATION_SPEC(2117, Keys.NO_DESTINATION_SPEC, "No such destination spec"),
    DUPLICATE_REGISTER_OBISCODE(2118, Keys.DUPLICATE_REGISTER_OBISCODE, "The OBIS code must be unique for all registers of your device"),
    DUPLICATE_CHANNEL_OBISCODE(2119, Keys.DUPLICATE_CHANNEL_OBISCODE, "The OBIS code must be unique for all the channels of your load profile"),
    OVERFLOW_INCREASED(2120, Keys.OVERFLOW_INCREASED, "The overflow value should not exceed the value of the configuration"),
    VETO_CANNOT_CHANGE_OBISCODE_CONFIG_ALREADY_OVERRIDDEN_DEVICE(2121, Keys.VETO_CANNOT_CHANGE_OBISCODE_CONFIG_ALREADY_OVERRIDDEN_DEVICE, "You can not change the OBIS code, you already have devices with an overridden value for this OBIS code: {0}"),
    UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT(2122, Keys.UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT, "This device doesn''t have the following reading types that are specified in the metrology configurations of the selected usage point: {0}"),
    USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE(2123, "usagePointAlreadyLinkedToAnotherDeviceX", "The usage point is already linked to device {0} starting from {1}"),
    USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE_UNTIL(2124, "usagePointAlreadyLinkedToAnotherDeviceXUntil", "The usage point is already linked to device {0} starting from {1} until {2}"),
    METER_ACTIVATION_TIMESTAMP_NOT_AFTER_LAST_ACTIVATION(2125, "meterActivationTimestampNotAfterLastActivation", "The activation date {0} should be after the last meter activation date {1}"),
    CHANGE_DEVICE_CONFIG_UNSATISFIED_REQUIREMENTS(2126, "changeDeviceConfigUnsatisfiedRequirements", "The device {0} can''t have the device configuration {1} because this device configuration doesn''t have the following reading types that are specified in the metrology configuration versions of the usage points that are linked to this device: {2}"),
    NO_SUCH_END_DEVICE_CONTROL_TYPE(2127, "NoSuchEndDeviceControlType", "No end device control type with MRID {0}"),
    NO_SUCH_DEVICE_MESSAGE_SPEC(2128, "NoSuchDeviceMessageSpec", "No such device message spec: {0}"),
    NO_STATUS_INFORMATION_COMTASK(2129, Keys.NO_STATUS_INFORMATION_COMTASK, "A comtask to read out the status information could not be located"),
    DEVICE_MESSAGE_REVOKE_PICKED_UP_BY_COMSERVER(2130, Keys.DEVICE_MESSAGE_REVOKE_PICKED_UP_BY_COMSERVER, "A communication server is currently executing this command, therefore it cannot be revoked"),
    COULD_NOT_FIND_SERVICE_CALL_TYPE(2131, Keys.COULD_NOT_FIND_SERVICE_CALL_TYPE, "Could not find service call type {0} having version {1}"),
    COMMAND_ARGUMENT_SPEC_NOT_FOUND(2132, Keys.COMMAND_ARGUMENT_SPEC_NOT_FOUND, "Could not find the command argument spec {0} for command {1}"),
    VAL_KPI_DEVICEGROUP_DELETION(2133, "deviceGroupXstillInUseValidation", "Device group {0} is still in use by a data validation KPI"),
    KPIS_DEVICEGROUP_DELETION(2134, "deviceGroupXstillInUseMultiple", "Device group {0} is still in use by: data validation KPI, data collection KPI"),
    VETO_ALLOWED_CALENDAR_OBSOLETE(2135, Keys.VETO_ALLOWED_CALENDAR_IN_USE, "The allowed calendar {0} is still used by at least one device"),
    CANNOT_CHANGE_CONFIG_DATALOGGER_SLAVE(2136, Keys.CANNOT_CHANGE_CONFIG_DATALOGGER_SLAVE, "You cannot change the configuration of a datalogger slave"),
    CANNOT_CHANGE_CONFIG_TO_DATALOGGER_ENABLED(2137, Keys.CANNOT_CHANGE_CONFIG_TO_DATALOGGER_ENABLED, "You cannot change the configuration to a datalogger enabled device"),
    CANNOT_CHANGE_CONFIG_FROM_DATALOGGER_ENABLED(2138, Keys.CANNOT_CHANGE_CONFIG_FROM_DATALOGGER_ENABLED, "You cannot change the configuration of a datalogger enabled device"),
    INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS(2139, Keys.INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS, "Invalid number of simultaneous connections, should be between 1 and 16"),
    INVALID_SHIPMENT_DATE(2140, Keys.INVALID_SHIPMENT_DATE, "The shipment date ({0}) should be between {1} and {2}"),
    DUPLICATE_DEVICE_NAME(2141, Keys.DUPLICATE_DEVICE_NAME, "Name must be unique"),
    MULTIPLE_COMSCHEDULES_WITH_SAME_COMTASK(2142, Keys.MULTIPLE_COMSCHEDULES_WITH_SAME_COMTASK, "You can not set a ComSchedule on the device because it already contains a ComTask which is linked to another ComSchedule"),
    GROUP_IS_USED_BY_ANOTHER_GROUP(2143, Keys.GROUP_IS_USED_BY_ANOTHER_GROUP, "The group is used by another group")
    ;
    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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

    @Override
    public String getModule() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    public static class Keys {
        public static final String FIELD_REQUIRED = "X.field.required";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String COMPORT_TYPE_NOT_SUPPORTED = "comPortTypeOfComPortPoolMustBeSupportedByConnectionType";
        public static final String CONNECTION_TASK_DEVICE_REQUIRED = "connectionType.device.required";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED = "connectionType.partialConnectionTask.required";
        public static final String DUPLICATE_CONNECTION_TASK = "connectionType.duplicate";
        public static final String CONNECTION_TASK_INCOMPATIBLE_PARTIAL = "connectionType.incompatiblePartialConnectionTask";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION = "connectionType.partialConnectionTaskNotInConfiguration";
        public static final String CONNECTION_TASK_IS_ALREADY_OBSOLETE = "connectionTask.isAlreadyObsolete";
        public static final String CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE = "connectionTask.isExecutingAndCannotObsolete";
        public static final String DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE = "defaultConnectionTask.isInUseAndCannotObsolete";
        public static final String CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED = "connectionTask.pluggableClass.required";
        public static final String CONNECTION_TASK_COMPORT_POOL_REQUIRED = "connectionTask.comPortPool.required";
        public static final String CONNECTION_TASK_INVALID_PROPERTY = "connectionTask.property.invalid";
        public static final String CONNECTION_TASK_PROPERTY_NOT_IN_SPEC = "connectionTaskPropertyXIsNotInConnectionTypeSpec";
        public static final String CONNECTION_TASK_PROPERTY_INVALID_VALUE = "connectionTaskProperty.value.invalid";
        public static final String CONNECTION_TASK_REQUIRED_PROPERTY_MISSING = "connectionTaskProperty.required";
        public static final String CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE = "inboundConnectionTask.comPortPool.uniquePerDevice";
        public static final String OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED = "outboundConnectionTask.strategy.required";
        public static final String OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS = "outboundConnectionTask.strategy.incompatibleWithSimultaneous";
        public static final String OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED = "outboundConnectionTask.nextExecutionSpecs.required";
        public static final String OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY = "outboundConnectionTask.nextExecutionSpecs.offsetBiggerThenFrequency";
        public static final String OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW = "outboundConnectionTask.nextExecutionSpecs.offsetNotWithinWindow";
        public static final String OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW = "outboundConnectionTask.nextExecutionSpecs.longOffsetNotWithinWindow";
        public static final String VALUE_IS_REQUIRED = "X.value.required";
        public static final String DUPLICATE_DEVICE_MRID = "deviceDuplicateMrid";
        public static final String DUPLICATE_DEVICE_NAME = "deviceDuplicateName";
        public static final String PHYSICAL_GATEWAY_STILL_IN_USE = "device.delete.linked.physical.gateway";
        public static final String COMMUNICATION_GATEWAY_STILL_IN_USE = "device.delete.linked.communication.gateway";
        public static final String PROPERTY_SPEC_DOESNT_EXIST = "device.property.infotype.required";
        public static final String PROPERTY_NOT_ON_DEVICE_PROTOCOL = "not.deviceprotocol.property";
        public static final String CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "cannotDeleteIfNotFromDevice";
        public static final String PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED = "protocolDialectConfigurationProperties.required";
        public static final String DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED = "deviceProtocolDialectProperty.device.required";
        public static final String DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC = "deviceProtocolDialectPropertyXIsNotInSpec";
        public static final String DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE = "deviceProtocolDialectProperty.value.invalid";
        public static final String DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING = "deviceProtocolDialectProperty.required";
        public static final String DEVICE_IS_REQUIRED = "deviceIsRequired";
        public static final String COMTASK_IS_REQUIRED = "comTaskIsRequired";
        public static final String COMSCHEDULE_IS_REQUIRED = "comScheduleIsRequired";
        public static final String NEXTEXECUTIONSPEC_IS_REQUIRED = "nextExecutionSpecIsRequired";
        public static final String PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED = "protocolDialectConfigurationPropertiesAreRequired";
        public static final String COM_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE = "comTaskExecutionIsObsoleteAndCanNotBeUpdated";
        public static final String COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE = "comTaskExecutionAlreadyObsolete";
        public static final String COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE = "comTaskExecutionCannotObsoleteCurrentlyExecuting";
        public static final String COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "comTaskExecutionCannotDeleteNotFromDevice";
        public static final String VETO_COM_TASK_ENABLEMENT_DELETION = "comTaskExecution.comTaskEnablement.inUse";
        public static final String VETO_PARTIAL_CONNECTION_TASK_DELETION = "partialConnectionTask.inUse";
        public static final String VETO_SECURITY_PROPERTY_SET_DELETION = "securityPropertySet.inUse";
        public static final String VETO_DEVICE_CONFIGURATION_IN_USE_BY_DEVICES = "deviceConfiguration.inUse";
        public static final String VETO_PROTOCOL_DIALECT_CONFIGURATION_DELETION = "deviceConfiguration.protocolDialect.inUse";
        public static final String VETO_PROTOCOL_DIALECT_CONFIGURATION_VALUE_DELETION = "deviceConfiguration.protocolDialect.value.inUse";
        public static final String CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT = "connectionTaskRequiredWhenNotUsingDefault";
        public static final String PRIORITY_NOT_IN_RANGE = "priorityNotInRange";
        public static final String UNIQUE_ADDHOC_COMTASKS_PER_DEVICE = "uniqueComTasksPerDevice";
        public static final String DUPLICATE_COMTASK_SCHEDULING = "duplicateComTaskScheduling";
        public static final String DUPLICATE_COMTASK = "duplicateComTask";
        public static final String COMTASK_CONFIGURATION_INCONSISTENT = "comTaskConfigurationInconsistent";
        public static final String COMTASKS_MUST_BE_ENABLED_BY_CONFIGURATION = "comTasksMustBeEnabledByConfiguration";
        public static final String VETO_COM_SCHEDULE_DELETION = "comTaskExecution.comSchedule.inUse";
        public static final String CANNOT_REMOVE_COM_SCHEDULE_BECAUSE_NOT_ON_DEVICE = "cannotDeleteComScheduleFromDevice";
        public static final String DEVICE_CONFIGURATION_NOT_ACTIVE = "device.configuration.not.active";
        public static final String COM_SCHEDULE_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "comScheduleCannotDeleteNotFromDevice";
        public static final String DEVICE_GROUP_IS_REQUIRED = "endDeviceGroupIsRequired";
        public static final String EMPTY_DATA_COLLECTION_KPI = "dataCollectionKpi.cannotBeEmpty";
        public static final String UNKNOWN_DEVICE_MESSAGE_ID_FOR_DEVICE = "unknown.deviceMessageId.device";
        public static final String DEVICE_MESSAGE_ID_NOT_SUPPORTED = "deviceMessageId.deviceMessage.device.notSupported";
        public static final String DEVICE_MESSAGE_IS_REQUIRED = "deviceMessage.required.deviceMessageAttribute";
        public static final String DEVICE_MESSAGE_ATTRIBUTE_NOT_IN_SPEC = "deviceMessageAttribute.not.defined";
        public static final String DEVICE_MESSAGE_ATTRIBUTE_IS_REQUIRED = "deviceMessageAttribute.required";
        public static final String DEVICE_MESSAGE_ATTRIBUTE_INVALID_VALUE = "deviceMessageAttribute.invalid.value";
        public static final String DEVICE_MESSAGE_NOT_ALLOWED_BY_CONFIG = "deviceMessage.not.allowed.config";
        public static final String DEVICE_MESSAGE_USER_NOT_ALLOWED = "deviceMessage.user.not.allowed";
        public static final String DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT = "deviceMessage.releaseDate.update.sent";
        public static final String DEVICE_MESSAGE_STATUS_INVALID_MOVE = "deviceMessage.status.invalid.move";
        public static final String DEVICE_MESSAGE_INVALID_REVOKE = "deviceMessage.revoke.invalid.status";
        public static final String DEVICE_MESSAGE_REVOKE_PICKED_UP_BY_COMSERVER = "deviceMessage.revoke.picked.up.by.comserver";
        public static final String DEVICE_MESSAGE_RELEASE_DATE_IS_REQUIRED = "releaseDateIsRequired.deviceMessage";
        public static final String DUPLICATE_FOUND = "duplicateFound";
        public static final String CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER = "deviceIdentifier.not.found";
        public static final String CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER = "loadprofileIdentifier.not.found";
        public static final String CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER = "logbookIdentifier.not.found";
        public static final String CAN_NOT_FIND_FOR_MESSAGE_IDENTIFIER = "messageIdentifier.not.found";
        public static final String CAN_NOT_REPLACE_EXISTING_KPI = "dataCollectionKpi.canNotReplaceExistingKpi";
        public static final String FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT = "firmware.comtaskexec.needs.firmware.comtaskenablement";
        public static final String DEVICE_GROUP_MUST_BE_UNIQUE = "kpi.deviceGroup.unique";
        public static final String CAN_NOT_CHANGE_FREQUENCY = "kpi.frequency.immutable";
        public static final String USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES = "securityPropertySet.edit.notAllowed";
        public static final String NO_METER_ACTIVATION_AT = "no.meteractivation";
        public static final String LAST_CHECKED_CANNOT_BE_NULL = "lastChecked.null";
        public static final String LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED = "lastChecked.after.currentLastChecked";
        public static final String CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG = "cannotChangeDeviceToSameConfig";
        public static final String CANNOT_CHANGE_DEVICE_CONFIG_NOT_ALL_CONFLICTS_SOLVED = "cannotChangeConfigStillUnsolvedConflicts";
        public static final String CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE = "cannotChangeToConfigOfOtherDeviceType";
        public static final String NO_DESTINATION_DEVICE_CONFIG_FOUND_FOR_VERSION = "noDestinationDeviceConfigFoundForVersion";
        public static final String VETO_CONFIG_CHANGE_ACTIVE_NO_NEW_CONFLICTS_ALLOWED = "activeConfigChangeNoNewConflictsAllowed";
        public static final String INVALID_SEARCH_DOMAIN = "device.invalidSearchDomain";
        public static final String NO_DESTINATION_SPEC_FOUND = "device.noDestinationSpecFound";
        public static final String NO_DEVICE_CONFIG_CHANGE_BUSINESS_LOCK_FOUND = "device.configchange.noBusinessLock";
        public static final String INCORRECT_DEVICE_VERSION = "incorrect.device.version";
        public static final String NO_DEVICE_CONFIG_CHANGE_SINGLE_DEVICE_BUSINESS_LOCK_FOUND = "device.configchange.single.device.noBusinessLock";
        public static final String BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_CONFIG = "bulk.device.configchange.needto.search.on.config";
        public static final String BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_UNIQUE_CONFIG = "bulk.device.configchange.needto.search.on.unique.config";
        public static final String BULK_CHANGE_CONFIG_INVALID_SEARCH_VALUE = "bulk.device.configchange.invalid.search.item";
        public static final String CHANGE_CONFIG_WRONG_DEVICE_STATE = "change.config.wrong.device.state";
        public static final String CANNOT_CONFIGURE_DEVICE_MULTIPLIER_IN_PAST_WHEN_DATA_EXISTS = "cannot.configure.device.multiplier.in.past.when.data.exists";
        public static final String MULTIPLIER_MUST_HAVE_METERACTIVATION = "multiplier.must.have.meteractivation";
        public static final String MULTIPLIER_OF_ZERO_IS_NOT_ALLOWED = "multiplier.zero.not.allowed";
        public static final String MULTIPLIER_VALUE_EXCEEDS_MAX_VALUE = "multiplier.exceeds.max.value";
        public static final String READING_OVERFLOW_DETECTED = "reading.overflow";
        public static final String COM_TASK_ENABLEMENT_UPDATE_RESTRICTED = "com.task.enablement.update.restricted";
        public static final String NO_SUCH_COM_TASK_ENABLEMENT = "NoSuchComTaskEnablement";
        public static final String DEVICE_MESSAGE_TRACKING_ID_MISSING = "TrackingIdMissing";
        public static final String DEVICE_MESSAGE_TRACKING_CATEGORY_MISSING = "TrackingCategoryMissing";
        public static final String NO_SUCH_DEVICE_MESSAGE = "No.such.device.message";
        public static final String NO_SUCH_USAGE_POINT = "No.such.usage.point";
        public static final String NO_CURRENT_METER_ACTIVATION = "The.requested.meter.does.not.seem.to.be.active.at.the.moment.(no.current.meter.activation)";
        public static final String NO_METER_IN_ACTIVATION = "The.meter.activation.does.not.seem.to.contain.a.meter";
        public static final String NO_COMTASK_FOR_COMMAND = "A.comtask.to.execute.the.device.messages.could.not.be.located";
        public static final String UNKNOWN_STATUS = "The.requested.contactor.status.is.not.supported.at.this.time";
        public static final String UNKNOWN_UNIT_CODE = "The.requested.load.limit.unit.is.not.supported.at.this.time";
        public static final String INCOMPLETE_LOADLIMIT = "Received.incomplete.load.limit.-.please.make.sure.to.specify.both.the.limit.and.the.unit.";
        public static final String UNKNOWN_READING_TYPE = "The.requested.load.limit.reading.type.is.not.supported.at.this.time";
        public static final String NO_COMTASK_FOR_STATUS_INFORMATION = "A.comtask.to.verify.the.status.information.could.not.be.located";
        public static final String NO_SUCH_DEVICE = "No.such.device";
        public static final String NO_DESTINATION_SPEC = "No.such.Destination.Spec";
        public static final String DUPLICATE_REGISTER_OBISCODE = "duplicate.register.obiscode";
        public static final String DUPLICATE_CHANNEL_OBISCODE = "duplicate.channel.obiscode";
        public static final String OVERFLOW_INCREASED = "overflow.increased";
        public static final String VETO_CANNOT_CHANGE_OBISCODE_CONFIG_ALREADY_OVERRIDDEN_DEVICE = "cannot.change.obiscode.config.already.overriden.device";
        public static final String UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT = "unsatisfied.reading.type.requirements.of.usage.point";
        public static final String NO_STATUS_INFORMATION_COMTASK = "no.status.information.comtask";
        public static final String COULD_NOT_FIND_SERVICE_CALL_TYPE = "could.not.find.service.call.type";
        public static final String COMMAND_ARGUMENT_SPEC_NOT_FOUND = "command.argument.spec.not.found";
        public static final String VETO_ALLOWED_CALENDAR_IN_USE = "allowed.calendar.in.use";
        public static final String CANNOT_CHANGE_CONFIG_DATALOGGER_SLAVE = "device.config.change.not.on.slave";
        public static final String CANNOT_CHANGE_CONFIG_TO_DATALOGGER_ENABLED = "device.config.change.not.to.datalogger";
        public static final String CANNOT_CHANGE_CONFIG_FROM_DATALOGGER_ENABLED = "device.config.change.not.from.datalogger";
        public static final String INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS = "InvalidNumberOfSimultaneousConnections";
        public static final String INVALID_SHIPMENT_DATE = "InvalidShipmentDate";
        public static final String MULTIPLE_COMSCHEDULES_WITH_SAME_COMTASK = "multiple.comschedules.with.same.comtask";
        public static final String GROUP_IS_USED_BY_ANOTHER_GROUP = "group.is.used.by.another.group";

    }
}
