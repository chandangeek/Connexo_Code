package com.elster.jupiter.time.impl;

import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativeOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by borunova on 01.10.2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeDateImplTest {
    ZonedDateTime referenceTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

    @Test
    public void testSerialisation() {
        // 2 weeks ago on Monday
        List<RelativeOperation> operations = new ArrayList<>();

        operations.add(new RelativeOperation(RelativeField.WEEK, RelativeOperator.MINUS, 2));
        operations.add(new RelativeOperation(RelativeField.STRING_DAY_OF_WEEK, RelativeOperator.EQUAL, DayOfWeek.MONDAY.getLong(ChronoField.DAY_OF_WEEK)));

        RelativeDateImpl relativeDate = new RelativeDateImpl();
        relativeDate.setPattern(operations);
        assertThat(relativeDate.getRelativeDate()).isEqualTo("2:-:2;12:=:1;");
     }

    @Test
    public void testDeserialisation() {
        String pattern = "2:-:2;12:=:1;";
        RelativeDateImpl relativeDate = new RelativeDateImpl(pattern);
        ZonedDateTime date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusWeeks(2).minusDays(2));
    }

    @Test
    public void testRelativeDate() {
        // 3 months ago on 17 day of month 9:05
        List<RelativeOperation> operations = new ArrayList<>();
        operations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.MINUS, 3));
        operations.add(new RelativeOperation(RelativeField.DAY_IN_MONTH, RelativeOperator.EQUAL, 17));
        operations.add(new RelativeOperation(RelativeField.HOUR_OF_DAY, RelativeOperator.EQUAL, 9));
        operations.add(new RelativeOperation(RelativeField.MINUTES_OF_HOUR, RelativeOperator.EQUAL, 5));
        RelativeDateImpl relativeDate = new RelativeDateImpl();
        relativeDate.setPattern(operations);

        ZonedDateTime date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusMonths(3).withDayOfMonth(17).withHour(9).withMinute(5));

        // 5 months ago on current day of month
        Instant instant = Instant.now();
        ZonedDateTime res = ZonedDateTime.ofInstant(instant, referenceTime.getZone());
        operations = new ArrayList<>();
        operations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.MINUS, 5));
        operations.add(new RelativeOperation(RelativeField.DAY_IN_MONTH, RelativeOperator.EQUAL, res.getDayOfMonth()));
        relativeDate = new RelativeDateImpl();
        relativeDate.setPattern(operations);
        date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusMonths(5).withDayOfMonth(res.getDayOfMonth()));
    }
}
