package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdw.core.CodeCalendar;

/**
 * Describes 1 WeekDayDefinition
 * Each definition contains a DayType ID and a DayOfWeek value
 */
class WeekDayDefinitions {

    final int dayOfWeek;

    final int dayTypeId;
    private CodeTableParser codeTableParser;

    public WeekDayDefinitions(CodeTableParser codeTableParser, CodeCalendar cc) {
        this.codeTableParser = codeTableParser;
        this.dayOfWeek = cc.getDayOfWeek();
        this.dayTypeId = codeTableParser.getDayIDValue(cc.getDayType().getId());
    }

    /**
     * Getter for the day of the Week
     *
     * @return the day of the week
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Getter for the dayType ID
     *
     * @return the dayType ID
     */
    public int getDayTypeId() {
        return dayTypeId;
    }
}
