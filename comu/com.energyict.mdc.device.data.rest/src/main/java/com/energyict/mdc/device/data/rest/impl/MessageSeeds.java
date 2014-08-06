package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ProcessStatus;
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
    CONFIRMED(24, ProcessStatus.Flag.CONFIRMED.name(), "Confirmed"),
    EDITED(25, ProcessStatus.Flag.EDITED.name(), "Edited"),
    ESTIMATED(26, ProcessStatus.Flag.ESTIMATED.name(), "Estimated"),
    QUALITY(27, ProcessStatus.Flag.QUALITY.name(), "Quality"),
    SUSPECT(28, ProcessStatus.Flag.SUSPECT.name(), "Suspect"),
    WARNING(29, ProcessStatus.Flag.WARNING.name(), "Warning"),

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

    public String formate(Thesaurus thesaurus, Object... args){
        if (thesaurus == null){
            throw new IllegalArgumentException("Thesaurus cant't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }
    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}
