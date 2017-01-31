/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.ScheduleExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class PeriodicalScheduleExpressionTest extends EqualsContractTest {

    @Parameterized.Parameters
    public static List<Object[]> arguments() {
        return asList(
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(2).months().at(4, 0, 0, 0).build(),
                    "P[2,MONTH,0,0,0,4]",
                    asList(
                            Pair.of(time(2014, 12, 2, 14, 32), time(2014, 12, 4, 0, 0, 0)),
                            Pair.of(time(2014, 12, 4, 14, 32), time(2015, 2, 4, 0, 0, 0)),
                            Pair.of(time(2014, 12, 8, 14, 32), time(2015, 2, 4, 0, 0, 0))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(2).months().atLastDayOfMonth(0, 0, 0).build(),
                    "P[2,MONTH,0,0,0,LAST]",
                    asList(
                            Pair.of(time(2014, 12, 2, 14, 32), time(2014, 12, 31, 0, 0, 0)),
                            Pair.of(time(2014, 12, 31, 14, 32), time(2015, 2, 28, 0, 0, 0)),
                            Pair.of(time(2014, 12, 8, 14, 32), time(2014, 12, 31, 0, 0, 0))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(3).years().atLastDayOfMonth(2, 14, 15, 22).build(),
                    "P[3,YEAR,22,15,14,LAST,2]",
                    asList(
                            Pair.of(time(2012, 4, 18, 15, 12), time(2015, 2, 28, 14, 15, 22)),
                            Pair.of(time(2012, 1, 18, 15, 12), time(2012, 2, 29, 14, 15, 22)),
                            Pair.of(time(2011, 1, 18, 15, 12), time(2011, 2, 28, 14, 15, 22))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(3).years().at(2, 3, 14, 15, 22).build(),
                    "P[3,YEAR,22,15,14,3,2]",
                    asList(
                            Pair.of(time(2012, 4, 18, 15, 12), time(2015, 2, 3, 14, 15, 22)),
                            Pair.of(time(2012, 1, 18, 15, 12), time(2012, 2, 3, 14, 15, 22)),
                            Pair.of(time(2011, 1, 18, 15, 12), time(2011, 2, 3, 14, 15, 22))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(4).weeks().at(DayOfWeek.TUESDAY, 0, 0, 0).build(),
                    "P[4,WEEK,0,0,0,TUESDAY]",
                    asList(
                            Pair.of(time(2014, 12, 8, 15, 12), time(2014, 12, 9, 0, 0, 0)),
                            Pair.of(time(2014, 12, 9, 0, 0), time(2015, 1, 6, 0, 0, 0)),
                            Pair.of(time(2014, 12, 9, 0, 1), time(2015, 1, 6, 0, 0, 0))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(5).days().at(16, 12, 11).build(),
                    "P[5,DAY,11,12,16]",
                    asList(
                            Pair.of(time(1984, 5, 22, 15, 12), time(1984, 5, 22, 16, 12, 11)),
                            Pair.of(time(1984, 5, 22, 17, 12), time(1984, 5, 27, 16, 12, 11)),
                            Pair.of(time(2012, 2, 27, 17, 1), time(2012, 3, 3, 16, 12, 11))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(6).hours().at(10, 0).build(),
                    "P[6,HOUR,0,10]",
                    asList(
                            Pair.of(time(1994, 6, 30, 15, 12), time(1994, 6, 30, 21, 10, 0)),
                            Pair.of(time(1994, 6, 30, 15, 9), time(1994, 6, 30, 15, 10, 0)),
                            Pair.of(time(1994, 6, 30, 21, 18), time(1994, 7, 1, 3, 10, 0))
                    )
            },
            new Object[] {
                    (Supplier<ScheduleExpression>) () -> PeriodicalScheduleExpression.every(7).minutes().at(23).build(),
                    "P[7,MINUTE,23]",
                    asList(
                            Pair.of(time(1994, 6, 30, 15, 12, 24), time(1994, 6, 30, 15, 19, 23)),
                            Pair.of(time(1994, 6, 30, 15, 12, 20), time(1994, 6, 30, 15, 12, 23))
                    )
            }

        );
    }

    private Supplier<PeriodicalScheduleExpression> supplier;
    private PeriodicalScheduleExpression expression;
    private String stringForm;
    private List<Pair<ZonedDateTime, ZonedDateTime>> nextOccurrencePairs;

    public PeriodicalScheduleExpressionTest(Supplier<PeriodicalScheduleExpression> supplier, String stringForm, List<Pair<ZonedDateTime, ZonedDateTime>> nextOccurrencePairs) {
        this.supplier = supplier;
        this.expression = supplier.get();
        this.stringForm = stringForm;
        this.nextOccurrencePairs = nextOccurrencePairs;
    }

    @Test
    public void testEncoded() {
        assertThat(expression.encoded()).isEqualTo(stringForm);
    }

    @Test
    public void testNextOccurrence() {
        for (Pair<ZonedDateTime, ZonedDateTime> pair : nextOccurrencePairs) {
            assertThat(expression.nextOccurrence(pair.getFirst())).contains(pair.getLast());
        }
    }

    @Test
    public void testOffsetDuringDST() {
        PeriodicalScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(14, 0, 0).build();
        ZoneId zone = ZoneId.of("Europe/Brussels");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        ZonedDateTime date = ZonedDateTime.of(2013, 3, 29, 2, 30, 0, 0, zone);

        ZonedDateTime expected1 = ZonedDateTime.of(2013, 3, 29, 14, 0, 0, 0, zone);
        ZonedDateTime expected2 = ZonedDateTime.of(2013, 3, 30, 14, 0, 0, 0, zone);
        ZonedDateTime expected3 = ZonedDateTime.of(2013, 3, 31, 14, 0, 0, 0, zone);
        ZonedDateTime expected4 = ZonedDateTime.of(2013, 4, 1, 14, 0, 0, 0, zone);
        ZonedDateTime expected5 = ZonedDateTime.of(2013, 4, 2, 14, 0, 0, 0, zone);

        assertThat(scheduleExpression.nextOccurrence(date)).contains(expected1);
        assertThat(scheduleExpression.nextOccurrence(expected1)).contains(expected2);
        assertThat(scheduleExpression.nextOccurrence(expected2)).contains(expected3);
        assertThat(scheduleExpression.nextOccurrence(expected3)).contains(expected4);
        assertThat(scheduleExpression.nextOccurrence(expected4)).contains(expected5);
    }

    @Test
    public void testOffsetDuringDSTTricky() {
        PeriodicalScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(2, 30, 0).build();
        ZoneId zone = ZoneId.of("Europe/Brussels");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        ZonedDateTime date = ZonedDateTime.of(2013, 3, 29, 1, 30, 0, 0, zone);

        ZonedDateTime expected1 = ZonedDateTime.of(2013, 3, 29, 2, 30, 0, 0, zone);
        ZonedDateTime expected2 = ZonedDateTime.of(2013, 3, 30, 2, 30, 0, 0, zone);
        ZonedDateTime expected3 = ZonedDateTime.of(2013, 3, 31, 2, 30, 0, 0, zone);
        ZonedDateTime expected4 = ZonedDateTime.of(2013, 4, 1, 3, 30, 0, 0, zone);
        ZonedDateTime expected5 = ZonedDateTime.of(2013, 4, 2, 2, 30, 0, 0, zone);

        assertThat(scheduleExpression.nextOccurrence(date)).contains(expected1);
        assertThat(scheduleExpression.nextOccurrence(expected1)).contains(expected2);
        assertThat(scheduleExpression.nextOccurrence(expected2)).contains(expected3);
        assertThat(scheduleExpression.nextOccurrence(expected3)).contains(expected4);
        assertThat(scheduleExpression.nextOccurrence(expected4)).contains(expected5);
    }

    @Override
    protected Object getInstanceA() {
        return expression;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return supplier.get();
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return asList(
                PeriodicalScheduleExpression.every(4).years().at(2, 3, 14, 15, 22).build(),
                PeriodicalScheduleExpression.every(3).years().at(3, 3, 14, 15, 22).build(),
                PeriodicalScheduleExpression.every(3).years().at(2, 4, 14, 15, 22).build(),
                PeriodicalScheduleExpression.every(3).years().at(2, 3, 12, 15, 22).build(),
                PeriodicalScheduleExpression.every(3).years().at(2, 3, 14, 17, 22).build(),
                PeriodicalScheduleExpression.every(3).years().at(2, 3, 14, 15, 23).build()
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    private static ZonedDateTime time(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 44, 654897321, ZoneId.systemDefault());
    }

    private static ZonedDateTime time(int year, int month, int day, int hour, int minute, int second) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.systemDefault());
    }

}