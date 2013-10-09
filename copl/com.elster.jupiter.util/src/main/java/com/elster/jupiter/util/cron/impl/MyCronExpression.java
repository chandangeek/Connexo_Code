package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MyCronExpression implements CronExpression {

    private static final Pattern LAST_EXPRESSION = Pattern.compile("([1-7])?L");
    private static final Pattern NEAREST_WEEKDAY_EXPRESSION = Pattern.compile("([1-9][0-9]?)?W");
    private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("([^ ]+ ){5}[^ ]+]"); // only 6 fields instead of 7, omitting seconds
    private static final Pattern INTEGER = Pattern.compile("\\d+");
    private static final Pattern SINGLE_VALUE = Pattern.compile("[\\dA-Z]+");
    private static final Pattern RANGE = Pattern.compile("([\\dA-Z]+)\\-([\\dA-Z]+)");
    private static final Pattern STEPS = Pattern.compile("([\\*\\dA-Z]+)/([\\dA-Z]+)");
    private static final long ALL_SIXTY = 0x0F_FF_FF_FF_FF_FF_FF_FFL;
    private static final long ALL_24 = 0x00_00_00_00_00_FF_FF_FFL;
    private static final long ALL_28 = 0x00_00_00_00_1F_FF_FF_FEL;
    private static final long ALL_29 = 0x00_00_00_00_3F_FF_FF_FEL;
    private static final long ALL_30 = 0x00_00_00_00_7F_FF_FF_FEL;
    private static final long ALL_31 = 0x00_00_00_00_FF_FF_FF_FEL;
    private static final long ALL_12 = 0x00_00_00_00_00_00_1F_FEL;
    private static final long ALL_7 = 0x00_00_00_00_00_00_00_FEL;
    private static final long[] MONTHS = new long[]{0, ALL_31, ALL_28, ALL_31, ALL_30, ALL_31, ALL_30, ALL_31, ALL_31, ALL_30, ALL_31, ALL_30, ALL_31};
    private static final long[] LEAP_MONTHS = new long[]{0, ALL_31, ALL_29, ALL_31, ALL_30, ALL_31, ALL_30, ALL_31, ALL_31, ALL_30, ALL_31, ALL_30, ALL_31};
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = {
            0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8,
            31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
    };
    private static final int DE_BRUIJN_MULTIPLIER = 0x077CB531;
    private static final int DE_BRUIJN_SHIFT = 27;
    private static final int BITS_PER_INT = 32;
    private static final int SATURDAY_FLAG = 0x80;
    private static final int MAX_OCCURRENCES_OF_DAY_OF_WEEK_IN_ONE_MONTH = 5;
    private final String expression;

    private long[] fields = new long[Field.values().length - 1];
    private List<YearMatcher> yearMatchers = new ArrayList<>();

    private boolean lastDayOfMonth;
    private Set<DayOfWeek> lastDayOfWeek = EnumSet.noneOf(DayOfWeek.class);
    private long nearestWeekDay;
    private boolean noDayOfMonthChecks;
    private boolean noDayOfWeekChecks;
    private boolean lastWeekDay;

    private enum Month {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC
    }

    private enum DayOfWeek {
        SUN(7), MON(1), TUE(2), WED(3), THU(4), FRI(5), SAT(6);

        private final int jodaIndex;

        DayOfWeek(int jodaIndex) {
            this.jodaIndex = jodaIndex;
        }

        private int getJodaIndex() {
            return jodaIndex;
        }

        public static DayOfWeek forJodaIndex(int j) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                if (dayOfWeek.getJodaIndex() == j) {
                    return dayOfWeek;
                }
            }
            return null;
        }
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
            } else if (isLastExpression(subSetExpression)) {
                handleLastExpressionValue(field, subSetExpression, 0);
            } else if (SINGLE_VALUE.matcher(subSetExpression).matches()) {
                int year = numeric(field, subSetExpression);
                yearMatchers.add(new SingleYear(year));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private void handleSetBasedField(Field field, String fieldExpression) {
        fields[field.ordinal()] |= valuesForExpression(field, fieldExpression);
    }

    private long valuesForExpression(Field field, String fieldExpression) {
        long set = 0;
        Scanner andScanner = new Scanner(fieldExpression).useDelimiter(",");
        while (andScanner.hasNext()) {
            set |= valuesForSingleExpression(field, andScanner.next());
        }
        return set;
    }

    private long valuesForSingleExpression(Field field, String subSetExpression) {
        long subset = 0;
        Matcher matcher;
        if ("*".equals(subSetExpression)) {
            subset |= field.getMask();
        } else if ((matcher = STEPS.matcher(subSetExpression)).matches()) {
            String singleValueExpression = matcher.group(1);
            int from = numeric(field, singleValueExpression);
            int step = Integer.parseInt(matcher.group(2));
            long bit = 1L << from;
            while ((bit & field.getMask()) > 0) {
                subset |= bit;
                bit <<= step;
            }
            subset &= field.getMask();
        } else if ((matcher = RANGE.matcher(subSetExpression)).matches()) {
            int from = numeric(field, matcher.group(1));
            int to = numeric(field, matcher.group(2));
            while (from <= to) {
                subset |= 1L << from;
                from++;
            }
        } else if (isLastExpression(subSetExpression)) {
            subset = handleLastExpressionValue(field, subSetExpression, subset);
        } else if ("LW".equalsIgnoreCase(subSetExpression)) {
            lastWeekDay = true;
        } else if ((matcher = NEAREST_WEEKDAY_EXPRESSION.matcher(subSetExpression)).matches()) {
            Integer dayOfMonth = Integer.valueOf(matcher.group(1));
            nearestWeekDay |= (1 << dayOfMonth);
        } else if (SINGLE_VALUE.matcher(subSetExpression).matches()) {
            int value = numeric(field, subSetExpression);
            subset |= (1L << value);
        } else if ("?".equals(subSetExpression)) {
            if (Field.DAY_OF_MONTH.equals(field)) {
                noDayOfMonthChecks = true;
            } else if (Field.DAY_OF_WEEK.equals(field)) {
                noDayOfWeekChecks = true;
            } else {
                throw new IllegalArgumentException("? not allowed here");
            }
        } else {
            throw new IllegalArgumentException("Invalid expression : " + subSetExpression);
        }
        return subset;
    }

    public Date nextAfter(Date date) {
        boolean inEarlierDSTOverlap = isInEarlierDSTOverlap(date);
        Date normalNextAfter = doNextAfter(date);
        if (!inEarlierDSTOverlap) {
            return normalNextAfter;
        }
        return isInEarlierDSTOverlap(normalNextAfter) ? normalNextAfter : doNextAfterInclusive(new Date(DateTimeZone.getDefault().nextTransition(date.getTime())));
    }

    private Date doNextAfterInclusive(Date date) {
        if (matches(date)) {
            return date;
        }
        return doNextAfter(date);
    }

    private Date doNextAfter(Date date) {
        DateTime dateTime = new DateTime(date);
        if (isOnMatchingDay(dateTime)) {
            LocalTime localTime = nextTimeWithinDay(new LocalTime(dateTime));
            if (localTime != null) {
                LocalDate localDate = dateTime.toLocalDate();
                LocalDateTime localDateTime = localDate.toLocalDateTime(localTime);
                if (DateTimeZone.getDefault().isLocalDateTimeGap(localDateTime)) {
                    long transition = DateTimeZone.getDefault().nextTransition(localDate.toDateMidnight().getMillis());
                    return nextAfter(new DateTime(transition).toDate());
                }
                return localTime.toDateTime(dateTime).toDate();
            }
        }
        LocalDate localDate = nextValidDay(new LocalDate(dateTime));
        if (localDate == null) {
            return null;
        }
        LocalTime localTime = new LocalTime().withMillisOfDay(0);
        if (!matches(localTime)) {
            localTime = nextTimeWithinDay(new LocalTime().withMillisOfDay(0));
        }
        LocalDateTime localDateTime = localDate.toLocalDateTime(localTime);
        if (DateTimeZone.getDefault().isLocalDateTimeGap(localDateTime)) {
            long transition = DateTimeZone.getDefault().nextTransition(localDate.toDateMidnight().getMillis());
            return nextAfter(new DateTime(transition).toDate());
        }
        return localDateTime.toDateTime().toDate();
    }

    private boolean isInEarlierDSTOverlap(Date date) {
        long earlier = date.getTime();
        long later = DateTimeZone.getDefault().adjustOffset(earlier, true);
        return later != earlier;
    }

    @Override
    public boolean matches(Date date) {
        LocalDate localDate = new LocalDate(date);
        return matches(localDate) && matches(new LocalTime(date));
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
        return result;
    }

    private boolean isOnMatchingDayOfWeek(LocalDate result) {
        if (noDayOfWeekChecks) {
            return true;
        }
        long daysOfWeek = fields[Field.DAY_OF_WEEK.ordinal()];
        int dayOfWeek = 1 << (result.getDayOfWeek() % DayOfWeek.values().length + 1);
        return (daysOfWeek & dayOfWeek) != 0 || lastDayOfWeek.contains(DayOfWeek.forJodaIndex(result.getDayOfWeek()));
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
        while (result != null) {
            LocalDate resultWithinMonth = nextDateWithinMonth(result);
            if (resultWithinMonth != null) {
                return resultWithinMonth;
            }
            result = advanceToNextValidValue(result, Field.MONTH);
        }
        return null;
    }

    private boolean yearMatches(LocalDate result) {
        return isMatchingYear(result.getYear());
    }

    private LocalDate advanceToNextValidValue(LocalDate result, Field field) {
        if (Field.YEAR.equals(field)) {
            return advanceToNextYear(result);
        }
        long tailFilter = tailFilter(result, field);
        long tailSet = tailSet(result, field);
        if (Field.DAY_OF_MONTH.equals(field)) {
            long[] months = result.year().isLeap() ? LEAP_MONTHS : MONTHS;
            if (lastDayOfMonth) {
                long lastDayOfMonth = months[result.getMonthOfYear()] | 1;
                lastDayOfMonth = lastDayOfMonth - (lastDayOfMonth >>> 1);
                tailSet |= (lastDayOfMonth & tailFilter);
            }
            if (!lastDayOfWeek.isEmpty()) {
                int monthOfYear = result.getMonthOfYear();
                LocalDate lastDayOfMonth = result.withDayOfMonth(1).plusMonths(1).minusDays(1);
                for (DayOfWeek dayOfWeek : lastDayOfWeek) {
                    LocalDate last = lastDayOfMonth.withDayOfWeek(dayOfWeek.getJodaIndex());
                    if (last.getMonthOfYear() > monthOfYear) {
                        last = last.minusWeeks(1);
                    }
                    tailSet |= ((1 << last.getDayOfMonth()) & tailFilter);
                }
            }
            if (nearestWeekDay != 0) {
                long applied = 0L;
                long source = nearestWeekDay;
                while (source != 0L) {
                    int day = trailingZeroes(source);
                    source &= ~(1 << day);
                    int dayOfWeek = result.withDayOfMonth(day).getDayOfWeek();
                    if (dayOfWeek == DateTimeConstants.SATURDAY) {
                        if (day > 1) {
                            day--; // to friday
                        } else {
                            day += 2; // to monday
                        }
                    }
                    if (dayOfWeek == DateTimeConstants.SUNDAY) {
                        if (day == result.dayOfMonth().getMaximumValue()) {
                            day -= 2; // to friday
                        } else {
                            day++; // to monday
                        }
                    }
                    applied |= (1 << day);
                }
                tailSet |= (applied & tailFilter);
            }
            if (lastWeekDay) {
                LocalDate localDate = result.dayOfMonth().withMaximumValue();
                if (localDate.getDayOfWeek() > DateTimeConstants.FRIDAY) {
                    localDate = localDate.withDayOfWeek(DateTimeConstants.FRIDAY);
                }
                tailSet |= ((1 << localDate.getDayOfMonth()) & tailFilter);
            }
            tailSet = tailSet & months[result.getMonthOfYear()];
        }
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
        long allMarked = fields[field.ordinal()];
        if (Field.DAY_OF_MONTH.equals(field) && !lastDayOfWeek.isEmpty()) {
            for (DayOfWeek dayOfWeek : lastDayOfWeek) {
                LocalDate localDate = lastDayOfWeekInMonth(dayOfWeek, (LocalDate) result);
                allMarked |= (1 << localDate.getDayOfMonth());
            }
        }
        return allMarked & tailFilter(result, field);
    }

    private long tailFilter(ReadablePartial result, Field field) {
        long tailFilter = 1L << result.get(field.getFieldType());
        tailFilter = ~(tailFilter | (tailFilter - 1));
        return tailFilter;
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
        for (Field field : EnumSet.of(Field.MONTH, Field.DAY_OF_MONTH, Field.DAY_OF_WEEK)) {
            int value = field.get(dateTime);
            if (((1L << value) & fields[field.ordinal()]) == 0) {
                return false;
            }
        }
        //TODO incorporate last day of month / last day of week of month
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
        if (localDate == null) {
            return false;
        }
        if (!isMatchingYear(localDate.getYear())) {
            return false;
        }
        for (Field field : EnumSet.of(Field.MONTH, Field.DAY_OF_MONTH, Field.DAY_OF_WEEK)) {
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

    private long handleLastExpressionValue(Field field, String subSetExpression, long set) {
        Matcher lastMatcher = LAST_EXPRESSION.matcher(subSetExpression);
        if (Field.DAY_OF_MONTH.equals(field) && lastMatcher.matches()) {
            lastDayOfMonth = true;
        }
        if (Field.DAY_OF_WEEK.equals(field) && lastMatcher.matches()) {
            if (lastMatcher.group(1) == null) {
                return set | SATURDAY_FLAG;
            } else {
                Integer dayOfWeek = Integer.valueOf(lastMatcher.group(1));
                lastDayOfWeek.add(DayOfWeek.values()[dayOfWeek - 1]);
            }
        }
        return set;
    }

    private boolean isLastExpression(String subSetExpression) {
        return LAST_EXPRESSION.matcher(subSetExpression).matches();
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
        int v = (int) value;
        if (v != 0) {
            return MULTIPLY_DE_BRUIJN_BIT_POSITION[(((v & -v) * DE_BRUIJN_MULTIPLIER) >>> DE_BRUIJN_SHIFT)];
        }
        v = (int) (value >>> BITS_PER_INT);
        return BITS_PER_INT + MULTIPLY_DE_BRUIJN_BIT_POSITION[(((v & -v) * DE_BRUIJN_MULTIPLIER) >>> DE_BRUIJN_SHIFT)];
    }

    private LocalDate nthDayOfWeekInMonth(int n, DayOfWeek dayOfWeek, LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate intermediate = start.withDayOfWeek(dayOfWeek.getJodaIndex());
        return intermediate.isBefore(start) ? intermediate.plusWeeks(n) : intermediate.plusWeeks(n - 1);
    }

    private LocalDate lastDayOfWeekInMonth(DayOfWeek dayOfWeek, LocalDate month) {
        LocalDate intermediate = nthDayOfWeekInMonth(MAX_OCCURRENCES_OF_DAY_OF_WEEK_IN_ONE_MONTH, dayOfWeek, month);
        return intermediate.getMonthOfYear() == month.getMonthOfYear() ? intermediate : intermediate.minusWeeks(1);
    }
}
