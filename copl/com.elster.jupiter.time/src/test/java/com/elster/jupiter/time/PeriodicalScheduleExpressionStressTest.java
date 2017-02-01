/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class PeriodicalScheduleExpressionStressTest {

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalValuesForSecond() {
        PeriodicalScheduleExpression.every(2).months().at(4, 0, 0, 61).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalValuesForMinute() {
        PeriodicalScheduleExpression.every(2).months().at(4, 0, 61, 0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalValuesForHour() {
        PeriodicalScheduleExpression.every(2).months().at(4, 27, 0, 0).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalValuesForDay() {
        PeriodicalScheduleExpression.every(2).months().at(35, 0, 0, 0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalValuesForMonth() {
        PeriodicalScheduleExpression.every(3).years().at(13, 3, 14, 15, 22).build();
    }

    @Test
    public void testDayAt31InShortMonth() {
        PeriodicalScheduleExpression expression = PeriodicalScheduleExpression.every(1).months().at(31, 0, 0, 0).build();

        ZonedDateTime time = ZonedDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault());

        ZonedDateTime expected = ZonedDateTime.of(2000, 2, 29, 0, 0, 0, 0, ZoneId.systemDefault());

        assertThat(expression.nextOccurrence(time)).contains(expected);
    }

}