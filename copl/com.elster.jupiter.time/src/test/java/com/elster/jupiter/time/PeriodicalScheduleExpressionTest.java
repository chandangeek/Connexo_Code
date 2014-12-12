package com.elster.jupiter.time;

import com.elster.jupiter.util.time.ScheduleExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class PeriodicalScheduleExpressionTest {

    @Test
    public void test() {
        List<PeriodicalScheduleExpression> expressions =
                asList(
                        PeriodicalScheduleExpression.every(2).months().at(4, 0, 0, 0).build(),
                        PeriodicalScheduleExpression.every(3).years().atLastDayOfMonth(2, 14, 15, 22).build(),
                        PeriodicalScheduleExpression.every(4).weeks().at(DayOfWeek.TUESDAY, 0, 0, 0).build(),
                        PeriodicalScheduleExpression.every(5).days().at(16, 12, 11).build(),
                        PeriodicalScheduleExpression.every(6).hours().at(10, 0).build(),
                        PeriodicalScheduleExpression.every(7).minutes().at(23).build()
                );

        expressions.stream().map(ScheduleExpression::encoded).forEach(System.out::println);

        ZonedDateTime time = ZonedDateTime.now();

        expressions.stream()
                .map(sch -> sch.nextOccurrence(time))
                .forEach(System.out::println);

        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(3, TimeDuration.TimeUnit.YEARS), new TimeDuration(0, TimeDuration.TimeUnit.SECONDS));

        Optional<ZonedDateTime> threeYears = temporalExpression.nextOccurrence(temporalExpression.nextOccurrence(time).get());

        System.out.println(threeYears);
    }


}