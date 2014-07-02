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
    LEGACY(100, "DDC.legacy.exception", "Coding: BusinessException or SQLException from legacy code that has not been ported to the jupiter ORM framework", Level.SEVERE),
    NAME_IS_REQUIRED(1000, Constants.NAME_REQUIRED_KEY, "The name is required", Level.SEVERE),
    CODING_RELATION_IS_ALREADY_OBSOLETE(1001, "DDC.relation.isAlreadyObsolete", "Cannot delete a property because the relation (of type ''{0}'') that holds it is already obsolete", Level.SEVERE),
    CODING_NO_PROPERTIES_EXPECTED(1002, "DDC.noAttributesExpected", "Was not expecting a value to be added for property ''{0}'' because the pluggable does not have any properties", Level.SEVERE),
    UNEXPECTED_RELATION_TRANSACTION_ERROR(1003, "DDC.unExpectedRelationTransactionError", "Unexpected problem occurred in the relation transaction framework", Level.SEVERE),
    COMPORT_TYPE_NOT_SUPPORTED(1004, Constants.COMPORT_TYPE_NOT_SUPPORTED_KEY, "The communication port type of the communication port pool must be supported by the connection type", Level.SEVERE),
    CONNECTION_METHOD_ALREADY_EXISTS(1005, "DDC.connectionMethod.duplicateNameX", "A connection method with name '{0}' already exists", Level.SEVERE),
    CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED(1006, Constants.CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED_KEY, "A connection method requires a connection type pluggable class", Level.SEVERE),
    CONNECTION_METHOD_COMPORT_POOL_REQUIRED(1007, Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY, "A connection method requires a communication port pool", Level.SEVERE),
    MRID_IS_REQUIRED(1008, Constants.MRID_REQUIRED_KEY, "The MRID is required", Level.SEVERE),
    DEVICE_TYPE_IS_REQUIRED(1009, Constants.DEVICE_TYPE_REQUIRED_KEY, "The device type is required", Level.SEVERE),
    DEVICE_CONFIGURATION_IS_REQUIRED(1010, Constants.DEVICE_CONFIGURATION_REQUIRED_KEY, "The device configuration is required", Level.SEVERE),
    DUPLICATE_DEVICE_MRID(1011, Constants.DUPLICATE_DEVICE_MRID, "The MRID is already used by another device", Level.SEVERE),
    CONNECTION_TASK_DEVICE_REQUIRED(2000, Constants.CONNECTION_TASK_DEVICE_REQUIRED_KEY, "A connection type should be linked to a device", Level.SEVERE),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED(2001, Constants.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED_KEY, "A connection type should be linked to a partial connection task from the device configuration", Level.SEVERE),
    DUPLICATE_CONNECTION_TASK(2002, Constants.DUPLICATE_CONNECTION_TASK_KEY, "The partial connection task {0} is already used by connection task {1} on device {2} and therefore no other connection task with the same partial connection task can be created", Level.SEVERE),
    CONNECTION_TASK_INCOMPATIBLE_PARTIAL(2034, Constants.CONNECTION_TASK_INCOMPATIBLE_PARTIAL_KEY, "The type of the partial connection task of a connection task must be compatible. Expected ''{0}'' but got ''{1}''", Level.SEVERE),
    CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION(2003, Constants.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION_KEY, "A connection task must be created against a partial connection task (id={0}, configuration id={1}) from the same device configuration (id={2})", Level.SEVERE),
    CONNECTION_TASK_IS_ALREADY_OBSOLETE(2004, Constants.CONNECTION_TASK_IS_ALREADY_OBSOLETE_KEY, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}", Level.SEVERE),
    CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE(2005, Constants.CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE_KEY, "The connection task ''{0}'' on device {1} cannot be made obsolete because it is currently being executed by communication server ''{2}''", Level.SEVERE),
    CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE(2006, Constants.CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE_KEY, "The connection task ''{0}'' on device {1} cannot be updated because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}", Level.SEVERE),
    DEFAULT_CONNECTION_TASK_IS_INUSE_AND_CANNOT_DELETE(2007, Constants.DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE_KEY, "The default connection task ''{0}'' on device {1} cannot be delete because it is still in use by communication tasks", Level.SEVERE),
    CONNECTION_TASK_INVALID_PROPERTY(2008, Constants.CONNECTION_TASK_INVALID_PROPERTY_KEY, "The connection task ''{0}'' on device {1} cannot be updated because it is already obsolete since {2,date,yyyy-MM-dd HH:mm:ss}", Level.SEVERE),
    CONNECTION_TASK_PROPERTY_NOT_IN_SPEC(2009, Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY, "ConnectionType '{0}' does not contain a specification for attribute '{1}'", Level.SEVERE),
    CONNECTION_TASK_PROPERTY_INVALID_VALUE(2010, Constants.CONNECTION_TASK_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of ConnectionType ''{2}''", Level.SEVERE),
    CONNECTION_TASK_REQUIRED_PROPERTY_MISSING(2035, Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY, "A value is missing for required attribute ''{1}'' of ConnectionType ''{2}''", Level.SEVERE),
    CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE(2011, Constants.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE_KEY, "An inbound communication port pool can only be used once on the same device", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED(2012, Constants.OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED_KEY, "An outbound connection task requires a connection strategy", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS(2013, Constants.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS_KEY, "", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED(2014, Constants.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED_KEY, "An outbound connection task with strategy to minimize connections requires execution scheduling specifications", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY(2015, Constants.OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY_KEY, "The offset of the next execution scheduling specifications should not extend its frequency", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW(2016, Constants.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW_KEY, "The offset of the next execution scheduling specifications is not within the communication window", Level.SEVERE),
    OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW(2017, Constants.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW_KEY, "The offset of the next execution scheduling specifications within a week or month, once calculated back to a daily offset is not within the communication window", Level.SEVERE),
    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(2018, Constants.PHYSICAL_GATEWAY_STILL_IN_USE,"You can not delete device '{0}' because it is still used as a physical gateway for '{1}'", Level.SEVERE),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(2019, Constants.COMMUNICATION_GATEWAY_STILL_IN_USE,"You can not delete device '{0}' because it is still used as a communication gateway for '{1}'", Level.SEVERE),
    DEVICE_PROPERTY_INFO_TYPE_DOENST_EXIST(2020, Constants.INFOTYPE_DOESNT_EXIST,"The intotype for property value '{0}' does not exist.", Level.SEVERE),
    DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL(2021, Constants.PROPERTY_NOT_ON_DEVICE_PROTOCOL,"The property '{0}' is not defined by the device protocol '{1}' of device '{2}'", Level.SEVERE),
    PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED(2022, Constants.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED_KEY, "The protocol dialect configuration properties are required to create device protocol dialect properties", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC(2023, Constants.DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY, "The protocol dialect ''{0}'' does not contain a specification for attribute ''{1}''", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE(2024, Constants.DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of device dialect protocol ''{2}''", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING(2025, Constants.DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY, "A value is missing for required attribute ''{0}'' of device dialect protocol''{1}''", Level.SEVERE),
    DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED(2026, Constants.DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED_KEY, "Device protocol dialect properties need to be created against a device", Level.SEVERE),
    CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2027, Constants.CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not delete connection task {0} because it is not owned by device {1}", Level.SEVERE),
    COM_TASK_IS_OBSOLETE_AND_CAN_NOT_BE_UPDATED(2028, Constants.COM_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not update comtaskexecution {0} for device {1} because it made obsolete", Level.SEVERE),
    COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE(2029, Constants.COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it has already been made obsolete on {2}", Level.SEVERE),
    COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE(2030, Constants.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it is currently execution on comserver {2}", Level.SEVERE),
    COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE(2031, Constants.COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE, "You can not delete comtaskexecution {0} because it is not owned by device {1}", Level.SEVERE),
    VETO_COM_TASK_ENABLEMENT_DELETION(2032, Constants.VETO_COM_TASK_ENABLEMENT_DELETION, "The device protocol pluggable class {0} is still used by the following device types: {1}", SEVERE),
    VETO_DEVICE_CONFIGURATION_DEACTIVATION(2033, Constants.VETO_DEVICE_CONFIGURATION_IN_USE_BY_DEVICES, "The device configuration {0} is still used by at least one device", SEVERE),
    CONNECTION_TASK_STATUS_INCOMPLETE(2036, Constants.CONNECTION_TASK_STATUS_INCOMPLETE, "Incomplete", INFO),
    CONNECTION_TASK_STATUS_ACTIVE(2037, Constants.CONNECTION_TASK_STATUS_ACTIVE, "Active", INFO),
    CONNECTION_TASK_STATUS_INACTIVE(2038, Constants.CONNECTION_TASK_STATUS_INACTIVE, "Inactive", INFO),
    ;
    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(DeviceDataService.COMPONENTNAME+".")) {
            return key.substring(DeviceDataService.COMPONENTNAME.length()+1);
        } else {
            return key;
        }
    }



    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getFullyQualifiedKey() {
        return DeviceDataService.COMPONENTNAME+"."+key;
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

    public static class Constants {
        public static final String NAME_REQUIRED_KEY = "DDC.X.name.required";
        public static final String MRID_REQUIRED_KEY = "DDC.mRIDRequired";
        public static final String DEVICE_TYPE_REQUIRED_KEY = "DDC.deviceTypeRequired";
        public static final String DEVICE_CONFIGURATION_REQUIRED_KEY = "DDC.deviceConfigurationRequired";
        public static final String COMPORT_TYPE_NOT_SUPPORTED_KEY = "DDC.comPortTypeOfComPortPoolMustBeSupportedByConnectionType";
        public static final String CONNECTION_TASK_DEVICE_REQUIRED_KEY = "DDC.connectionType.device.required";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED_KEY = "DDC.connectionType.partialConnectionTask.required";
        public static final String DUPLICATE_CONNECTION_TASK_KEY = "DDC.connectionType.duplicate";
        public static final String CONNECTION_TASK_INCOMPATIBLE_PARTIAL_KEY = "DDC.connectionType.incompatiblePartialConnectionTask";
        public static final String CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION_KEY = "DDC.connectionType.partialConnectionTaskNotInConfiguration";
        public static final String CONNECTION_TASK_IS_ALREADY_OBSOLETE_KEY = "DDC.connectionTask.isAlreadyObsolete";
        public static final String CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE_KEY = "DDC.connectionTask.isExecutingAndCannotObsolete";
        public static final String DEFAULT_CONNECTION_TASK_IS_IN_USE_AND_CANNOT_OBSOLETE_KEY = "DDC.defaultConnectionTask.isInUseAndCannotObsolete";
        public static final String CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE_KEY = "DDC.connectionTask.isObsoleteAndCannotUpdate";
        public static final String CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED_KEY = "DDC.connectionMethod.pluggableClass.required";
        public static final String CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY = "DDC.connectionMethod.comPortPool.required";
        public static final String CONNECTION_TASK_INVALID_PROPERTY_KEY = "DDC.connectionTask.property.invalid";
        public static final String CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY = "DDC.connectionTaskPropertyXIsNotInConnectionTypeSpec";
        public static final String CONNECTION_TASK_PROPERTY_INVALID_VALUE_KEY = "DDC.connectionTaskProperty.value.invalid";
        public static final String CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY = "DDC.connectionTaskProperty.required";
        public static final String CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE_KEY = "DDC.inboundConnectionTask.comPortPool.uniquePerDevice";
        public static final String OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED_KEY = "DDC.outboundConnectionTask.strategy.required";
        public static final String OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS_KEY = "DDC.outboundConnectionTask.strategy.incompatibleWithSimultaneous";
        public static final String OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED_KEY = "DDC.outboundConnectionTask.nextExecutionSpecs.required";
        public static final String OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY_KEY = "DDC.outboundConnectionTask.nextExecutionSpecs.offsetBiggerThenFrequency";
        public static final String OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW_KEY = "DDC.outboundConnectionTask.nextExecutionSpecs.offsetNotWithinWindow";
        public static final String OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW_KEY = "DDC.outboundConnectionTask.nextExecutionSpecs.longOffsetNotWithinWindow";
        public static final String VALUE_IS_REQUIRED_KEY = "DDC.X.value.required";
        public static final String DUPLICATE_DEVICE_MRID = "DDC.deviceDuplicateMrid";
        public static final String GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY = "DDC.gateway.not.origin";
        public static final String PHYSICAL_GATEWAY_STILL_IN_USE = "DDC.device.delete.linked.physical.gateway";
        public static final String COMMUNICATION_GATEWAY_STILL_IN_USE = "DDC.device.delete.linked.communication.gateway";
        public static final String INFOTYPE_DOESNT_EXIST = "DDC.device.property.infotype.required";
        public static final String PROPERTY_NOT_ON_DEVICE_PROTOCOL = "DDC.not.deviceprotocol.property";
        public static final String CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "DDC.cannotDeleteIfNotFromDevice";
        public static final String PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED_KEY = "DDC.protocolDialectConfigurationProperties.required";
        public static final String DEVICE_PROTOCOL_DIALECT_DEVICE_REQUIRED_KEY = "DDC.deviceProtocolDialectProperty.device.required";
        public static final String DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY = "DDC.deviceProtocolDialectPropertyXIsNotInSpec";
        public static final String DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY = "DDC.deviceProtocolDialectProperty.value.invalid";
        public static final String DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY = "DDC.deviceProtocolDialectProperty.required";
        public static final String DEVICE_IS_REQUIRED = "DDC.deviceIsRequired";
        public static final String COMTASK_IS_REQUIRED = "DDC.comTaskIsRequired";
        public static final String CONNECTIONTASK_IS_REQUIRED = "DDC.connectionTaskIsRequired";
        public static final String PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED = "DDC.protocolDialectConfigurationPropertiesAreRequired";
        public static final String COM_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE = "DDC.comTaskExecutionIsObsoleteAndCanNotBeUpdated";
        public static final String COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE = "DDC.comTaskExecutionAlreadyObsolete";
        public static final String COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE = "DDC.comTaskExecutionCannotObsoleteCurrentlyExecuting";
        public static final String COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE = "DDC.comTaskExecutionCannotDeleteNotFromDevice";
        public static final String VETO_COM_TASK_ENABLEMENT_DELETION = "DDC.comTaskExecution.comTaskEnablement.inUse";
        public static final String VETO_DEVICE_CONFIGURATION_IN_USE_BY_DEVICES = "DDC.deviceConfiguration.inUse";
        public static final String CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT = "DDC.connectionTaskRequiredWhenNotUsingDefault";
        public static final String PRIORITY_NOT_IN_RANGE = "DDC.priorityNotInRange";
        public static final String UNIQUE_COMTASKS_PER_DEVICE = "DDC.uniqueComTasksPerDevice";
        public static final String CONNECTION_TASK_STATUS_INCOMPLETE = "DDC.connectionTaskStatusIncomplete";
        public static final String CONNECTION_TASK_STATUS_ACTIVE = "DDC.connectionTaskStatusActive";
        public static final String CONNECTION_TASK_STATUS_INACTIVE = "DDC.connectionTaskStatusInActive";
    }

}