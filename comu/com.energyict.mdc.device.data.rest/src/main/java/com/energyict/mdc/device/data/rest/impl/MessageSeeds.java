package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import com.energyict.mdc.protocol.api.ConnectionType;
import java.text.MessageFormat;
import java.util.logging.Level;
public enum MessageSeeds implements MessageSeed {


    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with mrId {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_PARTIAL_CONNECTION_TASK(16, "NoSuchPartialConnectionTask", "No such connection method on device config"),
    NO_SUCH_CONNECTION_METHOD(17, "NoSuchConnectionTask" , "Device {0} has no connection method {1}"),
    NO_SUCH_REGISTER(18, "NoSuchRegister" , "No register with id {0}"),
    NO_SUCH_COM_SCHEDULE(19, "NoSuchComSchedule" , "No communication schedule with id {0}"),
    DEVICE_VALIDATION_BULK_MSG(20, "DeviceValidationBulkMessage" , "This bulk operation for {0} schedule on {1} device is invalid"),
    NO_SUCH_READING(21, "NoSuchReading" , "Register {0} has no reading with id {1}"),
    INVALID_DATE(22, "InvalidDate", "Date should be less or equal to {0}"),
    NO_SUCH_LOAD_PROFILE_ON_DEVICE(23, "NoSuchLoadProfile", "Device {0} has no load profile {1}"),
    NO_SUCH_CHANNEL_ON_LOAD_PROFILE(30, "NoSuchChannel", "Load profile {0} has no channel {1}"),
    NO_CHANNELS_ON_REGISTER(72, "NoChannelsOnRegister", "Register {0} has no channels"),
    NO_SUCH_READING_ON_REGISTER(73, "NoSuchReadingOnRegister", "Register {0} has no reading with timestamp {1}"),
    NO_SUCH_LOG_BOOK_ON_DEVICE(24, "NoSuchLogBook", "Device {0} has no log book {1}"),
    CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE(25, "connectionTypeStrategy.notApplicable", "Not applicable"),
    UPDATE_URGENCY_NOT_ALLOWED(26,"urgencyUpdateNotAllowed" ,"Urgency update not allowed"),
    UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED(27,"updateDialectPropertiesNotAllowed" ,"Protocol dialect update not allowed"),
    UPDATE_CONNECTION_METHOD_NOT_ALLOWED(28,"updateConnectionMethodNotAllowed" ,"Connection method update not allowed"),
    RUN_COMTASK__NOT_ALLOWED(29,"runComTaskNotAllowed" ,"Running of this communication task is not allowed"),
    POWERDOWN(31, ProfileStatus.Flag.POWERDOWN.name(), "Power down"),
    POWERUP(32, ProfileStatus.Flag.POWERUP.name(), "Power up"),
    SHORTLONG(33, ProfileStatus.Flag.SHORTLONG.name(), "Short long"),
    WATCHDOGRESET(34, ProfileStatus.Flag.WATCHDOGRESET.name(), "Watchdog reset"),
    CONFIGURATIONCHANGE(45, ProfileStatus.Flag.CONFIGURATIONCHANGE.name(), "Configuration change"),
    CORRUPTED(46, ProfileStatus.Flag.CORRUPTED.name(), "Corrupted"),
    OVERFLOW(47, ProfileStatus.Flag.OVERFLOW.name(), "Overflow"),
    RESERVED1(48, ProfileStatus.Flag.RESERVED1.name(), "Reserved 1"),
    RESERVED4(49, ProfileStatus.Flag.RESERVED4.name(), "Reserved 4"),
    RESERVED5(50, ProfileStatus.Flag.RESERVED5.name(), "Reserved 5"),
    MISSING(51, ProfileStatus.Flag.MISSING.name(), "Missing"),
    SHORT(52, ProfileStatus.Flag.SHORT.name(), "Short"),
    LONG(53, ProfileStatus.Flag.LONG.name(), "Long"),
    OTHER(54, ProfileStatus.Flag.OTHER.name(), "Other"),
    REVERSERUN(55, ProfileStatus.Flag.REVERSERUN.name(), "Reverse run"),
    PHASEFAILURE(56, ProfileStatus.Flag.PHASEFAILURE.name(), "Phase failure"),
    BADTIME(57, ProfileStatus.Flag.BADTIME.name(), "Bad time"),
    DEVICE_ERROR(58, ProfileStatus.Flag.DEVICE_ERROR.name(), "Device error"),
    BATTERY_LOW(59, ProfileStatus.Flag.BATTERY_LOW.name(), "Battery low"),
    TEST(60, ProfileStatus.Flag.TEST.name(), "Test"),
    NULL_DATE(61, "NullDate", "Date must be filled in"),
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(62, "DeactivateValidationRuleSetNotPossible", "Deactivate of validation rule set {0} is currently not possible."),
    PENDING(63, "Pending", "Pending"),
    COMMAND_FAILED(64, "Failed", "Failed"),
    BUSY(65, "Busy", "Busy"),
    ON_HOLD(66, "OnHold", "On hold"),
    RETRYING(67, "Retrying", "Retrying"),
    NEVER_COMPLETED(68, "NeverCompleted", "Never completed"),
    WAITING(69, "Waiting", "Waiting"),
    DEFAULT(70, "Default", "Default"),
    DEFAULT_NOT_DEFINED(71, "DefaultNotDefined", "Default (not defined yet)"),
    SUCCESS(74, "Success", "Success"),
    BROKEN(75, "Broken", "Broken"),
    SETUP_ERROR(76, "SetupError", "Setup error"),
    FAILURE(77, "Failure", "Failure"),
    CONNECTION_ERROR(78, "ConnectionError", "Connection error"),
    CONFIGURATION_ERROR(79, "ConfigurationError", "Configuration error"),
    CONFIGURATION_WARNING(80, "ConfigurationWarning", "Configuration warning"),
    IO_ERROR(81, "IoError", "I/O error"),
    PROTOCOL_ERROR(82, "ProtocolError", "Protocol error"),
    OK(83, "OK", "Ok"),
    RESCHEDULED(84, "Rescheduled", "Rescheduled"),
    TIME_ERROR(85, "TimeError", "Time error"),
    UNEXPECTED_ERROR(86, "UnexpectedError", "Unexpected error"),
    INDIVIDUAL(87, "Individual", "Individual"),
    NO_SUCH_COM_SESSION_ON_CONNECTION_METHOD(88,"noSuchComSession" ,"No such communication session exists for this connection method"),
    INBOUND(89, ConnectionType.Direction.INBOUND.name(), "Inbound"),
    OUTBOUND(90, ConnectionType.Direction.OUTBOUND.name(), "Outbound"),
    NO_SUCH_COM_TASK(91, "NoSucComTaskOnDevice", "No such communication task exists for device ''{0}''"),
    COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE(92, "NoEnablementForDevice", "Communication task ''{0}'' is not enabled for device ''{1}''"),
    NO_SUCH_COM_TASK_EXEC_SESSION(93, "NoSuchComTaskExecSession", "The communication task logging could not be found"),
    DEVICEGROUPNAME_ALREADY_EXISTS(94, "deviceGroupNameAlreadyExists", "A devicegroup with name {0} already exists"),
    COMMAND_CANCELED(100, "CommandCancelled", "Cancelled"),
    COMMAND_CONFIRMED(101, "CommandConfirmed", "Confirmed"),
    COMMAND_IN_DOUBT(102, "CommandInDoubt", "In doubt"),
    COMMAND_PENDING(103, "CommandPending", "Pending"),
    COMMAND_SENT(104, "CommandSent", "Sent"),
    COMMAND_WAITING(105, "CommandWaiting", "Waiting"),
    NO_SUCH_USER(106, "NoSuchUser", "No such user"),
    NO_SUCH_MESSAGE_SPEC(107, "NoSuchMessageSpec", "No such device message specification"),
    NO_SUCH_MESSAGE(108, "NoSuchMessage", "No such device message exists on the device" )
    ;

    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DeviceApplication.COMPONENT_NAME;
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
        return format;
    }

    public String format(Thesaurus thesaurus, Object... args){
        if (thesaurus == null){
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }
    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}
