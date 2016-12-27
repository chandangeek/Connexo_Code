package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdc.upl.messages.legacy.Extractor;

/**
 * Describes 1 SpecialDay.
 * Each specialDay contains a start -year/-month/-day and a CalendarDayType ID
 */
class SpecialDayDefinition {

    private final int year;
    private final int month;
    private final int day;
    private final int dayTypeId;

    SpecialDayDefinition(CodeTableParser codeTableParser, Extractor.CalendarRule rule) {
        this.dayTypeId = codeTableParser.getDayIDValue(rule.dayTypeId());
        this.year = rule.year();
        this.month = rule.month();
        this.day = rule.day();
    }

    int getDay() {
        return day;
    }

    int getMonth() {
        return month;
    }

    int getYear() {
        return year;
    }

    int getDayTypeId() {
        return dayTypeId;
    }

}