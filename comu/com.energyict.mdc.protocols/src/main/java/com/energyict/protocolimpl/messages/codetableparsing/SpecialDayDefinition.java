package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;

/**
 * Describes 1 SpecialDay.
 * Each specialDay contains a start -year/-month/-day and a DayType ID
 */
class SpecialDayDefinition {

    final int year;
    final int month;
    final int day;
    final int dayTypeId;

    SpecialDayDefinition(CodeTableParser codeTableParser, ExceptionalOccurrence exceptionalOccurrence) {
        this.dayTypeId = codeTableParser.getDayIDValue(exceptionalOccurrence.getDayType().getId());
        if (exceptionalOccurrence instanceof RecurrentExceptionalOccurrence) {
            RecurrentExceptionalOccurrence occurrence = (RecurrentExceptionalOccurrence) exceptionalOccurrence;
            this.year = -1;
            this.month = occurrence.getOccurrence().getMonthValue();
            this.day = occurrence.getOccurrence().getDayOfMonth();
        } else {
            FixedExceptionalOccurrence occurrence = (FixedExceptionalOccurrence) exceptionalOccurrence;
            this.year = occurrence.getOccurrence().getYear();
            this.month = occurrence.getOccurrence().getMonthValue();
            this.day = occurrence.getOccurrence().getDayOfMonth();
        }
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