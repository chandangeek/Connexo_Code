/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lucian on 5/15/2015.
 */
class ScanFrequency {

    static Integer toScanFrequency(ScheduleExpression scheduleExpression) {
        int frequency = 1;
        try {
            if (Never.NEVER.equals(scheduleExpression)) {
                frequency = 1;
            } else {
                if (scheduleExpression instanceof TemporalExpression) {
                   PeriodicalExpressionInfo schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                    frequency = schedule.count;
                } else if (scheduleExpression instanceof PeriodicalScheduleExpression) {
                   PeriodicalExpressionInfo schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                    frequency = schedule.count;
                }
                else {
                    Pattern everyMinutesPattern = Pattern.compile("[^ ]+ 0\\/(\\d+) ([^ ]+ ){4}");
                    Matcher matcher = everyMinutesPattern.matcher(scheduleExpression.encoded());
                    if (matcher.find()) {
                        frequency = Integer.valueOf(matcher.group(1));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return frequency;
    }

    static ScheduleExpression toScheduleExpression(Integer scanFrequency) {
        PeriodicalExpressionInfo schedule = new PeriodicalExpressionInfo();
        schedule.offsetSeconds = 0;
        schedule.offsetMinutes = 0;
        schedule.offsetHours = 0;
        schedule.offsetDays = 0;
        schedule.offsetMonths = 0;
        schedule.lastDayOfMonth = false;
        schedule.dayOfWeek = null;
        schedule.count = scanFrequency;
        schedule.timeUnit = PeriodicalScheduleExpression.Period.MINUTE.getIdentifier();
        return schedule.toExpression();
    }

}