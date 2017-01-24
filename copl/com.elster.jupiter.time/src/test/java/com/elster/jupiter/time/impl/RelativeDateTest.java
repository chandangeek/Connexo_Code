package com.elster.jupiter.time.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativeOperator;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.time.RelativeField.DAY;
import static com.elster.jupiter.time.RelativeField.DAY_OF_WEEK;
import static com.elster.jupiter.time.RelativeField.HOUR;
import static com.elster.jupiter.time.RelativeField.MINUTES;
import static com.elster.jupiter.time.RelativeField.MONTH;
import static com.elster.jupiter.time.RelativeField.SECONDS;
import static com.elster.jupiter.time.RelativeField.WEEK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by borunova on 01.10.2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeDateTest extends EqualsContractTest {
    ZonedDateTime referenceTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));

    private RelativeDate instanceA;

    @Test
    public void testSerialisation() {
        // 2 weeks ago on Monday
        List<RelativeOperation> operations = new ArrayList<>();

        operations.add(new RelativeOperation(RelativeField.WEEK, RelativeOperator.MINUS, 2));
        operations.add(new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL, DayOfWeek.MONDAY.getLong(ChronoField.DAY_OF_WEEK)));

        RelativeDate relativeDate = new RelativeDate(operations);
        assertThat(relativeDate.getRelativeDate()).isEqualTo("3:-:2;9:=:1;");
     }

    @Test
    public void testDeserialisation() {
        String pattern = "3:-:2;9:=:1;";
        RelativeDate relativeDate = new RelativeDate(pattern);
        ZonedDateTime date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusWeeks(2).minusDays(2));
    }

    @Test
    public void testRelativeDate() {
        // 3 months ago on 17 day of month 9:05
        List<RelativeOperation> operations = new ArrayList<>();
        operations.add(new RelativeOperation(MONTH, RelativeOperator.MINUS, 3));
        operations.add(new RelativeOperation(DAY, RelativeOperator.EQUAL, 17));
        operations.add(new RelativeOperation(RelativeField.HOUR, RelativeOperator.EQUAL, 9));
        operations.add(new RelativeOperation(RelativeField.MINUTES, RelativeOperator.EQUAL, 5));
        RelativeDate relativeDate = new RelativeDate(operations);

        ZonedDateTime date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusMonths(3).withDayOfMonth(17).withHour(9).withMinute(5));

        // 5 months ago on current day of month
        Instant instant = Instant.now();
        ZonedDateTime res = ZonedDateTime.ofInstant(instant, referenceTime.getZone());
        operations = new ArrayList<>();
        operations.add(new RelativeOperation(MONTH, RelativeOperator.MINUS, 5));
        operations.add(DAY.equalTo(res.getDayOfMonth()));
        relativeDate = new RelativeDate(operations);
        date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusMonths(5).withDayOfMonth(res.getDayOfMonth()));
    }

    @Test
    public void testOperationLastDayOfMonth() {
        // last day of month
        Instant instant = Instant.now();
        ZonedDateTime res = ZonedDateTime.ofInstant(instant, referenceTime.getZone());
        List<RelativeOperation> operations = new ArrayList<>();
        operations.add(new RelativeOperation(MONTH, RelativeOperator.MINUS, 11));
        operations.add(new RelativeOperation(DAY, RelativeOperator.EQUAL, 31));
        RelativeDate relativeDate = new RelativeDate(operations);
        ZonedDateTime date = relativeDate.getRelativeDate(referenceTime);
        assertThat(date).isEqualTo(referenceTime.minusMonths(11).withDayOfMonth(28));
    }

    @Test
    public void testWithNoOverlappingOperations() {
        RelativeDate relativeDate = new RelativeDate(WEEK.minus(7));

        assertThat(relativeDate.with(DAY_OF_WEEK.equalTo(4))).isEqualTo(new RelativeDate(WEEK.minus(7), DAY_OF_WEEK.equalTo(4)));
    }

    @Test
    public void testWithOverlappingOperations() {
        RelativeDate relativeDate = new RelativeDate(WEEK.minus(7), DAY_OF_WEEK.equalTo(4));

        assertThat(relativeDate.with(DAY_OF_WEEK.equalTo(3), MINUTES.equalTo(14))).isEqualTo(new RelativeDate(WEEK.minus(7), DAY_OF_WEEK.equalTo(4), DAY_OF_WEEK.equalTo(3), MINUTES.equalTo(14)));
    }

    @Test
    public void testWithOverlappingOperationsNotLast() {
        RelativeDate relativeDate = new RelativeDate(WEEK.minus(7), DAY_OF_WEEK.equalTo(4));

        assertThat(relativeDate.with(WEEK.equalTo(3), MINUTES.equalTo(14))).isEqualTo(new RelativeDate(WEEK.minus(7), DAY_OF_WEEK.equalTo(4), WEEK.equalTo(3), MINUTES.equalTo(14)));
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new RelativeDate(MONTH.minus(1), DAY.equalTo(13), HOUR.equalTo(12));
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new RelativeDate(MONTH.minus(1), DAY.equalTo(13), HOUR.equalTo(12));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new RelativeDate(MONTH.minus(1), DAY.equalTo(13), HOUR.equalTo(12), SECONDS.equalTo(5)),
                new RelativeDate(MONTH.minus(1), HOUR.equalTo(12)),
                new RelativeDate(MONTH.minus(2), DAY.equalTo(13), HOUR.equalTo(12)),
                new RelativeDate(MONTH.minus(1), DAY.equalTo(12), HOUR.equalTo(12)),
                new RelativeDate(MONTH.minus(1), DAY.equalTo(13), HOUR.equalTo(18)),
                new RelativeDate(MONTH.minus(1), HOUR.equalTo(12), DAY.equalTo(13))
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
}
