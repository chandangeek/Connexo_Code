package com.elster.jupiter.time;

import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public final class PeriodicalScheduleExpression implements ScheduleExpression {

    private int count;
    private Offset offset;

    private interface Offset {

        ZonedDateTime truncate(ZonedDateTime time);

        ZonedDateTime addOffset(ZonedDateTime time);

        List<String> stringElements();

        TemporalUnit getTemporalUnit();
    }

    private static class Minutely implements Offset {
        private Period period;
        private int secondOfMinute;

        private Minutely(Period period, int secondOfMinute) {
            this.period = period;
            this.secondOfMinute = secondOfMinute;
        }

        @Override
        public ZonedDateTime truncate(ZonedDateTime time) {
            return period.truncate(time);
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return time.plusSeconds(secondOfMinute);
        }

        @Override
        public TemporalUnit getTemporalUnit() {
            return period.temporalField.getBaseUnit();
        }

        @Override
        public List<String> stringElements() {
            ArrayList<String> strings = new ArrayList<>();
            strings.add(period.name());
            strings.add(String.valueOf(secondOfMinute));
            return strings;
        }
    }

    private static class Hourly extends Minutely {
        private int minuteOfHour;

        private Hourly(Period period, int minuteOfHour, int secondOfMinute) {
            super(period, secondOfMinute);
            this.minuteOfHour = minuteOfHour;
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(time.plusMinutes(minuteOfHour));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(String.valueOf(minuteOfHour));
            return strings;
        }
    }

    private static class Daily extends Hourly {
        private int hourOfDay;

        private Daily(Period period, int hourOfDay, int minuteOfHour, int secondOfMinute) {
            super(period, minuteOfHour, secondOfMinute);
            this.hourOfDay = hourOfDay;
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(time.plusHours(hourOfDay));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(String.valueOf(hourOfDay));
            return strings;
        }
    }

    private static class Weekly extends Daily {
        private DayOfWeek dayOfWeek;

        private Weekly(Period period, DayOfWeek dayOfWeek, int hourOfDay, int minuteOfHour, int secondOfMinute) {
            super(period, hourOfDay, minuteOfHour, secondOfMinute);
            this.dayOfWeek = dayOfWeek;
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(time.with(ChronoField.DAY_OF_WEEK, dayOfWeek.getValue()));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(dayOfWeek.name());
            return strings;
        }
    }

    private static class Monthly extends Daily {
        private DayOfMonth dayOfMonth;

        private Monthly(Period period, DayOfMonth dayOfMonth,  int hourOfDay, int minuteOfHour, int secondOfMinute) {
            super(period, hourOfDay, minuteOfHour, secondOfMinute);
            this.dayOfMonth = dayOfMonth;
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(dayOfMonth.addTo(time));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(dayOfMonth.toString());
            return strings;
        }
    }

    private static class Yearly extends Monthly {
        private int monthOfYear;

        private Yearly(Period period, int monthOfYear, DayOfMonth dayOfMonth,  int hourOfDay, int minuteOfHour, int secondOfMinute) {
            super(period, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute);
            this.monthOfYear = monthOfYear;
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(time.withMonth(monthOfYear));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(String.valueOf(monthOfYear));
            return strings;
        }
    }

    public enum Period {
        YEAR(ChronoField.YEAR), MONTH(ChronoField.MONTH_OF_YEAR), WEEK(IsoFields.WEEK_OF_WEEK_BASED_YEAR) {
            public ZonedDateTime truncate(ZonedDateTime time) {
                ZonedDateTime result = time;
                for (Period period : values()) {
                    if (period.compareTo(DAY) > 0) {
                        result = result.with(period.temporalField, period.temporalField.range().getMinimum());
                    }
                }
                return result.with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue())
                        .with(ChronoField.SECOND_OF_MINUTE, 0)
                        .with(ChronoField.MILLI_OF_SECOND, 0);
            }
        }, DAY(ChronoField.DAY_OF_MONTH), HOUR(ChronoField.HOUR_OF_DAY), MINUTE(ChronoField.MINUTE_OF_HOUR);

        private final TemporalField temporalField;

        Period(TemporalField temporalField) {
            this.temporalField = temporalField;
        }

        public ZonedDateTime truncate(ZonedDateTime time) {
            ZonedDateTime result = time;
            for (Period period : values()) {
                if (period.compareTo(this) > 0 && period != WEEK) {
                    result = result.with(period.temporalField, period.temporalField.range().getMinimum());
                }
            }
            return result.with(ChronoField.SECOND_OF_MINUTE, 0).with(ChronoField.MILLI_OF_SECOND, 0);
        }

    }

    private interface DayOfMonth {

        ZonedDateTime addTo(ZonedDateTime time);
    }

    private static class NthDayOfMonth implements DayOfMonth {
        private final int dayOfMonth;

        private NthDayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        @Override
        public ZonedDateTime addTo(ZonedDateTime time) {
            return time.withDayOfMonth(dayOfMonth);
        }

        @Override
        public String toString() {
            return String.valueOf(dayOfMonth);
        }
    }

    private enum LastDayOfMonth implements DayOfMonth {
        LAST;

        @Override
        public ZonedDateTime addTo(ZonedDateTime time) {
            return time.withDayOfMonth((int) ChronoField.DAY_OF_MONTH.rangeRefinedBy(time).getMaximum());
        }


        @Override
        public String toString() {
            return name();
        }
    }

    @Override
    public Optional<ZonedDateTime> nextOccurrence(ZonedDateTime time) {
        ZonedDateTime result = offset.truncate(time);
        result = offset.addOffset(result);
        if (result.isAfter(time)) {
            return Optional.of(result);
        }
        return Optional.of(result.plus(count, offset.getTemporalUnit()));
    }

    @Override
    public String encoded() {
        StringJoiner joiner = new StringJoiner(",", "P[", "]");
        joiner.add(String.valueOf(count));
        offset.stringElements().forEach(joiner::add);
        return joiner.toString();
    }

    public static Builder every(int count) {
        return new Builder(count);
    }

    public static class Builder {
        private PeriodicalScheduleExpression build = new PeriodicalScheduleExpression();

        Builder(int count) {
            build.count = count;
        }

        public MinutelyBuilder minutes() {
            return new MinutelyBuilder();
        }

        public class MinutelyBuilder {
            public Builder at(int seconds) {
                build.offset = new Minutely(Period.MINUTE, seconds);
                return Builder.this;
            }
        }

        public HourlyBuilder hours() {
            return new HourlyBuilder();
        }

        public class HourlyBuilder {
            Builder at(int minute, int seconds) {
                build.offset = new Hourly(Period.HOUR, minute, seconds);
                return Builder.this;
            }
        }

        public DailyBuilder days() {
            return new DailyBuilder();
        }

        public class DailyBuilder {
            Builder at(int hour, int minute, int seconds) {
                build.offset = new Daily(Period.DAY, hour, minute, seconds);
                return Builder.this;
            }
        }

        public WeeklyBuilder weeks() {
            return new WeeklyBuilder();
        }

        public class WeeklyBuilder {
            Builder at(DayOfWeek dayOfWeek, int hour, int minute, int seconds) {
                build.offset = new Weekly(Period.WEEK, dayOfWeek, hour, minute, seconds);
                return Builder.this;
            }
        }

        public MonthlyBuilder months() {
            return new MonthlyBuilder();
        }

        public class MonthlyBuilder {
            Builder at(int dayOfMonth, int hour, int minute, int seconds) {
                build.offset = new Monthly(Period.MONTH, new NthDayOfMonth(dayOfMonth), hour, minute, seconds);
                return Builder.this;
            }

            Builder atLastDayOfMonth(int hour, int minute, int seconds) {
                build.offset = new Monthly(Period.MONTH, LastDayOfMonth.LAST, hour, minute, seconds);
                return Builder.this;
            }
        }

        public YearlyBuilder years() {
            return new YearlyBuilder();
        }

        public class YearlyBuilder {
            Builder at(int monthOfYear, int dayOfMonth, int hour, int minute, int seconds) {
                build.offset = new Yearly(Period.YEAR, monthOfYear, new NthDayOfMonth(dayOfMonth), hour, minute, seconds);
                return Builder.this;
            }

            Builder atLastDayOfMonth(int monthOfYear, int hour, int minute, int seconds) {
                build.offset = new Yearly(Period.YEAR, monthOfYear, LastDayOfMonth.LAST, hour, minute, seconds);
                return Builder.this;
            }
        }

        public PeriodicalScheduleExpression build() {
            return build;
        }

    }
}
