package com.energyict.mdc.engine.impl.commands;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.EngineService;

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
    LOAD_PROFILE_CHANNEL_MISSING(5054, "loadprofileobiscodeXmissingchannel", "Channel with OBIS code ''{0}'' not found for load profile ({1})"),
    LOAD_PROFILE_NO_CHANNEL_MATCH(5055, "loadprofileobiscodeXmissingchannelOtherMatch", "No channel match for Load profile with OBIS code ''{0}'' "),
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
    CONFIG_SERIAL_NUMBER_MISMATCH(5034, "serialNumberMismatch", "Serial number mismatch; meter has {0}, while {1} is configured in the system"),
    UNSUPPORTED_DISCOVERY_RESULT_TYPE(5035, "unsupportedDiscoveryResultType", "Discovery Result type {0} is unknown, not supported or no longer supported"),
    MAXIMUM_TIME_DIFFERENCE_EXCEEDED(5036, "maxTimeDiffExceeded", "Time difference exceeds the configured maximum: The time difference ({0}) is larger than the configured allowed maximum ({1})"),
    INCORRECT_NUMBER_OF_COMTASKS(5037, "incorrectNbrOfComTasks", "Incorrect number of PreparedComTaskExecutions. Expected {0} but got {1}"),
    SESSION_FOR_COMTASK_MISSING(5038, "comTaskSessionMissing", "Expected session for ComTask {0} was not found in ComSessionShadow"),
    CONNECTION_FAILURE(5039, "connectionFailure", "Failure to connect to device"),
    UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION(5040, "unexpectedInboundCommException", "Unexpected inbound communication exception, see stacktrace for more details"),
    MODEM_COULD_NOT_ESTABLISH_CONNECTION(5041, "modemConnectError", "Failed to establish a connection between modem on COM port {0} and its receiver within timeout [{1} ms]"),
    UNEXPECTED_IO_EXCEPTION(5042, "unexpectedIOException", "Exception occurred while communication with a device"),
    COMMUNICATION_FAILURE(5043, "communicationFailure", "Communication with device failed", Level.SEVERE),
    NO_NEW_LOAD_PROFILE_DATA_COLLECTED(5044, "noLoadProfileDataCollected", "No new loadprofile data was collected for {0} since {1}", Level.WARNING),
    TIME_DIFFERENCE_LARGER_THAN_MAX_DEFINED(5045, "timediffXlargerthanmaxdefined", "Time difference is larger ({0}) than the maximum defined on the ComTask, setting the time will not be performed"),
    TIME_DIFFERENCE_BELOW_THAN_MIN_DEFINED(5046, "timediffXbelowthanmindefined", "Time difference of {0} is smaller that the configured minimum"),
    INTERVALS_MARKED_AS_BAD_TIME(5047, "intervalsMarkedAsBadTime", "Load profile intervals will be marked as bad time: The time difference ({0}) exceeds the configured allowed maximum ({1})"),
    NOT_POSSIBLE_TO_VERIFY_SERIALNUMBER(5048, "notPossibleToVerifySerialNumber", "It is not possible to verify the serialnumber of device {0} with protocol {1}"),
    DEVICEPROTOCOL_PROTOCOL_ISSUE(5049, "deviceprotocol.protocol.issue", "An error occurred during the execution of the protocol: {0}"),
    DEVICEPROTOCOL_LEGACY_ISSUE(5050, "deviceprotocol.legacy.issue", "An error occurred during the execution of a legacy protocol, see following stacktrace: {0}"),
    LOADPROFILE_NOT_SUPPORTED(5051, "loadProfileXnotsupported", "Load profile with OBIS code '{0}' is not supported by the device"),
    MESSAGE_NO_LONGER_VALID(5052, "messageNoLongerValid", "The message is no longer valid (see message below):"),
    CALENDAR_NO_LONGER_ALLOWED(5053, "calendarNoLongerAllowed", "The calendar(s) ''{0}'' is or are no longer allowed by device type ''{1}''"),
    NOT_EXECUTED_DUE_TO_CONNECTION_ERROR(5056, "notExecutedDueToConnectionError", "Communication task will be rescheduled due to connection errors in previous task"),
    NOT_EXECUTED_DUE_TO_INIT_ERROR(5057, "notExecutedDueToInitError", "Communication task will be rescheduled due to an initialization error"),
    NOT_EXECUTED_DUE_TO_OTHER_COMTASK_EXECUTION_ERROR(5058, "notExecutedDueToOtherComTaskExecutionError", "Communication task will be rescheduled due to an error in the previous communication task"),
    NOT_EXECUTED_DUE_TO_CONNECTION_SETUP_ERROR(5059, "notExecutedDueToConnectionSetupError", "Communication task will be rescheduled due to a connection setup failure"),
    NOT_EXECUTED_DUE_TO_GENERAL_SETUP_ERROR(5060, "notExecutedDueToGeneralSetupError", "Communication task will be rescheduled due to a general error during the setup of the tasks. No connection to the device was made."),
    SOMETHING_UNEXPECTED_HAPPENED(5061, "somethingUnexpectedHappened", "Some unexpected error occurred"),
    COMMAND_FAILED_DUE_TO_CONNECTION_RELATED_ISSUE(5062, "commandFailedDueToConnectionRelatedIssue", "Communication task failed due to connection related error: {0}"),
    NOT_EXECUTED_DUE_TO_BASIC_CHECK_FAILURE(5063, "notExecutedDueToBasicCheckFailure", "Communication task will be rescheduled due to the failure of the BasicCheck task"),
    LOAD_PROFILE_CONFIGURATION_MISMATCH(5064, "loadProfileConfigDoesNotMatchCollectedData", "Collected load profile data for {0} doesn''t match the configured load profile interval ''{1}''"),
    MAC_CHECK_FAILURE(5065, "macCheckFailure", "Failed to execute command due to message authentication check failure.")
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