package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.PeriodicalScheduleExpressionParser;

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
