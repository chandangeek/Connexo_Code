package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum TelegramFunctionType {
    INSTANTANEOUS_VALUE(0),
    MAXIMUM_VALUE(1),
    MINIMUM_VALUE(2),
    ERROR_STATE_VALUE(3),
    SPECIAL_FUNCTION(4),
    SPECIAL_FUNCTION_FILL_BYTE(5),
    USER_DEFINED_CELL_ID(6);

    private int value;

    private TelegramFunctionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}