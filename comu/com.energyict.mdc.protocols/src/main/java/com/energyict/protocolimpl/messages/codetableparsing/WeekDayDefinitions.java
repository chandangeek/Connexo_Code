package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.Period;
import com.energyict.mdc.protocol.api.codetables.CodeCalendar;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Describes 1 WeekDayDefinition
 * Each definition contains a DayType ID and a DayOfWeek value
 */
class WeekDayDefinitions {

    final int dayOfWeek;
    final int dayTypeId;

    WeekDayDefinitions(CodeTableParser codeTableParser, CodeCalendar cc) {
        this.dayOfWeek = cc.getDayOfWeek();
        this.dayTypeId = codeTableParser.getDayIDValue(cc.getDayType().getId());
    }

    private WeekDayDefinitions(DayOfWeek dayOfWeek, Period period, CodeTableParser codeTableParser) {
        this.dayOfWeek = dayOfWeek.ordinal() + 1;
        this.dayTypeId = codeTableParser.getDayIDValue(period.getDayType(dayOfWeek).getId());
    }

    static List<WeekDayDefinitions> fromPeriod(Period period, CodeTableParser codeTableParser) {
        return Stream
                .of(DayOfWeek.values())
                .map(dayOfWeek -> new WeekDayDefinitions(dayOfWeek, period, codeTableParser))
                .collect(Collectors.toList());
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