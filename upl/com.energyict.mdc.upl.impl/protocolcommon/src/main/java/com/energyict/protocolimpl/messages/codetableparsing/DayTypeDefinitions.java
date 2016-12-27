package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdc.upl.messages.legacy.Extractor;

/**
 * Describes 1 CalendarDayType definition.
 * Each definition contains a tariffCode + hour/min/sec
 */
class DayTypeDefinitions {

    final int tariffcode;
    final int hour;
    final int minute;
    final int seconds;

    DayTypeDefinitions(Extractor.CalendarDayTypeSlice slice) {
        this.tariffcode = Integer.parseInt(slice.tariffCode());
        this.hour = slice.start().getHour();
        this.minute = slice.start().getMinute();
        this.seconds = slice.start().getSecond();
    }

    /**
     * Getter for the hour
     *
     * @return the hour value
     */
    public int getHour() {
        return hour;
    }

    /**
     * Getter for the minutes
     *
     * @return the minutes value
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Getter for the seconds
     *
     * @return the seconds value
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Getter for the tariffCode
     *
     * @return the tariffCode
     */
    public int getTariffcode() {
        return tariffcode;
    }
}
