package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum TelegramDateMasks {
    DATE(0x02,"Actual Date"),						// 0010 Type G
    DATE_TIME(0x04, "Actual Date and Time"),		// 0100 Type F
    EXT_TIME(0x03, "Extended Date"),				// 0011 Type J
    EXT_DATE_TIME(0x06, "Extended Date and Time");	// 0110 Type I

    private int value;
    private String desc;

    private TelegramDateMasks(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}