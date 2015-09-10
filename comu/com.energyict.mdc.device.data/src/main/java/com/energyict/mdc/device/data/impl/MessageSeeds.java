package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceDataServices;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the device data module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-06 (14:41)
 */
public enum MessageSeeds implements MessageSeed {
    LEGACY(100, "legacy.exception", "Coding: BusinessException or SQLException from legacy code that has not been ported to the jupiter ORM framework"),
    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required"),
    CODING_RELATION_IS_ALREADY_OBSOLETE(1001, "relation.isAlreadyObsolete", "Cannot remove a property because the relation (of type ''{0}'') that holds it is already obsolete"),
    CODING_NO_PROPERTIES_EXPECTED(1002, "noAttributesExpected", "Was not expecting a value to be added for property ''{0}'' because the pluggable does not have any properties"),
    UNEXPECTED_RELATION_TRANSACTION_ERROR(1003, "unExpectedRelationTransactionError", "Unexpected problem occurred in the relation transaction framework"),
    COMPORT_TYPE_NOT_SUPPORTED(1004, Keys.COMPORT_TYPE_NOT_SUPPORTED, "The communication port type of the communication port pool must be supported by the connection type"),
    CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED(1006, Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED, "A connection task requires a connection type pluggable class"),
    CONNECTION_TASK_COMPORT_POOL_REQUIRED(1007, Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED, "A connection task requires a communication port pool"),
    MRID_IS_REQUIRED(1008, Keys.MRID_REQUIRED, "The MRID is required"),
    DEVICE_TYPE_IS_REQUIRED(1009, Keys.DEVICE_TYPE_REQUIRED, "The device type is required"),
    DEVICE_CONFIGURATION_IS_REQUIRED(1010, Keys.DEVICE_CONFIGURATION_REQUIRED, "The device configuration is required"),
    FIELD_TOO_LONG(1011, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    VETO_COMPORTPOOL_DELETION(1012, "comPortPoolXstillInUseByY", "ComPortPool {0} is still in use by at least one device"),
    VETO_DEVICEGROUP_DELETION(1013, "deviceGroupXstillInUse", "Device group {0} is still in use by a data collection KPI"),
    FIELD_IS_REQUIRED(1014, Keys.FIELD_REQUIRED, "This field is required"),
    CONNECTION_TASK_DEVICE_REQUIRED(2000, Keys.CONNECTION_TASK_DEVICE_REQUIRED, "A connection type should be linked to a device"),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED(2001, Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED, "A connection type should be linked to a partial connection task from the device configuration"),
    DUPLICATE_CONNECTION_TASK(2002, Keys.DUPLICATE_CONNECTION_TASK, "The partial connection task {0} is already used by connection task {1} on device {2} and therefore no other connection task with the same partial connection task can be added"),
    CONNECTION_TASK_INCOMPATIBLE_PARTIAL(2003, Keys.CONNECTION_TASK_INCOMPATIBLE_PARTIAL, "The type of the partial connection task of a connection task must be compatible. Expected ''{0}'' but got ''{1}''"),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION(2004, Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION, "A connection task must be added against a partial connection task (id={0}, configuration id={1}) from the same device configuration (id={2})"),
    CONNECTION_TASK_IS_ALREADY_OBSOLETE(2005, Keys.CONNECTION_TASK_IS_ALREADY_OBSOLETE, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}"),
    CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE(2006, Keys.CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is currently being executed by communication server ''{2}''"),
    CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE(2007, Keys.CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE, "The connection task ''{0}'' on device {1} cannot be updated because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}"),
    DEFAULT_CONNECTION_TASK_IS_INUSE_AND_CANNOT_DELETE(2008, Keys.DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE, "The default connection task ''{0}'' on device {1} cannot be removed because it is still in use by communication tasks"),
    CONNECTION_TASK_INVALID_PROPERTY(2009, Keys.CONNECTION_TASK_INVALID_PROPERTY, "Invalid value"),
    CONNECTION_TASK_PROPERTY_NOT_IN_SPEC(2010, Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC, "ConnectionType '{0}' does not contain a specification for attribute '{1}'"),
    CONNECTION_TASK_PROPERTY_INVALID_VALUE(2011, Keys.CONNECTION_TASK_PROPERTY_INVALID_VALUE, "''{0}'' is not a valid value for attribute ''{1}'' of ConnectionType ''{2}''"),
    CONNECTION_TASK_REQUIRED_PROPERTY_MISSING(2012, Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING, "This value is required"),
    CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE(2013, Keys.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE, "An inbound communication port pool can only be used once on the same device"),
    OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED(2014, Keys.OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED, "An outbound connection task requires a connection strategy"),
    OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS(2015, Keys.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS, "Outbound connection task with strategy to minimize connections is not compatible with simultaneous connections"),
    OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED(2016, Keys.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED, "An outbound connection task with strategy to minimize connections requires execution scheduling specifications"),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY(2017, Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY, "The offset of the next execution scheduling specifications should not extend its frequency"),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW(2018, Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW, "The offset of the next execution scheduling specifications is not within the communication window"),
    OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW(2019, Keys.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW, "The offset of the next execution scheduling specifications within a week or month, once calculated back to a daily offset is not within the communication window"),
    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(2020, Keys.PHYSICAL_GATEWAY_STILL_IN_USE, "You can not remove device '{0}' because it is still used as a physical gateway for '{1}'"),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(2021, Keys.COMMUNICATION_GATEWAY_STILL_IN_USE, "You can not remove device '{0}' because it is still used as a communication gateway for '{1}'"),
    DEVICE_PROPERTY_HAS_NO_SPEC(2022, Keys.PROPERTY_SPEC_DOESNT_EXIST, "The property specification for property value '{0}' does not exist."),
    DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL(2023, Keys.PROPERTY_NOT_ON_DEVICE_PROTOCOL, "The property '{0}' is not defined by the device protocol '{1}' of device '{2}'"),
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
    VETO_COM_TASK_ENABLEMENT_DELETION(2034, Keys.VETO_COM_TASK_ENABLEMENT_DELETION, "The communication task '{0}' is still used by devices having the configuration '{1}'"),
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
    COMTASK_CONFIGURATION_INCONSISTENT(2048, Keys.COMTASK_CONFIGURATION_INCONSISTENT, "The communication tasks in the communication schedule doesn't have the same connection method, security set, protocol dialect and/or urgency"),
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
    DUPLICATE_FOUND(2065, Keys.DUPLICATE_FOUND, "A duplicate '{0}' was found when a unique result was expected for '{1}'"),
    CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER(2066, Keys.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER, "Could not find a device for identifier '{0}'"),
    CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER(2067, Keys.CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER, "Could not find a loadprofile for identifier '{0}'"),
    CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER(2068, Keys.CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER, "Could not find a logbook for identifier '{0}'"),
    CAN_NOT_FIND_FOR_MESSAGE_IDENTIFIER(2069, Keys.CAN_NOT_FIND_FOR_MESSAGE_IDENTIFIER, "Could not find a message for identifier '{0}'"),
    CAN_NOT_REPLACE_EXISTING_KPI(2070, Keys.CAN_NOT_REPLACE_EXISTING_KPI, "An existing KPI can not be replaced", Level.SEVERE ),
    DUPLICATE_DEVICE_MRID(2071, Keys.DUPLICATE_DEVICE_MRID, "MRID must be unique"),
    FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT(2072, Keys.FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT, "The firmware comtask execution needs to have the 'Firmware management' ComTask"),
    DEVICE_GROUP_MUST_BE_UNIQUE(2073, Keys.DEVICE_GROUP_MUST_BE_UNIQUE, "There is already a KPI for this device group"),
    CAN_NOT_CHANGE_FREQUENCY(2074, Keys.CAN_NOT_CHANGE_FREQUENCY, "The frequency can not be changed"),
    USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES(2075, Keys.USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES, "You are not allowed to edit the security properties"),
    NO_SUCH_COM_SCHEDULE(2076, "NoSuchComSchedule" , "No communication schedule with id {0}"),
    NO_SUCH_DEVICE(2077, "NoSuchDevice" , "No device with MRID {0}"),
    NO_METER_ACTIVATION_AT(2078, Keys.NO_METER_ACTIVATION_AT, "There is no meter activation at {0}"),
    LAST_CHECKED_CANNOT_BE_NULL(2079, Keys.LAST_CHECKED_CANNOT_BE_NULL, "The new last checked timestamp cannot be null"),
    LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED(2080, Keys.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED, "The new last checked {2,date,yyyy-MM-dd HH:mm:ss} cannot be after current last checked {1,date,yyyy-MM-dd HH:mm:ss}"),
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
        public static final String NAME_REQUIRED = "X.name.required";
        public static final String FIELD_REQUIRED = "X.field.required";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String MRID_REQUIRED = "mRIDRequired";
        public static final String DEVICE_TYPE_REQUIRED = "deviceTypeRequired";
        public static final String DEVICE_CONFIGURATION_REQUIRED = "deviceConfigurationRequired";
        public static final String COMPORT_TYPE_NOT_SUPPORTED = "comPortTypeOfComPortPoolMustBeSupportedByConnectionType";
        public static final String CONNECTION_TASK_DEVICE_REQUIRED = "connectionType.device.required";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED = "connectionType.partialConnectionTask.required";
        public static final String DUPLICATE_CONNECTION_TASK = "connectionType.duplicate";
        public static final String CONNECTION_TASK_INCOMPATIBLE_PARTIAL = "connectionType.incompatiblePartialConnectionTask";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION = "connectionType.partialConnectionTaskNotInConfiguration";
        public static final String CONNECTION_TASK_IS_ALREADY_OBSOLETE = "connectionTask.isAlreadyObsolete";
        public static final String CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE = "connectionTask.isExecutingAndCannotObsolete";
        public static final String DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE = "defaultConnectionTask.isInUseAndCannotObsolete";
        public static final String CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE = "connectionTask.isObsoleteAndCannotUpdate";
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
    }
}