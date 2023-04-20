package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum TelegramEncoding {
    ENCODING_NULL(0),
    ENCODING_INTEGER(1),
    ENCODING_REAL(2),
    ENCODING_BCD(3),
    ENCODING_VARIABLE_LENGTH(4),
    ENCODING_USER_DEFINED_CELL_ID(5);

    private final int value;

    TelegramEncoding(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}