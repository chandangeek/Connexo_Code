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
    UNKNOWN_READING_TYPE(1010, "The requested load limit reading type is not supported at this time"),
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
