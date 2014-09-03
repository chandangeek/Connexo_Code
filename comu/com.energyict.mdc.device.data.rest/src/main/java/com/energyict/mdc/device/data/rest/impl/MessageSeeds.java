package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.configuration.rest.impl.DeviceConfigurationApplication;

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
    NO_SUCH_LOG_BOOK_ON_DEVICE(24, "NoSuchLogBook", "Device {0} has no log book {1}"),
    
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
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(62, "DeactivateValidationRuleSetNotPossible", "Deactivate of validation rule set {0} is currently not possible.")

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
        return DeviceConfigurationApplication.COMPONENT_NAME;
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
