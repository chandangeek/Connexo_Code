/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.time.PeriodicalScheduleExpression.Period;
import static com.elster.jupiter.time.PeriodicalScheduleExpression.every;


public enum PeriodicalScheduleExpressionParser implements ScheduleExpressionParser {
    INSTANCE;

    private static final String COUNT_GROUP = "count";
    private static final String PERIOD_GROUP = "period";
    private static final String SECOND_GROUP = "second";
    private static final String MINUTE_GROUP = "minute";
    private static final String HOUR_GROUP = "hour";
    private static final String DAY_GROUP = "day";
    private static final String MONTH_GROUP = "month";

    private static final Pattern PATTERN = Pattern.compile("P\\[" +
            "(?<" + COUNT_GROUP + ">\\d+)," +
            "(?<" + PERIOD_GROUP + ">MINUTE|HOUR|DAY|WEEK|MONTH|YEAR)," +
            "(?<" + SECOND_GROUP + ">\\d+)" +
            "(?:,(?<" + MINUTE_GROUP + ">\\d+)" +
            "(?:,(?<" + HOUR_GROUP + ">\\d+)" +
            "(?:,(?<" + DAY_GROUP + ">(?:\\d+|LAST|MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY))" +
            "(?:,(?<" + MONTH_GROUP + ">\\d+)" +
            ")?)?)?)?\\]");
    private static final Pattern allDigits = Pattern.compile("\\d+");
    public static final String LAST = "LAST";

    @Override
    public Optional<PeriodicalScheduleExpression> parse(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            return Optional.ofNullable(doParse(matcher));
        }
        return Optional.empty();
    }

    private PeriodicalScheduleExpression doParse(Matcher matcher) {
        RawParseData data = doParseRaw(matcher);
        if (data == null) {
            return null;
        }

        switch (data.period) {
            case YEAR:
                return data.last ? every(data.count).years().atLastDayOfMonth(data.months, data.hours, data.minutes, data.seconds).build()
                        : every(data.count).years().at(data.months, data.days, data.hours, data.minutes, data.seconds).build();
            case MONTH:
                return data.last ? every(data.count).months().atLastDayOfMonth(data.hours, data.minutes, data.seconds).build()
                        : every(data.count).months().at(data.days, data.hours, data.minutes, data.seconds).build();
            case WEEK:
                return data.dayOfWeek == null ? null : every(data.count).weeks().at(data.dayOfWeek, data.hours, data.minutes, data.seconds).build();
            case DAY:
                return every(data.count).days().at(data.hours, data.minutes, data.seconds).build();
            case HOUR:
                return every(data.count).hours().at(data.minutes, data.seconds).build();
            case MINUTE:
            default:
                return every(data.count).minutes().at(data.seconds).build();
        }
    }

    public Optional<RawParseData> parseRaw(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            return Optional.ofNullable(doParseRaw(matcher));
        }
        return Optional.empty();
    }

    private RawParseData doParseRaw(Matcher matcher) {
        int count = Integer.parseInt(matcher.group(COUNT_GROUP));
        Period period = Period.valueOf(matcher.group(PERIOD_GROUP));

        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        int days = 0;
        boolean last = false;
        DayOfWeek dayOfWeek = null;
        int months = 0;

        switch (period) {
            case YEAR:
                months = Integer.valueOf(matcher.group(MONTH_GROUP));
            case MONTH:
                String dayString = matcher.group(DAY_GROUP);
                last = LAST.equals(dayString);
                if (!last) {
                    if (!allDigits.matcher(dayString).matches()) {
                        return null;
                    }
                    days = Integer.valueOf(dayString);
                }
            case WEEK:
                String weekday = matcher.group(DAY_GROUP);
                dayOfWeek = Arrays.stream(DayOfWeek.values())
                        .filter(dow -> dow.name().equals(weekday))
                        .findFirst()
                        .orElse(null);
            case DAY:
                hours = Integer.valueOf(matcher.group(HOUR_GROUP));
            case HOUR:
                minutes = Integer.valueOf(matcher.group(MINUTE_GROUP));
            case MINUTE:
                seconds = Integer.valueOf(matcher.group(SECOND_GROUP));
        }

        return new RawParseData(count, period, seconds, minutes, hours, days, last, dayOfWeek, months);
    }

    public static class RawParseData {
        private int count;
        private Period period;
        private int seconds = 0;
        private int minutes = 0;
        private int hours = 0;
        private int days = 0;
        private boolean last = false;
        private DayOfWeek dayOfWeek = null;
        private int months = 0;

        public RawParseData(int count, Period period, int seconds, int minutes, int hours, int days, boolean last, DayOfWeek dayOfWeek, int months) {
            this.count = count;
            this.period = period;
            this.seconds = seconds;
            this.minutes = minutes;
            this.hours = hours;
            this.days = days;
            this.last = last;
            this.dayOfWeek = dayOfWeek;
            this.months = months;
        }

        public int getCount() {
            return count;
        }

        public Period getPeriod() {
            return period;
        }

        public int getSeconds() {
            return seconds;
        }

        public int getMinutes() {
            return minutes;
        }

        public int getHours() {
            return hours;
        }

        public int getDays() {
            return days;
        }

        public boolean isLast() {
            return last;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public int getMonths() {
            return months;
        }
    }

}
