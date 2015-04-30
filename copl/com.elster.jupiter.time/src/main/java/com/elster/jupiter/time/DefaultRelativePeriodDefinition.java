package com.elster.jupiter.time;

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

import static com.elster.jupiter.time.RelativeField.*;

public enum DefaultRelativePeriodDefinition {
    LAST_7_DAYS("Last 7 days", atMidnight(DAY.minus(7)), atMidnight()),
    PREVIOUS_MONTH("Previous month", atMidnight(MONTH.minus(1), DAY.equalTo(1)), atMidnight(MONTH.minus(0), DAY.equalTo(1))),
    THIS_MONTH("This month", atMidnight(MONTH.minus(0), DAY.equalTo(1)), atMidnight(DAY.plus(1))),
    PREVIOUS_WEEK("Previous week", onFirstDayOfWeek(atMidnight(WEEK.minus(1))), onFirstDayOfWeek(atMidnight(WEEK.minus(0)))),
    THIS_WEEK("This week", onFirstDayOfWeek(atMidnight(WEEK.minus(0))), atMidnight(DAY.plus(1))),
    YESTERDAY("Yesterday", atMidnight(DAY.minus(1)), atMidnight(DAY.minus(0))),
    TODAY("Today", atMidnight(DAY.minus(0)), atMidnight(DAY.plus(1))),
    THIS_YEAR("This year", atMidnight(YEAR.minus(0), MONTH.equalTo(1), DAY.equalTo(1)), atMidnight(DAY.plus(1)));

    private final String name;
    private final RelativeDate from;
    private final RelativeDate to;

    DefaultRelativePeriodDefinition(String name, RelativeDate from, RelativeDate to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    public String getPeriodName() {
        return name;
    }

    public RelativePeriod create(TimeService timeService, List<RelativePeriodCategory> categories) {
        return timeService.createRelativePeriod(name, from, to, categories);
    }

    private static RelativeDate onFirstDayOfWeek(RelativeDate relativeDate) {
        return relativeDate.with(DAY_OF_WEEK.equalTo(getFirstDayOfWeek().getValue()));
    }

    private static RelativeDate atMidnight(RelativeOperation... operations) {
        return new RelativeDate(operations).with(HOUR.equalTo(0), MINUTES.equalTo(0), SECONDS.equalTo(0));
    }

    private static DayOfWeek getFirstDayOfWeek() {
        return WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    }

}
