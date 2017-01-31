/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.DayOfWeek;
import java.util.List;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class PeriodicalScheduleExpressionParserTest {

    private PeriodicalScheduleExpressionParser parser = PeriodicalScheduleExpressionParser.INSTANCE;

    @Parameterized.Parameters
    public static List<Object[]> arguments() {
        return asList(
            new Object[] {
                    PeriodicalScheduleExpression.every(2).months().at(4, 0, 0, 0).build(),
                    "P[2,MONTH,0,0,0,4]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(2).months().atLastDayOfMonth(0, 0, 0).build(),
                    "P[2,MONTH,0,0,0,LAST]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(3).years().atLastDayOfMonth(2, 14, 15, 22).build(),
                    "P[3,YEAR,22,15,14,LAST,2]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(3).years().at(2, 3, 14, 15, 22).build(),
                    "P[3,YEAR,22,15,14,3,2]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(4).weeks().at(DayOfWeek.TUESDAY, 0, 0, 0).build(),
                    "P[4,WEEK,0,0,0,TUESDAY]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(5).days().at(16, 12, 11).build(),
                    "P[5,DAY,11,12,16]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(6).hours().at(10, 0).build(),
                    "P[6,HOUR,0,10]"
            },
            new Object[] {
                    PeriodicalScheduleExpression.every(7).minutes().at(23).build(),
                    "P[7,MINUTE,23]"
            }

        );
    }

    private PeriodicalScheduleExpression expression;
    private String stringForm;

    public PeriodicalScheduleExpressionParserTest(PeriodicalScheduleExpression expression, String stringForm) {
        this.expression = expression;
        this.stringForm = stringForm;
    }

    @Test
    public void testParse() {
        assertThat(parser.parse(stringForm)).contains(expression);
    }

}