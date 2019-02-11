package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;

import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CASConflictsSolverTest {

    private static final Instant INSTANT_GAP_BEFORE = new GregorianCalendar(2004, 7, 23).toInstant();
    private static final Instant INSTANT_GAP_AFTER = new GregorianCalendar(2000, 5, 19).toInstant();
    private static final Instant VERSION_ID = new GregorianCalendar(2003, 4, 14).toInstant();

    private static final Instant LATE_DATE = new GregorianCalendar(2008, 5, 19).toInstant();
    private static final Instant EARLY_DATE = new GregorianCalendar(1980, 5, 19).toInstant();

    private CASConflictsSolver sut;

    @Mock
    private CustomPropertySetService customPropertySetService;

    @SuppressWarnings("rawtypes")
    @Mock
    private CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet;

    @Mock
    private Device device;

    private CustomPropertySetInfo newCustomProperySetInfo;

    @Mock
    private OverlapCalculatorBuilder overlapCalculatorBuilder;

    @Mock
    private ValuesRangeConflict conflictGapAfter;

    @Mock
    private ValuesRangeConflict conflictGapBefore;

    private Optional<Instant> startTime;

    private Optional<Instant> endTime;

    private CustomPropertySetValues existingValues;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        sut = new CASConflictsSolver(customPropertySetService);
        newCustomProperySetInfo = new CustomPropertySetInfo();
        when(customPropertySetService.calculateOverlapsFor(customPropertySet, device))
                .thenReturn(overlapCalculatorBuilder);

        when(conflictGapAfter.getType()).thenReturn(ValuesRangeConflictType.RANGE_GAP_AFTER);
        when(conflictGapAfter.getConflictingRange()).thenReturn(Range.atLeast(INSTANT_GAP_AFTER));

        when(conflictGapBefore.getType()).thenReturn(ValuesRangeConflictType.RANGE_GAP_BEFORE);
        when(conflictGapBefore.getConflictingRange()).thenReturn(Range.atMost(INSTANT_GAP_BEFORE));

        existingValues = CustomPropertySetValues.emptyDuring(Range.closedOpen(EARLY_DATE, LATE_DATE));
        startTime = Optional.empty();
        endTime = Optional.empty();
    }

    private void prepareConflicts(ValuesRangeConflict... conflicts) {
        doReturn(Arrays.asList(conflicts)).when(overlapCalculatorBuilder).whenCreating(any(Range.class));
    }

    @Test
    public void testSolveConflictsForCreate_NoConflicts() {
        prepareConflicts();
        newCustomProperySetInfo.setFromDate(EARLY_DATE);
        newCustomProperySetInfo.setEndDate(LATE_DATE);

        Range<Instant> result = sut.solveConflictsForCreate(device, customPropertySet, newCustomProperySetInfo);

        assertEquals(Range.closedOpen(EARLY_DATE, LATE_DATE), result);
    }

    @Test
    public void testSolveConflictsForCreate_GapAfter_EndTimeIsNull() {
        prepareConflicts(conflictGapAfter);

        Range<Instant> result = sut.solveConflictsForCreate(device, customPropertySet, newCustomProperySetInfo);

        assertEquals(Range.atLeast(INSTANT_GAP_AFTER), result);
    }

    @Test
    public void testSolveConflictsForCreate_GapAfter_EndTimeIsNotNull() {
        prepareConflicts(conflictGapAfter);
        newCustomProperySetInfo.setEndDate(LATE_DATE);

        Range<Instant> result = sut.solveConflictsForCreate(device, customPropertySet, newCustomProperySetInfo);

        assertEquals(Range.closedOpen(INSTANT_GAP_AFTER, LATE_DATE), result);
    }

    @Test
    public void testSolveConflictsForCreate_GapBefore_EndTimeIsNull() {
        prepareConflicts(conflictGapBefore);

        Range<Instant> result = sut.solveConflictsForCreate(device, customPropertySet, newCustomProperySetInfo);

        assertEquals(Range.lessThan(INSTANT_GAP_BEFORE), result);
    }

    @Test
    public void testSolveConflictsForCreate_GapBefore_EndTimeIsNotNull() {
        prepareConflicts(conflictGapBefore);
        newCustomProperySetInfo.setFromDate(EARLY_DATE);

        Range<Instant> result = sut.solveConflictsForCreate(device, customPropertySet, newCustomProperySetInfo);

        assertEquals(Range.closedOpen(EARLY_DATE, INSTANT_GAP_BEFORE), result);
    }

    @Test
    public void testSolveConflictsForUpdate_NoConflicts() {
        prepareConflicts();

        Range<Instant> result = sut.solveConflictsForUpdate(device, customPropertySet, startTime, endTime, VERSION_ID,
                existingValues);

        assertEquals(Range.closedOpen(EARLY_DATE, LATE_DATE), result);
    }

}
