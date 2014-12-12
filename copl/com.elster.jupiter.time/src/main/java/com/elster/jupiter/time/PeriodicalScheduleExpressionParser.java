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

        switch (period) {
            case YEAR:
                return last ? every(count).years().atLastDayOfMonth(months, hours, minutes, seconds).build()
                        : every(count).years().at(months, days, hours, minutes, seconds).build();
            case MONTH:
                return every(count).months().at(days, hours, minutes, seconds).build();
            case WEEK:
                return dayOfWeek == null ? null : every(count).weeks().at(dayOfWeek, hours, minutes, seconds).build();
            case DAY:
                return every(count).days().at(hours, minutes, seconds).build();
            case HOUR:
                return every(count).hours().at(minutes, seconds).build();
            case MINUTE:
            default:
                return every(count).minutes().at(seconds).build();

        }
    }

}
