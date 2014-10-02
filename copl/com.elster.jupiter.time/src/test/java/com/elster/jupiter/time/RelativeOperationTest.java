package com.elster.jupiter.time;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.*;
import java.util.List;
import java.util.UnknownFormatConversionException;

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

    @Test(expected = UnknownFormatConversionException.class)
    public void testOperationValidationChronoUnitEqual() {
        new RelativeOperation (RelativeField.MONTH, RelativeOperator.EQUAL, 4);
    }

    @Test(expected = UnknownFormatConversionException.class)
    public void testOperationValidationChronoFieldMinus() {
        new RelativeOperation (RelativeField.DAY_IN_MONTH, RelativeOperator.MINUS, 4);
    }

    @Test(expected = UnknownFormatConversionException.class)
    public void testOperationValidationChronoFieldPlus() {
        new RelativeOperation (RelativeField.MINUTES_OF_HOUR, RelativeOperator.PLUS, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperationValidationValueRangeMinutes() {
        new RelativeOperation (RelativeField.MINUTES_OF_HOUR, RelativeOperator.EQUAL, 65);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperationValidationValueRangeDaysInMonth() {
        new RelativeOperation (RelativeField.DAY_IN_MONTH, RelativeOperator.EQUAL, 65);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperationValidationValueRangeDaysInWeek() {
        new RelativeOperation (RelativeField.DAY_IN_WEEK, RelativeOperator.EQUAL, 8);
    }
}
