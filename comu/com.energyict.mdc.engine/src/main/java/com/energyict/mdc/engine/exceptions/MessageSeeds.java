package com.energyict.mdc.engine.exceptions;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.EngineService;

import java.util.logging.Level;

/**
 * Defines all the {@link MessageSeed}s of the master data module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:49)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    DEVICE_CACHE_SERIALIZATION(101, Keys.DEVICE_CACHE_NOT_SERIALIZABLE, "The device cache ''{0}'' could not be serialized", Level.SEVERE),
    DEVICE_CACHE_DESERIALIZATION(102, Keys.DEVICE_CACHE_NOT_DESERIALIZABLE, "The device cache ''{0}'' could not be deserialized", Level.SEVERE),
    NO_RESOURCES_ACQUIRED(103, "noResourcesAcquired", "Client code ignored previous failure to allocate device command execution resources", Level.SEVERE),
    COMMAND_NOT_UNIQUE(104, "commandNotUnique", "There is already a {0} command in the current Root", Level.SEVERE),
    ILLEGAL_COMMAND(105, "illegalCommand", "The command {0} is not allowed for {1}", Level.SEVERE),
    MBEAN_OBJECT_FORMAT(106, "mbeanObjectFormat", "MalformedObjectNameException for ComServer {0}", Level.SEVERE),
    COMPOSITE_TYPE_CREATION(107, "compositeTypeCreation", "CompositeType creation failed for class {0}", Level.SEVERE),
    COMPOSITE_DATA_CREATION(108, "compositeDataCreation", "CompositeDataSupport creation failed for class {0}", Level.SEVERE),
    UNKNOWN_COMPOSITE_DATA_ITEM(109, "unknownCompositeDataItem", "Unknown composite data item {1} on class {0}", Level.SEVERE),
    UNEXPECTED_SQL_ERROR(110, "unexpectedSqlError", "Unexpected SQL exception\\: {0}", Level.SEVERE),
    METHOD_ARGUMENT_CAN_NOT_BE_NULL(111, "methodArgumentCannotBeNull", "A null value for the argument {2} of method {1} of class {0} is NOT supported", Level.SEVERE),
    LOGGER_FACTORY_REQUIRES_INTERFACE(112, "loggerFactoryRequiresInterface", "Can only produce loggers for interface classes {0}", Level.SEVERE),
    LOGGER_FACTORY_SUPPORTS_ONLY_ONE_THROWABLE_PARAMETER(113, "loggerFactorySupportOnly1ThrowableParameter", "Only one Throwable message parameter supported but method {0} has multiple", Level.SEVERE),
    VALIDATION_FAILED(114, "validationFailed", "Validation for attribute {1} of class {0} has previously failed and is now causing the following exception", Level.SEVERE),
    UNRECOGNIZED_ENUM_VALUE(115, "unrecognizedEnumValue", "No value found for ordinal {1} of enumeration class {0}", Level.SEVERE),
    CONFIG_SERIAL_NUMBER_MISMATCH(116, "serialNumberMismatch", "SerialNumber mismatch; meter has {0}, while {1} is configured in EIServer", Level.SEVERE),
    UNSUPPORTED_DISCOVERY_RESULT_TYPE(117, "unsupportedDiscoveryResultType", "Discovery Result type {0} is unknown, not supported or no longer supported", Level.SEVERE),
    MAXIMUM_TIME_DIFFERENCE_EXCEEDED(118, "maxTimeDiffExceeded", "Time difference exceeds the configured maximum\\: The time difference ({0}) is larger than the configured allowed maximum ({1})", Level.SEVERE),
    INCORRECT_NUMBER_OF_COMTASKS(119, "incorrectNbrOfComTasks", "Incorrect number of PreparedComTaskExecutions. Expected {0} but got {1}", Level.SEVERE),
    SESSION_FOR_COMTASK_MISSING(120, "comTaskSessionMissing", "Expected session for ComTask {0} was not found in ComSessionShadow", Level.SEVERE),
    CONNECTION_FAILURE(122, "connectionFailure", "Failure to connect to device", Level.SEVERE),
    UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION(123, "unexpectedInboundCommException", "Unexpected inbound communication exception, see stacktrace for more details", Level.SEVERE),
    UNEXPECTED_IO_EXCEPTION(124, "unexpectedIOException", "Exception occurred while communication with a device", Level.SEVERE),
    MODEM_COULD_NOT_ESTABLISH_CONNECTION(125, "modemConnectError", "Failed to establish a connection between modem on COM port {0} and its receiver within timeout [{1} ms]", Level.SEVERE),
    PRETTY_PRINT_TIMEDURATION_YEAR_SINGULAR(126, "PrettyPrintTimeDuration.year.singular", "{0} year", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_YEAR_PLURAL(127, "PrettyPrintTimeDuration.year.plural", "{0} years", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_MONTH_SINGULAR(128, "PrettyPrintTimeDuration.month.singular", "{0} month", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_MONTH_PLURAL(129, "PrettyPrintTimeDuration.month.plural", "{0} months", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_DAY_SINGULAR(130, "PrettyPrintTimeDuration.day.singular", "{0} day", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_DAY_PLURAL(131, "PrettyPrintTimeDuration.day.plural", "{0} days", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_HOUR_SINGULAR(132, "PrettyPrintTimeDuration.hour.singular", "{0} hour", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_HOUR_PLURAL(133, "PrettyPrintTimeDuration.hour.plural", "{0} hours", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_MINUTE_SINGULAR(134, "PrettyPrintTimeDuration.minute.singular", "{0} minute", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_MINUTE_PLURAL(135, "PrettyPrintTimeDuration.minute.plural", "{0} minutes", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_SECOND_SINGULAR(136, "PrettyPrintTimeDuration.second.singular", "{0} second", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_SECOND_PLURAL(137, "PrettyPrintTimeDuration.second.plural", "{0} seconds", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_SEPARATOR(138, "PrettyPrintTimeDuration.separator", ", ", Level.INFO),
    PRETTY_PRINT_TIMEDURATION_LAST_SEPARATOR(139, "PrettyPrintTimeDuration.lastSeparator", " and ", Level.INFO),
    DUPLICATE_FOUND(140, "duplicateFound", "A duplicate ''{0}'' was found when a unique result was expected for ''{1}''", Level.SEVERE),
    COMTASK_NOT_ENABLED_ON_CONFIGURATION(146, "comTaskNotEnabled", "The communication task ''{0}'' is not enabled for execution on devices of configuration ''{1}''", Level.SEVERE),

    FW_DISCOVERED_NEW_GHOST(147, Keys.FW_DISCOVERED_NEW_GHOST, "Discovered a new ghost firmware version ''{0}''", Level.SEVERE),
    FW_DISCOVERED_EXISTING_GHOST(148, Keys.FW_DISCOVERED_EXISTING_GHOST, "Discovered an existing ghost firmware version ''{0}''", Level.INFO),
    FW_DISCOVERED_DEPRECATE(149, Keys.FW_DISCOVERED_DEPRECATE, "Discovered a deprecate firmware version", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_GHOST(150, Keys.FW_DISCOVERED_EMPTY_WAS_GHOST, "Discovered an EMPTY firmware version, while it previously was a ghost version ''{0}''", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_TEST(151, Keys.FW_DISCOVERED_EMPTY_WAS_TEST, "Discovered an EMPTY firmware version, while it previously was a test version ''{0}''", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_FINAL(152, Keys.FW_DISCOVERED_EMPTY_WAS_FINAL, "Discovered an EMPTY firmware version, while it previously was a final version ''{0}''", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_DEPRECATE(153, Keys.FW_DISCOVERED_EMPTY_WAS_DEPRECATE, "Discovered an EMPTY firmware version, while it previously was a deprecate version ''{0}''", Level.INFO),
    FW_DISCOVERED_NEW_GHOST_WAS_FINAL(154, Keys.FW_DISCOVERED_NEW_GHOST_WAS_FINAL, "Discovered a new ghost firmware version ''{0}'', while it previously was a final version ''{1}''", Level.INFO),
    FW_DISCOVERED_EXISTING_GHOST_WAS_FINAL(155, Keys.FW_DISCOVERED_EXISTING_GHOST_WAS_FINAL, "Discovered an existing ghost firmware version ''{0}'', while it previously was a final version ''{1}''", Level.INFO),
    FW_DISCOVERED_DEPRECATE_WAS_FINAL(156, Keys.FW_DISCOVERED_DEPRECATE_WAS_FINAL, "Discovered a deprecate firmware version ''{0}'', while it previously was a final version ''{1}'''", Level.INFO),
    FW_UNKNOWN(157, "DDC.device.firmware.unknown", "Unknown firmware version transition...", Level.INFO),

    INBOUND_DATA_STORAGE_FAILURE(158, "MDC.inbound.data.storage.failure", "Failed to store the data for inbound communication", Level.SEVERE),
    INBOUND_DATA_RESPONSE_FAILURE(159, "MDC.inbound.data.response.failure", "Failed to store to provide a proper result to the device", Level.SEVERE),
    INBOUND_DATA_PROCESSOR_ERROR(160, "MDC.inbound.data.processor.error", "Failed to execute the inbound device storage commands", Level.SEVERE),
    INBOUND_DUPLICATE_SERIALNUMBER_FAILURE(161, "MDC.inbound.data.duplicate.serialnumber", "Failed to process the inbound request because multiple devices were found with the same serialnumber ''{0}''", Level.SEVERE),
    COMMUNICATION_FAILURE(162, "communicationFailure", "Communication with device failed", Level.SEVERE),
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
        return EngineService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String DEVICE_IS_REQUIRED_FOR_CACHE = "DDC.device.required";
        public static final String DEVICE_CACHE_NOT_SERIALIZABLE = "DDC.device.cache.not.serializable";
        public static final String DEVICE_CACHE_NOT_DESERIALIZABLE = "DDC.device.cache.not.deserializable";
        public static final String FW_DISCOVERED_NEW_GHOST = "DDC.device.discovered.new.ghost.firmware";
        public static final String FW_DISCOVERED_EXISTING_GHOST = "DDC.device.discovered.existing.ghost.firmware";
        public static final String FW_DISCOVERED_DEPRECATE = "DDC.device.discovered.deprecate.firmware";
        public static final String FW_DISCOVERED_EMPTY_WAS_GHOST = "DDC.device.discovered.empty.was.ghost";
        public static final String FW_DISCOVERED_EMPTY_WAS_TEST = "DDC.device.discovered.empty.was.test";
        public static final String FW_DISCOVERED_EMPTY_WAS_FINAL = "DDC.device.discovered.empty.was.final";
        public static final String FW_DISCOVERED_EMPTY_WAS_DEPRECATE = "DDC.device.discovered.empty.was.deprecate";
        public static final String FW_DISCOVERED_NEW_GHOST_WAS_FINAL = "DDC.device.discovered.new.ghost.was.final";
        public static final String FW_DISCOVERED_EXISTING_GHOST_WAS_FINAL = "DDC.device.discovered.existing.ghost.was.final";
        public static final String FW_DISCOVERED_DEPRECATE_WAS_FINAL = "DDC.device.discovered.deprecate.was.final";
    }

}
