package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.DeviceDataService;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the device data module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-06 (14:41)
 */
public enum MessageSeeds implements MessageSeed {
    LEGACY(100, "legacy.exception", "Coding: BusinessException or SQLException from legacy code that has not been ported to the jupiter ORM framework", Level.SEVERE),
    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED_KEY, "The name is required", Level.SEVERE),
    CODING_RELATION_IS_ALREADY_OBSOLETE(1001, "relation.isAlreadyObsolete", "Cannot delete a property because the relation (of type ''{0}'') that holds it is already obsolete", Level.SEVERE),
    CODING_NO_PROPERTIES_EXPECTED(1002, "noAttributesExpected", "Was not expecting a value to be added for property ''{0}'' because the pluggable does not have any properties", Level.SEVERE),
    UNEXPECTED_RELATION_TRANSACTION_ERROR(1003, "unExpectedRelationTransactionError", "Unexpected problem occurred in the relation transaction framework", Level.SEVERE),
    COMPORT_TYPE_NOT_SUPPORTED(1004, Keys.COMPORT_TYPE_NOT_SUPPORTED_KEY, "The communication port type of the communication port pool must be supported by the connection type", Level.SEVERE),
    CONNECTION_METHOD_ALREADY_EXISTS(1005, "connectionMethod.duplicateNameX", "A connection method with name '{0}' already exists", Level.SEVERE),
    CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED(1006, Keys.CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED_KEY, "A connection method requires a connection type pluggable class", Level.SEVERE),
    CONNECTION_METHOD_COMPORT_POOL_REQUIRED(1007, Keys.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY, "A connection method requires a communication port pool", Level.SEVERE),
    MRID_IS_REQUIRED(1008, Keys.MRID_REQUIRED_KEY, "The MRID is required", Level.SEVERE),
    DEVICE_TYPE_IS_REQUIRED(1009, Keys.DEVICE_TYPE_REQUIRED_KEY, "The device type is required", Level.SEVERE),
    DEVICE_CONFIGURATION_IS_REQUIRED(1010, Keys.DEVICE_CONFIGURATION_REQUIRED_KEY, "The device configuration is required", Level.SEVERE),
    FIELD_TOO_LONG(1011, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters", Level.SEVERE),
    CONNECTION_TASK_DEVICE_REQUIRED(2000, Keys.CONNECTION_TASK_DEVICE_REQUIRED_KEY, "A connection type should be linked to a device", Level.SEVERE),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED(2001, Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED_KEY, "A connection type should be linked to a partial connection task from the device configuration", Level.SEVERE),
    DUPLICATE_CONNECTION_TASK(2002, Keys.DUPLICATE_CONNECTION_TASK_KEY, "The partial connection task {0} is already used by connection task {1} on device {2} and therefore no other connection task with the same partial connection task can be created", Level.SEVERE),
    CONNECTION_TASK_INCOMPATIBLE_PARTIAL(2034, Keys.CONNECTION_TASK_INCOMPATIBLE_PARTIAL_KEY, "The type of the partial connection task of a connection task must be compatible. Expected ''{0}'' but got ''{1}''", Level.SEVERE),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION(2003, Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION_KEY, "A connection task must be created against a partial connection task (id={0}, configuration id={1}) from the same device configuration (id={2})", Level.SEVERE),
    CONNECTION_TASK_IS_ALREADY_OBSOLETE(2004, Keys.CONNECTION_TASK_IS_ALREADY_OBSOLETE_KEY, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}", Level.SEVERE),
    CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE(2005, Keys.CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE_KEY, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is currently being executed by communication server ''{2}''", Level.SEVERE),
    CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE(2006, Keys.CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE_KEY, "The connection task ''{0}'' on device {1} cannot be updated because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}", Level.SEVERE),
    DEFAULT_CONNECTION_TASK_IS_INUSE_AND_CANNOT_DELETE(2007, Keys.DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE_KEY, "The default connection task ''{0}'' on device {1} cannot be delete because it is still in use by communication tasks", Level.SEVERE),
    CONNECTION_TASK_INVALID_PROPERTY(2008, Keys.CONNECTION_TASK_INVALID_PROPERTY_KEY, "Invalid value", Level.SEVERE),
    CONNECTION_TASK_PROPERTY_NOT_IN_SPEC(2009, Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY, "ConnectionType '{0}' does not contain a specification for attribute '{1}'", Level.SEVERE),
    CONNECTION_TASK_PROPERTY_INVALID_VALUE(2010, Keys.CONNECTION_TASK_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of ConnectionType ''{2}''", Level.SEVERE),
    CONNECTION_TASK_REQUIRED_PROPERTY_MISSING(2035, Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY, "This value is required", Level.SEVERE),
    CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE(2011, Keys.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE_KEY, "An inbound communication port pool can only be used once on the same device", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED(2012, Keys.OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED_KEY, "An outbound connection task requires a connection strategy", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS(2013, Keys.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS_KEY, "", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED(2014, Keys.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED_KEY, "An outbound connection task with strategy to minimize connections requires execution scheduling specifications", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY(2015, Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY_KEY, "The offset of the next execution scheduling specifications should not extend its frequency", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW(2016, Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW_KEY, "The offset of the next execution scheduling specifications is not within the communication window", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW(2017, Keys.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW_KEY, "The offset of the next execution scheduling specifications within a week or month, once calculated back to a daily offset is not within the communication window", Level.SEVERE),
    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(2018, Keys.PHYSICAL_GATEWAY_STILL_IN_USE,"You can not delete device '{0}' because it is still used as a physical gateway for '{1}'", Level.SEVERE),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(2019, Keys.COMMUNICATION_GATEWAY_STILL_IN_USE,"You can not delete device '{0}' because it is still used as a communication gateway for '{1}'", Level.SEVERE),
    DEVICE_PROPERTY_INFO_TYPE_DOENST_EXIST(2020, Keys.INFOTYPE_DOESNT_EXIST,"The intotype for property value '{0}' does not exist.", Level.SEVERE),
    DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL(2021, Keys.PROPERTY_NOT_ON_DEVICE_PROTOCOL,"The property '{0}' is not defined by the device protocol '{1}' of device '{2}'", Level.SEVERE),
    PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED(2022, Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED_KEY, "The protocol dialect configuration properties are required to create device protocol dialect properties", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC(2023, Keys.DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY, "The protocol dialect ''{0}'' does not contain a specification for attribute ''{1}''", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE(2024, Keys.DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of device dialect protocol ''{2}''", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING(2025, Keys.DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY, "This is a required property", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED(2026, Keys.DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED_KEY, "Device protocol dialect properties need to be created against a device", Level.SEVERE),
    CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2027, Keys.CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not delete connection task {0} because it is not owned by device {1}", Level.SEVERE),
    COM_TASK_IS_OBSOLETE_AND_CAN_NOT_BE_UPDATED(2028, Keys.COM_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not update comtaskexecution {0} for device {1} because it is obsolete", Level.SEVERE),
    COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE(2029, Keys.COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it has already been made obsolete on {2}", Level.SEVERE),
    COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE(2030, Keys.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it is currently execution on comserver {2}", Level.SEVERE),
    COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2031, Keys.COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not delete comtaskexecution {0} because it is not owned by device {1}", Level.SEVERE),
    VETO_COM_TASK_ENABLEMENT_DELETION(2032, Keys.VETO_COM_TASK_ENABLEMENT_DELETION, "The device protocol pluggable class {0} is still used by the following device types: {1}", SEVERE),
    VETO_DEVICE_CONFIGURATION_DEACTIVATION(2033, Keys.VETO_DEVICE_CONFIGURATION_IN_USE_BY_DEVICES, "The device configuration {0} is still used by at least one device", SEVERE),
    CONNECTION_TASK_STATUS_INCOMPLETE(2036, Keys.CONNECTION_TASK_STATUS_INCOMPLETE, "Incomplete", INFO),
    CONNECTION_TASK_STATUS_ACTIVE(2037, Keys.CONNECTION_TASK_STATUS_ACTIVE, "Active", INFO),
    CONNECTION_TASK_STATUS_INACTIVE(2038, Keys.CONNECTION_TASK_STATUS_INACTIVE, "Inactive", INFO),
    VETO_COM_SCHEDULE_DELETION(2039, Keys.VETO_COM_SCHEDULE_DELETION, "The master schedule {0} is still used by at least one device", SEVERE),
    CANNOT_REMOVE_COM_SCHEDULE_BECAUSE_NOT_ON_DEVICE(2040, Keys.CANNOT_REMOVE_COM_SCHEDULE_BECAUSE_NOT_ON_DEVICE, "The master schedule {0} cannot be removed from device {1} because it was not configured on that device", SEVERE),
    DEVICE_CONFIGURATION_NOT_ACTIVE(2041, Keys.DEVICE_CONFIGURATION_NOT_ACTIVE, "The device configuration must be active", SEVERE),
    VETO_PARTIAL_CONNECTION_TASK_DELETION(2042, Keys.VETO_PARTIAL_CONNECTION_TASK_DELETION, "The partial connection task {0} of device configuration {1} is still used by at least one connection task on a device of that configuration", SEVERE),
    VETO_SECURITY_PROPERTY_SET_DELETION(2043, Keys.VETO_SECURITY_PROPERTY_SET_DELETION, "The security property set {0} of device configuration {1} cannot be deleted because at least one device of that configuration still has security property values for it", SEVERE),
    UNIQUE_ADDHOC_COMTASKS_PER_DEVICE(2044, Keys.UNIQUE_ADDHOC_COMTASKS_PER_DEVICE, "The communication task is not unique for device", SEVERE),
    DUPLICATE_COMTASK_SCHEDULING(2045, Keys.DUPLICATE_COMTASK_SCHEDULING, "One or more communication tasks in the communication schedule are already scheduled on the device with a master communication schedule", SEVERE),
    MISMATCH_COMTASK_SCHEDULE_WITH_DEVICE_CONFIGURATION(2046, Keys.MISMATCH_COMTASK_SCHEDULE_WITH_DEVICE_CONFIGURATION, "Mismatch between device configuration and the communication schedule", SEVERE),
    ;
    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

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
        return DeviceDataService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String NAME_REQUIRED_KEY = "X.name.required";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String MRID_REQUIRED_KEY = "mRIDRequired";
        public static final String DEVICE_TYPE_REQUIRED_KEY = "deviceTypeRequired";
        public static final String DEVICE_CONFIGURATION_REQUIRED_KEY = "deviceConfigurationRequired";
        public static final String COMPORT_TYPE_NOT_SUPPORTED_KEY = "comPortTypeOfComPortPoolMustBeSupportedByConnectionType";
        public static final String CONNECTION_TASK_DEVICE_REQUIRED_KEY = "connectionType.device.required";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED_KEY = "connectionType.partialConnectionTask.required";
        public static final String DUPLICATE_CONNECTION_TASK_KEY = "connectionType.duplicate";
        public static final String CONNECTION_TASK_INCOMPATIBLE_PARTIAL_KEY = "connectionType.incompatiblePartialConnectionTask";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION_KEY = "connectionType.partialConnectionTaskNotInConfiguration";
        public static final String CONNECTION_TASK_IS_ALREADY_OBSOLETE_KEY = "connectionTask.isAlreadyObsolete";
        public static final String CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE_KEY = "connectionTask.isExecutingAndCannotObsolete";
        public static final String DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE_KEY = "defaultConnectionTask.isInUseAndCannotObsolete";
        public static final String CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE_KEY = "connectionTask.isObsoleteAndCannotUpdate";
        public static final String CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED_KEY = "connectionMethod.pluggableClass.required";
        public static final String CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY = "connectionMethod.comPortPool.required";
        public static final String CONNECTION_TASK_INVALID_PROPERTY_KEY = "connectionTask.property.invalid";
        public static final String CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY = "connectionTaskPropertyXIsNotInConnectionTypeSpec";
        public static final String CONNECTION_TASK_PROPERTY_INVALID_VALUE_KEY = "connectionTaskProperty.value.invalid";
        public static final String CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY = "connectionTaskProperty.required";
        public static final String CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE_KEY = "inboundConnectionTask.comPortPool.uniquePerDevice";
        public static final String OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED_KEY = "outboundConnectionTask.strategy.required";
        public static final String OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS_KEY = "outboundConnectionTask.strategy.incompatibleWithSimultaneous";
        public static final String OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED_KEY = "outboundConnectionTask.nextExecutionSpecs.required";
        public static final String OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY_KEY = "outboundConnectionTask.nextExecutionSpecs.offsetBiggerThenFrequency";
        public static final String OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW_KEY = "outboundConnectionTask.nextExecutionSpecs.offsetNotWithinWindow";
        public static final String OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW_KEY = "outboundConnectionTask.nextExecutionSpecs.longOffsetNotWithinWindow";
        public static final String VALUE_IS_REQUIRED_KEY = "X.value.required";
        public static final String DUPLICATE_DEVICE_MRID = "deviceDuplicateMrid";
        public static final String GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY = "gateway.not.origin";
        public static final String PHYSICAL_GATEWAY_STILL_IN_USE = "device.delete.linked.physical.gateway";
        public static final String COMMUNICATION_GATEWAY_STILL_IN_USE = "device.delete.linked.communication.gateway";
        public static final String INFOTYPE_DOESNT_EXIST = "device.property.infotype.required";
        public static final String PROPERTY_NOT_ON_DEVICE_PROTOCOL = "not.deviceprotocol.property";
        public static final String CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "cannotDeleteIfNotFromDevice";
        public static final String PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED_KEY = "protocolDialectConfigurationProperties.required";
        public static final String DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED_KEY = "deviceProtocolDialectProperty.device.required";
        public static final String DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY = "deviceProtocolDialectPropertyXIsNotInSpec";
        public static final String DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY = "deviceProtocolDialectProperty.value.invalid";
        public static final String DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY = "deviceProtocolDialectProperty.required";
        public static final String DEVICE_IS_REQUIRED = "deviceIsRequired";
        public static final String COMTASK_IS_REQUIRED = "comTaskIsRequired";
        public static final String COMSCHEDULE_IS_REQUIRED = "comScheduleIsRequired";
        public static final String NEXTEXECUTIONSPEC_IS_REQUIRED = "nextExecutionSpecIsRequired";
        public static final String CONNECTIONTASK_IS_REQUIRED = "connectionTaskIsRequired";
        public static final String PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED = "protocolDialectConfigurationPropertiesAreRequired";
        public static final String COM_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE = "comTaskExecutionIsObsoleteAndCanNotBeUpdated";
        public static final String COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE = "comTaskExecutionAlreadyObsolete";
        public static final String COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE = "comTaskExecutionCannotObsoleteCurrentlyExecuting";
        public static final String COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "comTaskExecutionCannotDeleteNotFromDevice";
        public static final String VETO_COM_TASK_ENABLEMENT_DELETION = "comTaskExecution.comTaskEnablement.inUse";
        public static final String VETO_PARTIAL_CONNECTION_TASK_DELETION = "partialConnectionTask.inUse";
        public static final String VETO_SECURITY_PROPERTY_SET_DELETION = "securityPropertySet.inUse";
        public static final String VETO_DEVICE_CONFIGURATION_IN_USE_BY_DEVICES = "deviceConfiguration.inUse";
        public static final String CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT = "connectionTaskRequiredWhenNotUsingDefault";
        public static final String PRIORITY_NOT_IN_RANGE = "priorityNotInRange";
        public static final String UNIQUE_ADDHOC_COMTASKS_PER_DEVICE = "uniqueComTasksPerDevice";
        public static final String DUPLICATE_COMTASK_SCHEDULING = "duplicateComtaskScheduling";
        public static final String CONNECTION_TASK_STATUS_INCOMPLETE = "connectionTaskStatusIncomplete";
        public static final String CONNECTION_TASK_STATUS_ACTIVE = "connectionTaskStatusActive";
        public static final String CONNECTION_TASK_STATUS_INACTIVE = "connectionTaskStatusInActive";
        public static final String VETO_COM_SCHEDULE_DELETION = "comTaskExecution.comSchedule.inUse";
        public static final String CANNOT_REMOVE_COM_SCHEDULE_BECAUSE_NOT_ON_DEVICE = "cannotDeleteComScheduleFromDevice";
        public static final String DEVICE_CONFIGURATION_NOT_ACTIVE = "device.configuration.not.active";
        public static final String MISMATCH_COMTASK_SCHEDULE_WITH_DEVICE_CONFIGURATION = "mismatchComtaskScheduleWithDeviceConfiguration";

    }

}