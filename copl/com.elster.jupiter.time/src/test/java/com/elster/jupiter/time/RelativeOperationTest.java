package com.elster.jupiter.time;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RelativeOperationTest {
    ZonedDateTime referenceTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

    @Test
    public void testPerformOperation() {
        // 17-10-2014 9:05
        ZonedDateTime providedDate = ZonedDateTime.of(2014, 10, 17, 9, 5, 0, 0, referenceTime.getZone());

        List<RelativeOperation> relativeOperations = RelativeOperation.from(providedDate);
        ZonedDateTime relativeDate = referenceTime;
        for(RelativeOperation operation : relativeOperations) {
            relativeDate = operation.performOperation(relativeDate);
        }
        assertThat(relativeDate.equals(providedDate)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperationValidationValueRangeMinutes() {
        new RelativeOperation (RelativeField.MINUTES, RelativeOperator.EQUAL, 65);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperationValidationValueRangeDaysInMonth() {
        new RelativeOperation (RelativeField.DAY, RelativeOperator.EQUAL, 65);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperationValidationValueRangeDaysInWeek() {
        new RelativeOperation (RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL, 8);
    }
}
