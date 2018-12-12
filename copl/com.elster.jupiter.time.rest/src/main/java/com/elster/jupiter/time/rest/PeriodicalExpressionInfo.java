/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.PeriodicalScheduleExpressionParser;
import com.elster.jupiter.time.TemporalExpression;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.DayOfWeek;

import static com.elster.jupiter.time.PeriodicalScheduleExpression.every;
import static com.elster.jupiter.time.PeriodicalScheduleExpressionParser.RawParseData;

@XmlRootElement
public class PeriodicalExpressionInfo {

    public int count;

    public String timeUnit;

    public int offsetMonths;

    public int offsetDays;

    public boolean lastDayOfMonth;

    public DayOfWeek dayOfWeek;

    public int offsetHours;

    public int offsetMinutes;

    public int offsetSeconds;

    public PeriodicalExpressionInfo() {
    }

    public PeriodicalExpressionInfo(PeriodicalScheduleExpression expression) {
        RawParseData rawParseData = PeriodicalScheduleExpressionParser.INSTANCE.parseRaw(expression.encoded()).get();
        count = rawParseData.getCount();
        timeUnit = rawParseData.getPeriod().getIdentifier();
        offsetMonths = rawParseData.getMonths();
        offsetDays = rawParseData.getDays();
        lastDayOfMonth = rawParseData.isLast();
        dayOfWeek = rawParseData.getDayOfWeek();
        offsetHours = rawParseData.getHours();
        offsetMinutes = rawParseData.getMinutes();
        offsetSeconds = rawParseData.getSeconds();
    }

    /**
     *  To be able to see previously defined schedules
     */
    public PeriodicalExpressionInfo(TemporalExpression expression) {
        count = expression.getEvery().getCount();
        timeUnit = expression.getEvery().getTimeUnit().getDescription();

        offsetSeconds = 0;
        offsetMinutes = 0;
        offsetHours = 0;
        offsetDays = 0;
        offsetMonths = 0;
        lastDayOfMonth = false;
        dayOfWeek = null;
        switch(expression.getOffset().getTimeUnit()) {
            case SECONDS:
                offsetSeconds = expression.getOffset().getCount();
                return;
            case MINUTES:
                offsetMinutes = expression.getOffset().getCount();
                return;
            case HOURS:
                offsetHours = expression.getOffset().getCount();
                return;
            case DAYS:
                int days = expression.getOffset().getCount();
                if ("weeks".equals(timeUnit)) {
                    dayOfWeek = DayOfWeek.of(days);
                    return;
                }
                if (days > 28) {
                    lastDayOfMonth = true;
                    return;
                }
                offsetDays = days;
                return;
            case MONTHS:
                offsetMonths = expression.getOffset().getCount();
                return;
            default:
        }
    }

    public PeriodicalScheduleExpression toExpression() {
        return PeriodicalScheduleExpression.Period.of(timeUnit).map(period -> {
            switch (period) {
                case YEAR:
                    return lastDayOfMonth ? every(count).years().atLastDayOfMonth(offsetMonths, offsetHours, offsetMinutes, offsetSeconds).build()
                            : every(count).years().at(offsetMonths, offsetDays, offsetHours, offsetMinutes, offsetSeconds).build();
                case MONTH:
                    return lastDayOfMonth ? every(count).months().atLastDayOfMonth(offsetHours, offsetMinutes, offsetSeconds).build()
                            : every(count).months().at(offsetDays, offsetHours, offsetMinutes, offsetSeconds).build();
                case WEEK:
                    return dayOfWeek == null ? null : every(count).weeks().at(dayOfWeek, offsetHours, offsetMinutes, offsetSeconds).build();
                case DAY:
                    return every(count).days().at(offsetHours, offsetMinutes, offsetSeconds).build();
                case HOUR:
                    return every(count).hours().at(offsetMinutes, offsetSeconds).build();
                case MINUTE:
                default:
                    return every(count).minutes().at(offsetSeconds).build();
            }
        }).orElseThrow(IllegalArgumentException::new);
    }

    public static PeriodicalExpressionInfo from(PeriodicalScheduleExpression scheduleExpression) {
        return new PeriodicalExpressionInfo(scheduleExpression);
    }
}
