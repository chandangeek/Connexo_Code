package com.elster.jupiter.util.cron.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

import com.elster.jupiter.util.cron.CronExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MyCronExpression implements CronExpression {

    private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("([^ ]+ ){5}[^ ]+]"); // only 6 fields instead of 7, omitting seconds
    private static final Pattern INTEGER = Pattern.compile("\\d+");
    private static final Pattern SINGLE_VALUE = Pattern.compile("[\\dA-Z]+");
    private static final Pattern RANGE = Pattern.compile("([\\dA-Z]+)\\-([\\dA-Z]+)");
    private static final Pattern STEPS = Pattern.compile("([\\*\\dA-Z]+)/([\\dA-Z]+)");
    private static final long ALL_SIXTY = 0x0F_FF_FF_FF_FF_FF_FF_FFL;
    private static final long ALL_24 = 0x00_00_00_00_00_FF_FF_FFL;
    private static final long ALL_31 = 0x00_00_00_00_FF_FF_FF_FEL;
    private static final long ALL_12 = 0x00_00_00_00_00_00_1F_FEL;
    private static final long ALL_7 = 0x00_00_00_00_00_00_00_FEL;
    private final String expression;

    private long[] fields = new long[6];
    private List<YearMatcher> yearMatchers = new ArrayList<>();

    private enum Month {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC
    }

    private enum DayOfWeek {
        SUN, MON, TUE, WED, THU, FRI, SAT
    }

    private interface YearMatcher {

        Integer nextMatch(int year);

        boolean matches(int year);
    }

    private enum AllYears implements YearMatcher {
        Instance;

        @Override
        public Integer nextMatch(int year) {
            return year + 1;
        }

        @Override
        public boolean matches(int year) {
            return true;
        }
    }

    private static class SingleYear implements YearMatcher {

        private final int year;

        private SingleYear(int year) {
            this.year = year;
        }

        @Override
        public Integer nextMatch(int year) {
            return this.year > year ? this.year : null;
        }

        @Override
        public boolean matches(int year) {
            return this.year == year;
        }
    }

    private static class YearRange implements YearMatcher {

        private final int from;
        private final int to;

        private YearRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Integer nextMatch(int year) {
            if (year < from) {
                return from;
            }
            if (year >= to) {
                return null;
            }
            return year + 1;
        }

        @Override
        public boolean matches(int year) {
            return year >= from && year <= to;
        }
    }

    private static class YearSteps implements YearMatcher {

        private final int from;
        private final int step;

        private YearSteps(int from, int step) {
            this.from = from;
            this.step = step;
        }

        @Override
        public Integer nextMatch(int year) {
            int candidate = year + step - (year - from) % step;
            return candidate > year ? candidate : null;
        }

        @Override
        public boolean matches(int year) {
            return year >= from && (year - from) % step == 0;
        }
    }

    private enum Field {
        SECONDS(ALL_SIXTY, DateTimeFieldType.secondOfMinute()), MINUTES(ALL_SIXTY, DateTimeFieldType.minuteOfHour()), HOURS(ALL_24, DateTimeFieldType.hourOfDay()), DAY_OF_MONTH(ALL_31, DateTimeFieldType.dayOfMonth()), MONTH(ALL_12, DateTimeFieldType.monthOfYear(), Month.class), DAY_OF_WEEK(ALL_7, DateTimeFieldType.dayOfWeek(), DayOfWeek.class), YEAR(0, DateTimeFieldType.year());

        private final long mask;
        private final Class<? extends Enum<?>> stringParser;
        private final DateTimeFieldType fieldType;

        Field(long mask, DateTimeFieldType fieldType, Class<? extends Enum<?>> stringParser) {
            this.mask = mask;
            this.fieldType = fieldType;
            this.stringParser = stringParser;
        }

        Field(long mask, DateTimeFieldType fieldType) {
            this(mask, fieldType, null);
        }

        public boolean isLast() {
            return YEAR.equals(this);
        }

        public boolean isSetBased() {
            return this != YEAR;
        }

        public Field next() {
            if (this == YEAR) {
                return null;
            }
            return Field.values()[ordinal() + 1];
        }

        public void set(MutableDateTime mutableDateTime, int value) {
            mutableDateTime.set(getFieldType(), value);
        }

        public int get(ReadableInstant mutableDateTime) {
            return mutableDateTime.get(getFieldType());
        }

        private long getMask() {
            return mask;
        }

        private Class<? extends Enum> getStringParser() {
            return stringParser;
        }

        public DateTimeFieldType getFieldType() {
            return fieldType;
        }
    }

    public MyCronExpression(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null");
        }
        if (SHORT_FORM_PATTERN.matcher(expression).matches()) {
            this.expression = "0 " + expression;
        } else {
            this.expression = expression;
        }

        Field field = Field.SECONDS;
        Scanner scanner = new Scanner(expression);
        while (scanner.hasNext()) {
            String fieldExpression = scanner.next();

            if (field.isSetBased()) {
                handleSetBasedField(field, fieldExpression);
            } else {
                handleYear(field, fieldExpression);
            }

            field = field.next();
        }
    }

    private void handleYear(Field field, String fieldExpression) {
        Scanner andScanner = new Scanner(fieldExpression).useDelimiter(",");
        while (andScanner.hasNext()) {
            String subSetExpression = andScanner.next();
            Matcher matcher;
            if ("*".equals(subSetExpression)) {
                yearMatchers = Collections.<YearMatcher>singletonList(AllYears.Instance);
                return;
            } else if ((matcher = STEPS.matcher(subSetExpression)).matches()) {
                String singleValueExpression = matcher.group(1);
                int from = numeric(field, singleValueExpression);
                int step = Integer.parseInt(matcher.group(2));
                yearMatchers.add(new YearSteps(from, step));
            } else if ((matcher = RANGE.matcher(subSetExpression)).matches()) {
                int from = numeric(field, matcher.group(1));
                int to = numeric(field, matcher.group(2));
                yearMatchers.add(new YearRange(from, to));
            } else if (isSpecialValue(subSetExpression)) {
                handleSpecialValue(field, subSetExpression);
            } else if (SINGLE_VALUE.matcher(subSetExpression).matches()) {
                int year = numeric(field, subSetExpression);
                yearMatchers.add(new SingleYear(year));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private void handleSetBasedField(Field field, String fieldExpression) {
        long set = fields[field.ordinal()];
        Scanner andScanner = new Scanner(fieldExpression).useDelimiter(",");
        while (andScanner.hasNext()) {
            String subSetExpression = andScanner.next();
            Matcher matcher;
            if ("*".equals(subSetExpression)) {
                set |= field.getMask();
            } else if ((matcher = STEPS.matcher(subSetExpression)).matches()) {
                String singleValueExpression = matcher.group(1);
                int from = numeric(field, singleValueExpression);
                int step = Integer.parseInt(matcher.group(2));
                long bit = 1L << from;
                while ((bit & field.getMask()) > 0) {
                    set |= bit;
                    bit <<= step;
                }
                set &= field.getMask();
            } else if ((matcher = RANGE.matcher(subSetExpression)).matches()) {
                int from = numeric(field, matcher.group(1));
                int to = numeric(field, matcher.group(2));
                while (from <= to) {
                    set |= 1L << from;
                    from++;
                }
            } else if (isSpecialValue(subSetExpression)) {
                handleSpecialValue(field, subSetExpression);
            } else if (SINGLE_VALUE.matcher(subSetExpression).matches()) {
                int value = numeric(field, subSetExpression);
                set |= (1L << value);
            } else {
                throw new IllegalArgumentException();
            }
        }
        fields[field.ordinal()] = set;
    }

    public Date nextAfter(Date date) {
        DateTime dateTime = new DateTime(date);
        if (isOnMatchingDay(dateTime)) {
            LocalTime localTime = nextTimeWithinDay(new LocalTime(dateTime));
            if (localTime != null) {
                return localTime.toDateTime(dateTime).toDate();
            }
        }
        LocalDate localDate = nextValidDay(new LocalDate(dateTime));
        LocalTime localTime = new LocalTime().withMillisOfDay(0);
        if (!matches(localTime)) {
            localTime = nextTimeWithinDay(new LocalTime().withMillisOfDay(0));
        }
        return localDate.toLocalDateTime(localTime).toDateTime().toDate();
    }

    @Override
    public boolean matches(Date date) {
        LocalDate localDate = new LocalDate(date);
        if (!matches(localDate)) {
            return false;
        }
        return matches(new LocalTime(date));
    }

    private LocalDate nextValidDay(LocalDate localDate) {
        LocalDate result = localDate;
        if (yearMatches(result)) {
            result = nextDateWithinYear(result);
            if (result != null) {
                return result;
            }
        }
        result = advanceToNextValidValue(localDate, Field.YEAR);
        return result == null ? null : nextDateWithinYear(result);
    }

    private LocalDate nextDateWithinMonth(LocalDate localDate) {
        LocalDate result = advanceToNextValidValue(localDate, Field.DAY_OF_MONTH);
        while (result != null && !isOnMatchingDayOfWeek(result)) {
            result = advanceToNextValidValue(result, Field.DAY_OF_MONTH);
        }
        return localDate;
    }

    private boolean isOnMatchingDayOfWeek(LocalDate result) {
        long daysOfWeek = fields[Field.DAY_OF_WEEK.ordinal()];
        int dayOfWeek = result.getDayOfWeek();
        return (daysOfWeek & (1L << dayOfWeek)) != 0;
    }

    private LocalDate nextDateWithinYear(LocalDate localDate) {
        LocalDate result = localDate;
        if (fieldMatches(result, Field.MONTH)) {
            result = nextDateWithinMonth(result);
            if (result != null) {
                return result;
            }
        }
        result = advanceToNextValidValue(localDate, Field.MONTH);
        if (matches(result)) {
            return result;
        }
        return result == null ? null : nextDateWithinMonth(result);
    }

    private boolean yearMatches(LocalDate result) {
        return isMatchingYear(result.getYear());
    }

    private LocalDate advanceToNextValidValue(LocalDate result, Field field) {
        if (Field.YEAR.equals(field)) {
            return advanceToNextYear(result);
        }
        long tailSet = tailSet(result, field);
        int nextValidValue = trailingZeroes(tailSet);
        result = nextValidValue < 0 ? null : result.withField(field.getFieldType(), nextValidValue).property(field.getFieldType()).roundFloorCopy();
        return result;
    }

    private LocalDate advanceToNextYear(LocalDate result) {
        int year = result.getYear();
        Integer found = null;
        for (YearMatcher yearMatcher : yearMatchers) {
            Integer match = yearMatcher.nextMatch(year);
            if (match != null && (found == null || (match.compareTo(found) < 0))) {
                found = match;
            }
        }
        if (found == null) {
            return null;
        }
        return new LocalDate(found, 1, 1);
    }

    private LocalTime nextTimeWithinDay(LocalTime localTime) {
        LocalTime result = localTime;
        if (fieldMatches(result, Field.HOURS)) {
            result = nextTimeWithinHour(result);
            if (result != null) {
                return result;
            }
        }
        result = advanceToNextValidValue(localTime, Field.HOURS);
        if (matches(result)) {
            return result;
        }
        return result == null ? null : nextTimeWithinHour(result);
    }

    private LocalTime advanceToNextValidValue(LocalTime result, Field field) {
        long tailSet = tailSet(result, field);
        int nextValidValue = trailingZeroes(tailSet);
        result = nextValidValue < 0 ? null : result.withField(field.getFieldType(), nextValidValue).property(field.getFieldType()).roundFloorCopy();
        return result;
    }

    private long tailSet(ReadablePartial result, Field field) {
        long hours = fields[field.ordinal()];
        long tailFilter = 1L << result.get(field.getFieldType());
        tailFilter = ~(tailFilter | (tailFilter - 1));
        return hours & tailFilter;
    }

    private LocalTime nextTimeWithinHour(LocalTime localTime) {
        LocalTime result = localTime;
        if (fieldMatches(result, Field.MINUTES)) {
            result = nextTimeWithinMinute(result);
            if (result != null) {
                return result;
            }
        }
        result = advanceToNextValidValue(localTime, Field.MINUTES);
        if (matches(result)) {
            return result;
        }
        return result == null ? null : nextTimeWithinHour(result);
    }

    private LocalTime nextTimeWithinMinute(LocalTime localTime) {
        return advanceToNextValidValue(localTime, Field.SECONDS);
    }

    private boolean fieldMatches(ReadablePartial localTime, Field field) {
        return (fields[field.ordinal()] & (1L << localTime.get(field.getFieldType()))) != 0L;
    }

    private boolean isOnMatchingDay(ReadableDateTime dateTime) {
        if (!isInMatchingYear(dateTime)) {
            return false;
        }
        for (Field field : EnumSet.of(Field.MONTH, Field.DAY_OF_MONTH)) {
            int value = field.get(dateTime);
            if (((1L << value) & fields[field.ordinal()]) == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(LocalTime localTime) {
        if (localTime == null) {
            return false;
        }
        for (Field field : EnumSet.of(Field.HOURS, Field.MINUTES, Field.SECONDS)) {
            int value = localTime.get(field.getFieldType());
            if (((1L << value) & fields[field.ordinal()]) == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(LocalDate localDate) {
        if (!isMatchingYear(localDate.getYear())) {
            return false;
        }
        for (Field field : EnumSet.of(Field.MONTH, Field.DAY_OF_MONTH)) {
            int value = localDate.get(field.getFieldType());
            if (((1L << value) & fields[field.ordinal()]) == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isInMatchingYear(ReadableDateTime dateTime) {
        int year = dateTime.getYear();
        return isMatchingYear(year);
    }

    private boolean isMatchingYear(int year) {
        for (YearMatcher yearMatcher : yearMatchers) {
            if (yearMatcher.matches(year)) {
                return true;
            }
        }
        return false;
    }

    private long filter(long allValues, long givenSeconds) {
        long mask = 1L << givenSeconds;
        mask = ~((mask - 1) | mask);
        long filter = allValues;
        filter &= mask;
        filter = filter & -filter;
        return filter;
    }


    private void handleSpecialValue(Field field, String subSetExpression) {
        //TODO automatically generated method body, provide implementation.

    }

    private boolean isSpecialValue(String subSetExpression) {
        //TODO automatically generated method body, provide implementation.
        return false;
    }

    private int numeric(Field field, String singleValueExpression) {
        if (INTEGER.matcher(singleValueExpression).matches()) {
            return Integer.parseInt(singleValueExpression);
        }
        if ("*".equals(singleValueExpression)) {
            return 0;
        }
        return Enum.valueOf(field.getStringParser(), singleValueExpression).ordinal() + 1;
        		
    }

    @Override
    public String toString() {
        return "MyCronExpression{" +
                "expression='" + expression + '\'' +
                ", fields=" + Arrays.toString(fields) +
                '}';
    }

    private static int trailingZeroes(long value) {
        if (value == 0) {
            return -1;
        }
        int[] multiplyDeBruijnBitPosition = {
                0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8,
                31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
        };
        int v = (int) value;
        if (v != 0) {
            return multiplyDeBruijnBitPosition[(((v & -v) * 0x077CB531) >>> 27)];
        }
        v = (int) (value >>> 32);
        return 32 + multiplyDeBruijnBitPosition[(((v & -v) * 0x077CB531) >>> 27)];
    }
}
