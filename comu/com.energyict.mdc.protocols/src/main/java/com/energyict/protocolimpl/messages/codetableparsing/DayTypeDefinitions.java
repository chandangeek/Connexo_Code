package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.EventOccurrence;
import com.energyict.mdc.protocol.api.codetables.CodeDayTypeDef;

/**
 * Describes 1 DayType definition.
 * Each definition contains a tariffCode + hour/min/sec
 */
class DayTypeDefinitions {

    final int tariffcode;
    final int hour;
    final int minute;
    final int seconds;

    /**
     * Constructor with a CodeDayTypeDef as argument
     *
     * @param codeDayTypeDef the given CodeDayType definition
     */
    DayTypeDefinitions(CodeDayTypeDef codeDayTypeDef) {
        this.tariffcode = codeDayTypeDef.getCodeValue();
        int tStamp = codeDayTypeDef.getTstampFrom();
        this.hour = tStamp / 10000;
        this.minute = (tStamp - hour * 10000) / 100;
        this.seconds = tStamp - (hour * 10000) - (minute * 100);
    }

    DayTypeDefinitions(EventOccurrence eventOccurrence) {
        this.tariffcode = (int) eventOccurrence.getEvent().getCode();
        this.hour = eventOccurrence.getFrom().getHour();
        this.minute = eventOccurrence.getFrom().getMinute();
        this.seconds = eventOccurrence.getFrom().getSecond();
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
