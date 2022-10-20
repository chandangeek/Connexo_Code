package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;

import java.time.temporal.ChronoUnit;

public enum SpacingControlByte {
    /** Signed difference, hours */
    INDEX_HOUR_SPACING_CONTROL_BYTE(0xE2, 1, ChronoUnit.HOURS),

    NIGHTLINE_15_MINUTES_SPACING_CON_BYTE(0xD2, 15, ChronoUnit.MINUTES),

    WEEKLY_LOG_SPACING_CONTROL_BYTE(0xF3, 1, ChronoUnit.DAYS); // to check?!


    private final int value;
    private final int timeAmount;
    private final ChronoUnit chronoUnit;


    SpacingControlByte(int byteValue, int timeAmount, ChronoUnit chronoUnit) {
        this.value = byteValue;
        this.timeAmount = timeAmount;
        this.chronoUnit = chronoUnit;
    }

    public int getValue() {
        return value;
    }

    public int getTimeAmount() {
        return timeAmount;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }
}
