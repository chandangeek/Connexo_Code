package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum Telegram_Date_Masks {
    DATE(0x02,"Auctual Date"),						// 0010 Type G
    DATE_TIME(0x04, "Actual Date and Time"),		// 0100 Type F
    EXT_TIME(0x03, "Extented Date"),				// 0011 Type J
    EXT_DATE_TIME(0x06, "Extented Daten and Time");	// 0110 Type I

    private int value;
    private String desc;

    private Telegram_Date_Masks(int value, String desc) {
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