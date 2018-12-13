/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;


import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    expression_empty_exception("expression_empty_exception", "Expression cannot be null or empty"),
    interval_description_format("interval_description_format", "every {0} days of the week"),
    between_description_format("between_description_format", "{0} through {1}"),
    between_weekday_description_format("between_weekday_description_format", "{0} through {1}"),
    on_the_day_of_the_month("on_the_day_of_the_month", "on the %s {0} of the month"),
    on_the_of_the_month("on_the_of_the_month", "on the {0} of the month"),
    on_the_last_of_the_month("on_the_last_of_the_month", "on the last {0} of the month"),
    on_the_last_day_of_the_month("on_the_last_day_of_the_month", "on the last day of the month"),
    on_the_last_weekday_of_the_month("on_the_last_weekday_of_the_month", "on the last weekday of the month"),
    between_days_of_the_month("between_days_of_the_monthbetween_days_of_the_month", "between day {0} and {1} of the month"),
    seconds_through_past_the_minute("seconds_through_past_the_minute", "seconds {0} through {1} past the minute"),
    between_x_and_y("between_x_and_y", "between {0} and {1}"),
    past_the_hour("past_the_hour", "past the hour"),
    at_x_seconds_past_the_minute("at_x_seconds_past_the_minute", "at {0} seconds past the minute"),
    minutes_through_past_the_hour("minutes_through_past_the_hour", "minutes {0} through {1} past the hour"),
    on_day_of_the_month("on_day_of_the_month", "on day {0} of the month"),
    first_weekday("first_weekday", "first weekday"),
    weekday_nearest_day("weekday_nearest_day", "weekday nearest day {0}"),
    only_on("only_on", "only on {0}"),
    only_in("only_in", "only in {0}"),
    every("every", "every"),
    every_x_seconds("every_x_seconds", "every {0} seconds"),
    every_minute_between("every_minute_between", "Every minute between {0} and {1}"),
    every_second("every_second", "every second"),
    every_minute("every_minute", "every minute"),
    every_1_minute("every_1_minute", "every 1 minute"),
    every_hour("every_hour", "every hour"),
    every_1_hour("every_1_hour", "every 1 hour"),
    every_day("every_day", "every day"),
    every_1_day("every_1_day", "every 1 day"),
    every_year("every_year", "every year"),
    every_x("every_x", "every {0}"),
    at_x("at_x", "at {0}"),
    first("first", "first"),
    milliseconds("milliseconds", "milliseconds"),
    seconds("seconds", "seconds"),
    second("second", "second"),
    third("third", "third"),
    fourth("fourth", "fourth"),
    fifth("fifth", "fifth"),
    time_pm("time_pm", "PM"),
    time_am("time_am", "AM"),
    and("and", "and"),
    at("at", "At"),
    day("day", "day"),
    days("days", "days"),
    hour("hour", "hour"),
    hours("hours", "hours"),
    minute("minute", "minute"),
    minutes("minutes", "minutes"),
    week("week", "week"),
    weeks("weeks", "weeks"),
    month("month", "month"),
    months("months", "months"),
    year("year", "year"),
    years("years", "years"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}