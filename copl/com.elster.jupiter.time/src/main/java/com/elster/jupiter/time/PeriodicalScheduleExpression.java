/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
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

        default String toString(int count, Thesaurus thesaurus) {
            MessageFormat format = new MessageFormat("");
            ChoiceFormat choiceFormat = new ChoiceFormat(new double[] {1, 2}, new String[] {getSingular(thesaurus), getPlural(thesaurus)});
            format.applyPattern("{0}");
            format.setFormats(new Format[] {choiceFormat});
            return format.format(new Object[]{count});
        }

        String getPlural(Thesaurus thesaurus);

        String getSingular(Thesaurus thesaurus);

    }

    private static class Minutely implements Offset {
        private Period period;
        private int secondOfMinute;

        private Minutely(Period period, int secondOfMinute) {
            this.period = period;
            this.secondOfMinute = secondOfMinute;
            checkRange(secondOfMinute, 0, 59);
        }

        @Override
        public ZonedDateTime truncate(ZonedDateTime time) {
            return period.truncate(time);
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return time.withSecond(secondOfMinute);
        }

        @Override
        public TemporalUnit getTemporalUnit() {
            return period.temporalField.getBaseUnit();
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = new ArrayList<>();
            strings.add(period.name());
            strings.add(String.valueOf(secondOfMinute));
            return strings;
        }

        @Override
        public String getPlural(Thesaurus thesaurus) {
            return thesaurus.getString("tme.minutes", "every {0} minutes");
        }

        @Override
        public String getSingular(Thesaurus thesaurus) {
            return thesaurus.getString("tme.minute", "every minute");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Minutely minutely = (Minutely) o;

            return secondOfMinute == minutely.secondOfMinute && period == minutely.period;

        }

        @Override
        public int hashCode() {
            return 31 * period.hashCode() + secondOfMinute;
        }
    }

    private static class Hourly extends Minutely {
        private int minuteOfHour;

        private Hourly(Period period, int minuteOfHour, int secondOfMinute) {
            super(period, secondOfMinute);
            this.minuteOfHour = minuteOfHour;
            checkRange(minuteOfHour, 0, 59);
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(time.withMinute(minuteOfHour));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(String.valueOf(minuteOfHour));
            return strings;
        }

        @Override
        public String getPlural(Thesaurus thesaurus) {
            return thesaurus.getString("tme.hours", "every {0} hours");
        }

        @Override
        public String getSingular(Thesaurus thesaurus) {
            return thesaurus.getString("tme.hour", "every hour");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            Hourly hourly = (Hourly) o;

            return minuteOfHour == hourly.minuteOfHour;

        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + minuteOfHour;
        }
    }

    private static class Daily extends Hourly {
        private int hourOfDay;

        private Daily(Period period, int hourOfDay, int minuteOfHour, int secondOfMinute) {
            super(period, minuteOfHour, secondOfMinute);
            this.hourOfDay = hourOfDay;
            checkRange(hourOfDay, 0, 23);
        }

        @Override
        public ZonedDateTime addOffset(ZonedDateTime time) {
            return super.addOffset(time.withHour(hourOfDay));
        }

        @Override
        public List<String> stringElements() {
            List<String> strings = super.stringElements();
            strings.add(String.valueOf(hourOfDay));
            return strings;
        }

        @Override
        public String getPlural(Thesaurus thesaurus) {
            return thesaurus.getString("tme.days", "every {0} days");
        }

        @Override
        public String getSingular(Thesaurus thesaurus) {
            return thesaurus.getString("tme.day", "every day");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            Daily daily = (Daily) o;

            return hourOfDay == daily.hourOfDay;
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + hourOfDay;
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

        @Override
        public String getPlural(Thesaurus thesaurus) {
            return thesaurus.getString("tme.weeks", "every {0} weeks");
        }

        @Override
        public String getSingular(Thesaurus thesaurus) {
            return thesaurus.getString("tme.week", "every week");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            Weekly weekly = (Weekly) o;

            return dayOfWeek == weekly.dayOfWeek;

        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + dayOfWeek.hashCode();
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

        @Override
        public String getPlural(Thesaurus thesaurus) {
            return thesaurus.getString("tme.months", "every {0} months");
        }

        @Override
        public String getSingular(Thesaurus thesaurus) {
            return thesaurus.getString("tme.month", "every month");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            Monthly monthly = (Monthly) o;

            return dayOfMonth.equals(monthly.dayOfMonth);

        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + dayOfMonth.hashCode();
        }
    }

    private static class Yearly extends Monthly {
        private int monthOfYear;

        private Yearly(Period period, int monthOfYear, DayOfMonth dayOfMonth,  int hourOfDay, int minuteOfHour, int secondOfMinute) {
            super(period, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute);
            this.monthOfYear = monthOfYear;
            checkRange(monthOfYear, 1, 12);
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

        @Override
        public String getPlural(Thesaurus thesaurus) {
            return thesaurus.getString("tme.years", "every {0} years");
        }

        @Override
        public String getSingular(Thesaurus thesaurus) {
            return thesaurus.getString("tme.year", "every year");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            Yearly yearly = (Yearly) o;

            return monthOfYear == yearly.monthOfYear;

        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + monthOfYear;
        }
    }

    public enum Period {
        YEAR(ChronoField.YEAR, "years"),
        MONTH(ChronoField.MONTH_OF_YEAR, "months"),
        WEEK(IsoFields.WEEK_OF_WEEK_BASED_YEAR, "weeks") {
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
        },
        DAY(ChronoField.DAY_OF_MONTH, "days"),
        HOUR(ChronoField.HOUR_OF_DAY, "hours"),
        MINUTE(ChronoField.MINUTE_OF_HOUR, "minutes");

        private final TemporalField temporalField;
        private final String identifier;

        Period(TemporalField temporalField, String identifier) {
            this.temporalField = temporalField;
            this.identifier = identifier;
        }

        ZonedDateTime truncate(ZonedDateTime time) {
            ZonedDateTime result = time;
            for (Period period : values()) {
                if (period.compareTo(this) > 0 && period != WEEK) {
                    result = result.with(period.temporalField, period.temporalField.range().getMinimum());
                }
            }
            return result.with(ChronoField.SECOND_OF_MINUTE, 0).with(ChronoField.MILLI_OF_SECOND, 0);
        }

        public String getIdentifier() {
            return identifier;
        }

        public static Optional<Period> of(String identifier) {
            return Arrays.stream(values())
                    .filter(e -> e.identifier.equals(identifier))
                    .findFirst();
        }
    }

    private interface DayOfMonth {
        ZonedDateTime addTo(ZonedDateTime time);
    }

    private static class NthDayOfMonth implements DayOfMonth {
        private final int dayOfMonth;

        private NthDayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
            checkRange(dayOfMonth, 1, 31);
        }

        @Override
        public ZonedDateTime addTo(ZonedDateTime time) {
            long maximum = ChronoField.DAY_OF_MONTH.rangeRefinedBy(time).getMaximum();
            if (maximum < dayOfMonth) {
                return time.withDayOfMonth((int) maximum);
            }
            return time.withDayOfMonth(dayOfMonth);
        }

        @Override
        public String toString() {
            return String.valueOf(dayOfMonth);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NthDayOfMonth that = (NthDayOfMonth) o;

            return dayOfMonth == that.dayOfMonth;

        }

        @Override
        public int hashCode() {
            return dayOfMonth;
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
            public Builder at(int minute, int seconds) {
                build.offset = new Hourly(Period.HOUR, minute, seconds);
                return Builder.this;
            }
        }

        public DailyBuilder days() {
            return new DailyBuilder();
        }

        public class DailyBuilder {
            public Builder at(int hour, int minute, int seconds) {
                build.offset = new Daily(Period.DAY, hour, minute, seconds);
                return Builder.this;
            }
        }

        public WeeklyBuilder weeks() {
            return new WeeklyBuilder();
        }

        public class WeeklyBuilder {
            public Builder at(DayOfWeek dayOfWeek, int hour, int minute, int seconds) {
                build.offset = new Weekly(Period.WEEK, dayOfWeek, hour, minute, seconds);
                return Builder.this;
            }
        }

        public MonthlyBuilder months() {
            return new MonthlyBuilder();
        }

        public class MonthlyBuilder {
            public Builder at(int dayOfMonth, int hour, int minute, int seconds) {
                build.offset = new Monthly(Period.MONTH, new NthDayOfMonth(dayOfMonth), hour, minute, seconds);
                return Builder.this;
            }

            public Builder atLastDayOfMonth(int hour, int minute, int seconds) {
                build.offset = new Monthly(Period.MONTH, LastDayOfMonth.LAST, hour, minute, seconds);
                return Builder.this;
            }
        }

        public YearlyBuilder years() {
            return new YearlyBuilder();
        }

        public class YearlyBuilder {
            public Builder at(int monthOfYear, int dayOfMonth, int hour, int minute, int seconds) {
                build.offset = new Yearly(Period.YEAR, monthOfYear, new NthDayOfMonth(dayOfMonth), hour, minute, seconds);
                return Builder.this;
            }

            public Builder atLastDayOfMonth(int monthOfYear, int hour, int minute, int seconds) {
                build.offset = new Yearly(Period.YEAR, monthOfYear, LastDayOfMonth.LAST, hour, minute, seconds);
                return Builder.this;
            }
        }

        public PeriodicalScheduleExpression build() {
            return build;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PeriodicalScheduleExpression that = (PeriodicalScheduleExpression) o;

        return count == that.count && offset.equals(that.offset);

    }

    @Override
    public int hashCode() {
        return 31 * offset.hashCode() + count;
    }

    @Override
    public String toString() {
        return encoded();
    }

    public String toString(Thesaurus timeServicesThesaurus) {
        return offset.toString(count, timeServicesThesaurus);
    }

    private static void checkRange(int actual, int lower, int upper) {
        if (actual < lower || actual > upper) {
            throw new IllegalArgumentException(MessageFormat.format("Value {0} is outside bounds : {1} - {2}", actual, lower, upper));
        }
    }

}