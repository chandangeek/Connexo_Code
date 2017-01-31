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
    NO_SUCH_DEVICE_MESSAGE(1001, "No such device message"),
    NO_SUCH_USAGE_POINT(1002, "No such usage point"),
    NO_CURRENT_METER_ACTIVATION(1003, "The requested meter does not seem to be active at the moment (no current meter activation)"),
    NO_METER_IN_ACTIVATION(1004, "The meter activation does not seem to contain a meter"),
    NO_DEVICE_FOR_METER(1005, "The meter activation of the usage point contains a meter, but the device counterpoint with MRID {0} could not be found"),
    NO_COMTASK_FOR_COMMAND(1006, "A comtask to execute the device messages could not be located"),
    UNKNOWN_STATUS(1007, "The requested contactor status is not supported at this time"),
    UNKNOWN_UNIT_CODE(1008, "The requested load limit unit is not supported at this time"),
    INCOMPLETE_LOADLIMIT(1009, "Received incomplete load limit - please make sure to specify both the limit and the unit."),
    TOLERANCE_WITHOUT_LOAD_LIMIT(1010, "Received incomplete load limit - load tolerance can only be used in combination with load limitation"),
    NO_COMTASK_FOR_STATUS_INFORMATION(1011, "A comtask to verify the status information could not be located"),
    NO_SUCH_DEVICE(1012, "No device with MRID {0}"),
    INCOMPLETE_CONTACTOR_INFO(1013, "Received incomplete request - please make sure to specify contactor status and/or load limit parameters"),
    NO_HEAD_END_INTERFACE(1013, "Could not find the head-end interface for end device with MRID {0}"),
    COMMAND_ARGUMENT_SPEC_NOT_FOUND(1014, "Could not find the command argument spec {0} for command {1}"),
    COULD_NOT_FIND_SERVICE_CALL_TYPE(1015, "Could not find service call type {0} having version {1}"),
    COULD_NOT_FIND_SERVICE_CALL(1017, "Could not find service call with ID {0}"),
    COULD_NOT_FIND_DESTINATION_SPEC(1018, "Could not find destination spec with name {0}"),
    CALL_BACK_URI_NOT_SPECIFIED(1019, "Not possible to send back the response, as the callback uri was not specified"),
    ;


    private final int number;
    private final String defaultFormat;

    MessageSeeds(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
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
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
