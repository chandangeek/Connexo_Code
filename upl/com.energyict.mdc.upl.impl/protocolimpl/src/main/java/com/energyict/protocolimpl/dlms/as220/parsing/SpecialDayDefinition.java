package com.energyict.protocolimpl.dlms.as220.parsing;

import com.energyict.mdw.core.CodeCalendar;

/**
 * Describes 1 SpecialDay.
 * Each specialDay contains a start -year/-month/-day and a DayType ID
 */
class SpecialDayDefinition {

    final int year;
    final int month;
    final int day;
    final int dayTypeId;
    private CodeTableParser codeTableParser;

    /**
     * Constructor
     *
     * @param codeCalendar a CodeCalendar with a '0' season-code
     */
    public SpecialDayDefinition(CodeTableParser codeTableParser, CodeCalendar codeCalendar) {
        this.codeTableParser = codeTableParser;
        this.dayTypeId = codeTableParser.getDayIDValue(codeCalendar.getDayType().getId());
        this.year = codeCalendar.getYear();
        this.month = codeCalendar.getMonth();
        this.day = codeCalendar.getDay();
    }

    /**
     * Getter for the Day value
     *
     * @return the day value
     */
    public int getDay() {
        return day;
    }

    /**
     * Getter for the Month value
     *
     * @return the month value
     */
    public int getMonth() {
        return month;
    }

    /**
     * Getter for the Year value
     *
     * @return the year value
     */
    public int getYear() {
        return year;
    }

    /**
     * Getter for the DayTypeID value
     *
     * @return the DayTypeId
     */
    public int getDayTypeId() {
        return dayTypeId;
    }
}
