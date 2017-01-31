/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.Objects;

/**
 * Immutable class that combines MonthDay with Localtime.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (13:17)
 */
public final class DayMonthTime implements Comparable<DayMonthTime> {
    private final MonthDay monthDay;
    private final LocalTime time;

    public static DayMonthTime fromMidnight(MonthDay monthDay) {
        return new DayMonthTime(monthDay, LocalTime.MIDNIGHT);
    }

    public static DayMonthTime from(MonthDay monthDay, LocalTime time) {
        return new DayMonthTime(monthDay, time);
    }

    private DayMonthTime(MonthDay monthDay, LocalTime time) {
        this.monthDay = monthDay;
        this.time = time;
    }

    public LocalDateTime atYear(int year) {
        return LocalDateTime.of(year, this.getMonthValue(), this.getDayOfMonth(), this.getHour(), this.getMinute());
    }

    public DayMonthTime with(Month month) {
        return from(this.monthDay.with(month), this.time);
    }

    public DayMonthTime withMonth(int month) {
        return from(this.monthDay.withMonth(month), this.time);
    }

    public DayMonthTime withDayOfMonth(int dayOfMonth) {
        return from(this.monthDay.withDayOfMonth(dayOfMonth), this.time);
    }

    public DayMonthTime withHour(int hour) {
        return from(this.monthDay, this.time.withHour(hour));
    }

    public DayMonthTime withMinute(int minute) {
        return from(this.monthDay, this.time.withMinute(minute));
    }

    public DayMonthTime withSecond(int second) {
        return from(this.monthDay, this.time.withSecond(second));
    }

    public DayMonthTime withNano(int nano) {
        return from(this.monthDay, this.time.withNano(nano));
    }

    public Month getMonth() {
        return this.monthDay.getMonth();
    }

    public int getMonthValue() {
        return this.monthDay.getMonthValue();
    }

    public int getDayOfMonth() {
        return this.monthDay.getDayOfMonth();
    }

    public int getHour() {
        return this.time.getHour();
    }

    public int getMinute() {
        return this.time.getMinute();
    }

    public int getSecond() {
        return this.time.getSecond();
    }

    public int getNano() {
        return this.time.getNano();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DayMonthTime that = (DayMonthTime) other;
        return Objects.equals(this.monthDay, that.monthDay)
            && Objects.equals(this.time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monthDay, time);
    }

    @Override
    public int compareTo(DayMonthTime other) {
        if (this.monthDay.isBefore(other.monthDay)) {
            return -1;
        } else if (this.monthDay.equals(other.monthDay)) {
            // On same day so consider time
            return this.time.compareTo(other.time);
        } else {
            return 1;
        }
    }

    public boolean isBefore(DayMonthTime other) {
        return this.monthDay.isBefore(other.monthDay)
            || (this.monthDay.equals(other.monthDay) && this.time.isBefore(other.time));
    }

    public boolean isAfter(DayMonthTime other) {
        return this.monthDay.isAfter(other.monthDay)
            || (this.monthDay.equals(other.monthDay) && this.time.isAfter(other.time));
    }

    @Override
    public String toString() {
        return this.monthDay.toString() + 'T' + this.time.toString();
    }

}