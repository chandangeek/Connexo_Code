package com.elster.jupiter.tasks.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;

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
        private final Class<? extends Enum> stringParser;
        private final DateTimeFieldType fieldType;

        Field(long mask, DateTimeFieldType fieldType, Class<? extends Enum> stringParser) {
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
        this.expression = expression;

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
        LocalDate localDate = nextValidDay(dateTime);
        LocalTime localTime = nextTimeWithinDay(new LocalTime().withMillisOfDay(0));
        return localDate.toLocalDateTime(localTime).toDateTime().toDate();
    }

    @Override
    public boolean matches(Date date) {
        //TODO automatically generated method body, provide implementation.
        return false;
    }

    private LocalDate nextValidDay(ReadableInstant date) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    private LocalTime nextTimeWithinDay(LocalTime localTime) {
        LocalTime result = localTime;
        if (fieldMatches(result, Field.HOURS)) {
            result = nextTimeWithinHour(result);
        }
        result = advanceToNextValidValue(result, Field.HOURS);
        return result == null ? null : nextTimeWithinHour(result);
    }

    private LocalTime advanceToNextValidValue(LocalTime result, Field field) {
        long tailSet = tailSet(result, field);
        int nextValidValue = trailingZeroes(tailSet);
        result = nextValidValue < 0 ? null : result.withMillisOfDay(0).withField(field.getFieldType(), nextValidValue);
        return result;
    }

    private long tailSet(LocalTime result, Field field) {
        long hours = fields[field.ordinal()];
        long tailFilter = 1L << result.get(field.getFieldType());
        tailFilter = ~(tailFilter | (tailFilter - 1));
        return hours & tailFilter;
    }

    private LocalTime nextTimeWithinHour(LocalTime localTime) {
        LocalTime result = localTime;
        if (fieldMatches(result, Field.MINUTES)) {
            result = nextTimeWithinMinute(result);
        }
        result = advanceToNextValidValue(result, Field.MINUTES);
        return result == null ? null :nextTimeWithinHour(result);
    }

    private LocalTime nextTimeWithinMinute(LocalTime localTime) {
        LocalTime result = localTime;
        if (fieldMatches(result, Field.SECONDS)) {
            result = nextTimeWithinMinute(result);
        }
        return advanceToNextValidValue(result, Field.SECONDS);
    }

    private boolean fieldMatches(LocalTime localTime, Field field) {
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

    private boolean isInMatchingYear(ReadableDateTime dateTime) {
        for (YearMatcher yearMatcher : yearMatchers) {
            if (yearMatcher.matches(dateTime.getYear())) {
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
