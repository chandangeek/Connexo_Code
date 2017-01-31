/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.Arrays;

/**
 * Tests equals contract for {@link DayMonthTime}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (13:42)
 */
public class DayMonthTimeEqualsContractTest extends EqualsContractTest {
    private static DayMonthTime INSTANCE_A = DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 40));

    @Override
    protected Object getInstanceA() {
        return INSTANCE_A;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 40, 0, 0));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                    DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 39, 59, 999999)),
                    DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 40, 1)),
                    DayMonthTime.fromMidnight(MonthDay.of(Month.MAY, 2)),
                    DayMonthTime.fromMidnight(MonthDay.of(Month.JULY, 15)),
                    DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(0, 30)),
                    DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(11, 30)));
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