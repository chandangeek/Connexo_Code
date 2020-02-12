/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by bvn on 7/13/15.
 */
public enum MessageSeeds implements MessageSeed {
    NO_SUCH_DEVICE_MESSAGE(1001, "NoSuchDeviceMessage", "No such device message."),
    NO_SUCH_USAGE_POINT(1002, "NoSuchUsagePoint", "No such usage point."),
    NO_CURRENT_METER_ACTIVATION(1003, "NoCurrentMeterActivation", "The requested meter doesn''t seem to be active at the moment (no current meter activation)."),
    NO_METER_IN_ACTIVATION(1004, "NoMeterInActivation", "The meter activation doesn''t seem to contain a meter."),
    NO_DEVICE_FOR_METER(1005, "NoDeviceForMeter", "The meter activation of the usage point contains a meter, but the device counterpoint with MRID {0} couldn''t be found."),
    NO_COMTASK_FOR_COMMAND(1006, "NoComTaskForCommand", "A comtask to execute the device messages couldn''t be located."),
    UNKNOWN_STATUS(1007, "UnknownStatus", "The requested contactor status isn''t supported at this time."),
    UNKNOWN_UNIT_CODE(1008, "UnknownUnitCode", "The requested load limit unit isn''t supported at this time."),
    INCOMPLETE_LOADLIMIT(1009, "IncompleteLoadLimit", "Received incomplete load limit - please make sure to specify both the limit and the unit."),
    TOLERANCE_WITHOUT_LOAD_LIMIT(1010, "ToleranceWithoutLoadLimit", "Received incomplete load limit - load tolerance can only be used in combination with load limitation."),
    NO_COMTASK_FOR_STATUS_INFORMATION(1011, "NoComTaskForStatusInformation", "A comtask to verify the status information couldn''t be located."),
    NO_SUCH_DEVICE(1012, "NoSuchDevice", "No device with MRID {0}."),
    INCOMPLETE_CONTACTOR_INFO(1013, "IncompleteContactorInfo", "Received incomplete request - please make sure to specify contactor status and/or load limit parameters."),
    NO_HEAD_END_INTERFACE(1013, "NoHeadEndInterface", "Couldn''t find the head-end interface for end device with MRID {0}."),
    COMMAND_ARGUMENT_SPEC_NOT_FOUND(1014, "CommandArgumentSpecNotFound", "Couldn''t find the command argument spec {0} for command {1}."),
    COULD_NOT_FIND_SERVICE_CALL_TYPE(1015, "CouldNotFindServiceCallType", "Couldn''t find service call type {0} having version {1}."),
    COULD_NOT_FIND_SERVICE_CALL(1017, "CouldNotFindServiceCall", "Couldn''t find service call with ID {0}."),
    COULD_NOT_FIND_DESTINATION_SPEC(1018, "CouldNotFindDestinationSpec", "Couldn''t find destination spec with name {0}."),
    CALL_BACK_URI_NOT_SPECIFIED(1019, "CallBackURINotSpecified", "Not possible to send back the response, as the callback uri wasn''t specified."),
    FIELD_TOO_LONG(1020, Keys.FIELD_TOO_LONG, "Field mustn''t exceed {max} characters."),
    ;


    private final int number;
    private final String defaultFormat;
    private final String key;
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
    public String getModule() {
        return PrepaymentApplication.COMPONENT_NAME;
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

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
    }
}
