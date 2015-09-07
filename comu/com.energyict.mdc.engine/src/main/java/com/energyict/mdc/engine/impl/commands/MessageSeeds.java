package com.energyict.mdc.engine.impl.commands;

import com.energyict.mdc.engine.EngineService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-21 (14:51)
 */
public enum MessageSeeds implements MessageSeed {
    UNSUPPORTED_LOAD_PROFILE(5000, "loadProfile.notSupported", "Loadprofile ''{0}'' is not supported by the device"),
    CHANNEL_UNIT_MISMATCH(5001, "channel.unit.mismatch", "Channel unit mismatch: load profile in the meter with OBIS code ''{0}'' has a channel ({1}) with the unit ''{2}'', whilst the configured unit for that channel is ''{3}''"),
    LOAD_PROFILE_INTERVAL_MISMATCH(5002, "loadprofile.interval.mismatch", "Load profile interval mismatch; load profile with OBIS code ''{0}'' has a {1} second(s) interval on the device, while {2} second(s) is configured"),
    LOAD_PROFILE_NUMBER_OF_CHANNELS_MISMATCH(5003, "loadprofile.nbrOfChannels.mismatch", "Number of channels mismatch; load profile with OBIS code ''{0}'' has {1} channel(s) on the device, while there are {2} channel(s) configured"),
    COLLECTED_DEVICE_TOPOLOGY_FOR_UN_KNOWN_DEVICE(5004, "collectedDeviceTopologyForUnKnownDevice", "The collected topology is for an unknown device ''{0}''"),
    COLLECTED_DEVICE_CACHE_FOR_UNKNOWN_DEVICE(5005, "collectedDeviceCacheForUnknownDevice", "Could not store the collected device cache: device '{0}'  does not exist!"),
    SERIALS_REMOVED_FROM_TOPOLOGY(5006, "serialsRemovedFromTopology", "The following devices are removed from the topology: {0}"),
    SERIALS_ADDED_TO_TOPOLOGY(5007, "serialsAddedToTopology", "The following devices are added to the topology: {0}"),
    UNKNOWN_SERIALS_ADDED_TO_TOPOLOGY(5008, "unknownSerialsAddedToTopology", "The following unknown devices were found in the topology: ''{0}''"),
    UNKNOWN_DEVICE_LOAD_PROFILE(5009, "unknownDeviceLoadProfileCollected", "Could not store the collected device load profile: load profile '{0}' does not exist!"),
    UNKNOWN_DEVICE_LOG_BOOK(5010, "unknownDeviceLogBookCollected", "Could not store the collected device logbook: logbook '{0}' does not exist!"),
    UNKNOWN_DEVICE_REGISTER(5011, "unknownDeviceRegisterCollected", "Could not store the collected device register: register '{0}' does not exist!"),
    UNKNOWN_DEVICE_MESSAGE(5012, "unknownDeviceMessageCollected", "Could not store the collected device message: message '{0}' does not exist!"),
    PROPERTY_VALIDATION_FAILED(5013, "propertyValidationFailed", "The validation of property ''{0}'' with value ''{1}'' failed"),
    DEVICE_CACHE_SERIALIZATION(5014, Keys.DEVICE_CACHE_NOT_SERIALIZABLE, "The device cache ''{0}'' could not be serialized"),
    DEVICE_CACHE_DESERIALIZATION(5015, Keys.DEVICE_CACHE_NOT_DESERIALIZABLE, "The device cache ''{0}'' could not be deserialized"),
    NO_RESOURCES_ACQUIRED(5016, "noResourcesAcquired", "Client code ignored previous failure to allocate device command execution resources"),
    LOGGER_FACTORY_REQUIRES_INTERFACE(5017, "loggerFactoryRequiresInterface", "Can only produce loggers for interface classes {0}"),
    LOGGER_FACTORY_SUPPORTS_ONLY_ONE_THROWABLE_PARAMETER(5018, "loggerFactorySupportOnly1ThrowableParameter", "Only one Throwable message parameter supported but method {0} has multiple"),
    FW_DISCOVERED_NEW_GHOST(5019, Keys.FW_DISCOVERED_NEW_GHOST, "Discovered a new ghost firmware version ''{0}''"),
    FW_DISCOVERED_EXISTING_GHOST(5020, Keys.FW_DISCOVERED_EXISTING_GHOST, "Discovered an existing ghost firmware version ''{0}''", Level.INFO),
    FW_DISCOVERED_DEPRECATE(5021, Keys.FW_DISCOVERED_DEPRECATE, "Discovered a deprecate firmware version", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_GHOST(5022, Keys.FW_DISCOVERED_EMPTY_WAS_GHOST, "Discovered an EMPTY firmware version, while it previously was a ghost version ''{0}''", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_TEST(5023, Keys.FW_DISCOVERED_EMPTY_WAS_TEST, "Discovered an EMPTY firmware version, while it previously was a test version ''{0}''", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_FINAL(5024, Keys.FW_DISCOVERED_EMPTY_WAS_FINAL, "Discovered an EMPTY firmware version, while it previously was a final version ''{0}''", Level.INFO),
    FW_DISCOVERED_EMPTY_WAS_DEPRECATE(5025, Keys.FW_DISCOVERED_EMPTY_WAS_DEPRECATE, "Discovered an EMPTY firmware version, while it previously was a deprecate version ''{0}''", Level.INFO),
    FW_DISCOVERED_NEW_GHOST_WAS_FINAL(5026, Keys.FW_DISCOVERED_NEW_GHOST_WAS_FINAL, "Discovered a new ghost firmware version ''{0}'', while it previously was a final version ''{1}''", Level.INFO),
    FW_DISCOVERED_EXISTING_GHOST_WAS_FINAL(5027, Keys.FW_DISCOVERED_EXISTING_GHOST_WAS_FINAL, "Discovered an existing ghost firmware version ''{0}'', while it previously was a final version ''{1}''", Level.INFO),
    FW_DISCOVERED_DEPRECATE_WAS_FINAL(5028, Keys.FW_DISCOVERED_DEPRECATE_WAS_FINAL, "Discovered a deprecate firmware version ''{0}'', while it previously was a final version ''{1}'''", Level.INFO),
    FW_UNKNOWN(5029, "DDC.device.firmware.unknown", "Unknown firmware version transition...", Level.INFO),
    INBOUND_DATA_STORAGE_FAILURE(5030, "MDC.inbound.data.storage.failure", "Failed to store the data for inbound communication"),
    INBOUND_DATA_RESPONSE_FAILURE(5031, "MDC.inbound.data.response.failure", "Failed to store to provide a proper result to the device"),
    INBOUND_DATA_PROCESSOR_ERROR(5032, "MDC.inbound.data.processor.error", "Failed to execute the inbound device storage commands"),
    INBOUND_DUPLICATE_SERIALNUMBER_FAILURE(5033, "MDC.inbound.data.duplicate.serialnumber", "Failed to process the inbound request because multiple devices were found with the same serialnumber ''{0}''"),
    CONFIG_SERIAL_NUMBER_MISMATCH(5034, "serialNumberMismatch", "SerialNumber mismatch; meter has {0}, while {1} is configured in EIServer"),
    UNSUPPORTED_DISCOVERY_RESULT_TYPE(5035, "unsupportedDiscoveryResultType", "Discovery Result type {0} is unknown, not supported or no longer supported"),
    MAXIMUM_TIME_DIFFERENCE_EXCEEDED(5036, "maxTimeDiffExceeded", "Time difference exceeds the configured maximum\\: The time difference ({0}) is larger than the configured allowed maximum ({1})"),
    INCORRECT_NUMBER_OF_COMTASKS(5037, "incorrectNbrOfComTasks", "Incorrect number of PreparedComTaskExecutions. Expected {0} but got {1}"),
    SESSION_FOR_COMTASK_MISSING(5038, "comTaskSessionMissing", "Expected session for ComTask {0} was not found in ComSessionShadow"),
    CONNECTION_FAILURE(5039, "connectionFailure", "Failure to connect to device"),
    UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION(5040, "unexpectedInboundCommException", "Unexpected inbound communication exception, see stacktrace for more details"),
    MODEM_COULD_NOT_ESTABLISH_CONNECTION(5041, "modemConnectError", "Failed to establish a connection between modem on COM port {0} and its receiver within timeout [{1} ms]"),
    UNEXPECTED_IO_EXCEPTION(5042, "unexpectedIOException", "Exception occurred while communication with a device"),
    COMMUNICATION_FAILURE(5043, "communicationFailure", "Communication with device failed", Level.SEVERE),
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
        return this.level;
    }

    @Override
    public String getModule() {
        return EngineService.COMPONENTNAME;
    }

    public static class Keys {
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