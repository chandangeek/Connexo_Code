/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.Holidays;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CodeCalendarObject implements Serializable {

    private long codeId;
    private String dayTypeName;
    private int year;
    private int month;
    private int day;
    private int dayOfWeek;
    private long seasonId;

    private static final java.util.Calendar[] HOLIDAYS = new java.util.Calendar[] {
            ProtocolTools.createCalendar(2000, 1, 1),
            ProtocolTools.createCalendar(2000, 1, 6),
            null, // Easter monday has no fixed date!
            ProtocolTools.createCalendar(2000, 4, 25),
            ProtocolTools.createCalendar(2000, 5, 1),
            ProtocolTools.createCalendar(2000, 6, 2),
            ProtocolTools.createCalendar(2000, 8, 15),
            ProtocolTools.createCalendar(2000, 10, 1),
            ProtocolTools.createCalendar(2000, 12, 8),
            ProtocolTools.createCalendar(2000, 12, 25),
            ProtocolTools.createCalendar(2000, 12, 26)
    };

    public static List<CodeCalendarObject> allFrom(Calendar calendar) {
        List<CodeCalendarObject> all = new ArrayList<>();
        calendar
            .getPeriods()
            .stream()
            .flatMap(CodeCalendarObject::from)
            .forEach(all::add);
        calendar
            .getExceptionalOccurrences()
            .stream()
            .map(CodeCalendarObject::from)
            .forEach(all::add);
        return all;
    }

    private static Stream<CodeCalendarObject> from(Period period) {
        return Stream.of(DayOfWeek.values()).map(dayOfWeek -> from(period, dayOfWeek));
    }

    private static CodeCalendarObject from(Period period, DayOfWeek dayOfWeek) {
        CodeCalendarObject cc = new CodeCalendarObject();
        cc.setCodeId(period.getCalendar().getId());
        DayType dayType = period.getDayType(dayOfWeek);
        cc.setDayTypeName(dayType.getName());
        cc.setYear(-1);
        cc.setMonth(-1);
        cc.setDay(-1);
        cc.setDayOfWeek(dayOfWeek.getValue());
        cc.setSeasonId(period.getId());
        return cc;
    }

    private static CodeCalendarObject from(ExceptionalOccurrence exceptionalOccurrence) {
        if (exceptionalOccurrence instanceof FixedExceptionalOccurrence) {
            return from((FixedExceptionalOccurrence) exceptionalOccurrence);
        } else {
            return from((RecurrentExceptionalOccurrence) exceptionalOccurrence);
        }
    }

    private static CodeCalendarObject from(FixedExceptionalOccurrence exceptionalOccurrence) {
        CodeCalendarObject cc = new CodeCalendarObject();
        cc.setCodeId(exceptionalOccurrence.getCalendar().getId());
        cc.setDayTypeName(exceptionalOccurrence.getDayType().getName());
        cc.setYear(exceptionalOccurrence.getOccurrence().getYear());
        cc.setMonth(exceptionalOccurrence.getOccurrence().getMonthValue());
        cc.setDay(exceptionalOccurrence.getOccurrence().getDayOfMonth());
        cc.setDayOfWeek(-1);
        cc.setSeasonId(-1);
        return cc;
    }

    private static CodeCalendarObject from(RecurrentExceptionalOccurrence exceptionalOccurrence) {
        CodeCalendarObject cc = new CodeCalendarObject();
        cc.setCodeId(exceptionalOccurrence.getCalendar().getId());
        cc.setDayTypeName(exceptionalOccurrence.getDayType().getName());
        cc.setYear(-1);
        cc.setMonth(exceptionalOccurrence.getOccurrence().getMonthValue());
        cc.setDay(exceptionalOccurrence.getOccurrence().getDayOfMonth());
        cc.setDayOfWeek(-1);
        cc.setSeasonId(-1);
        return cc;
    }

    public long getCodeId() {
        return codeId;
    }

    public void setCodeId(long codeId) {
        this.codeId = codeId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayTypeName() {
        return dayTypeName;
    }

    public void setDayTypeName(String dayTypeName) {
        this.dayTypeName = dayTypeName;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(long seasonId) {
        this.seasonId = seasonId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isHoliday() {
        if (isFullDate()) {
            if (dayTypeName.endsWith("Holiday")) {
                for (java.util.Calendar holiday : HOLIDAYS) {
                    if (holiday != null) {
                        holiday.set(java.util.Calendar.YEAR, getYear());
                        if (isSameDate(holiday)) {
                            return true;
                        }
                    } else {
                        if (isSameDate(Holidays.getEasterMonday(getYear()))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getHolidayID() {
        if (isHoliday()) {
            for (int i = 0; i < HOLIDAYS.length; i++) {
                java.util.Calendar holiday = HOLIDAYS[i];
                if (holiday != null) {
                    holiday.set(java.util.Calendar.YEAR, getYear());
                    if (isSameDate(holiday)) {
                        return i;
                    }
                } else {
                    if (isSameDate(Holidays.getEasterMonday(getYear()))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public boolean isCustomDay() {
        return isFullDate() && !isHoliday();
    }

    public boolean isSpecialDay() {
        return isCustomDay() || isHoliday();
    }

    private boolean isFullDate() {
        return (year != -1) && (month != -1) && (day != -1);
    }

    public boolean isSameDate(java.util.Calendar calendar) {
        java.util.Calendar cc = getCalendar();
        if (calendar != null) {
            boolean same = true;
            same &= (cc.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR));
            same &= (cc.get(java.util.Calendar.MONTH) == calendar.get(java.util.Calendar.MONTH));
            same &= (cc.get(java.util.Calendar.DAY_OF_MONTH) == calendar.get(java.util.Calendar.DAY_OF_MONTH));
            return same;
        } else {
            return false;
        }
    }

    public boolean isSameDate(CodeCalendarObject codeCalendarObject) {
        return isSameDate(codeCalendarObject.getCalendar());
    }

    public java.util.Calendar getCalendar() {
        return isFullDate() ? ProtocolTools.createCalendar(year, month, day) : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodeCalendarObject) {
            CodeCalendarObject that = (CodeCalendarObject) obj;
            boolean same = true;
            same &= (this.year == that.year);
            same &= (this.month == that.month);
            same &= (this.day == that.day);
            same &= that.dayTypeName.startsWith(this.dayTypeName.substring(0, 2));
            same &= that.dayTypeName.startsWith(this.dayTypeName.substring(3, dayTypeName.length()-1), 3);
            return same;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CodeCalendarObject" +
               "{codeId=" + codeId +
               ", dayTypeId=" + dayTypeName +
               ", year=" + year +
               ", month=" + month +
               ", day=" + day +
               ", dayOfWeek=" + dayOfWeek +
               ", seasonId=" + seasonId +
               ", holiday=" + isHoliday() +
               ", customDay=" + isCustomDay() +
               ", specialDay=" + isSpecialDay() +
               '}';
    }

}