/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RelativeOperationTest extends EqualsContractTest {
    ZonedDateTime referenceTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

    private RelativeOperation instanceA = new RelativeOperation(RelativeField.DAY, RelativeOperator.PLUS, 2);

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
    public void testOperationValidationChronoUnit() {
        new RelativeOperation (RelativeField.MONTH, RelativeOperator.MINUS, -4);
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

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new RelativeOperation(RelativeField.DAY, RelativeOperator.PLUS, 2);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, 2),
                new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.PLUS, 2),
                new RelativeOperation(RelativeField.DAY, RelativeOperator.PLUS, 3)
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        //TODO automatically generated method body, provide implementation.
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
