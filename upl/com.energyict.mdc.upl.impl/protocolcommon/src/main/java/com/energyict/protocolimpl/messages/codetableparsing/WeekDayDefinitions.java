package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdc.upl.messages.legacy.Extractor;

/**
 * Describes 1 WeekDayDefinition
 * Each definition contains a CalendarDayType ID and a DayOfWeek value
 */
class WeekDayDefinitions {

    private final int dayOfWeek;
    private final int dayTypeId;

    WeekDayDefinitions(CodeTableParser parser, Extractor.CalendarRule rule) {
        this.dayOfWeek = rule.dayOfWeek();
        this.dayTypeId = parser.getDayIDValue(rule.dayTypeId());
    }

    int getDayOfWeek() {
        return dayOfWeek;
    }

    int getDayTypeId() {
        return dayTypeId;
    }

}