/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class OverlapCalculatorBuilderImpl implements OverlapCalculatorBuilder {

    private final Thesaurus thesaurus;
    private final List<CustomPropertySetValues> customPropertySetValues;

    OverlapCalculatorBuilderImpl(List<CustomPropertySetValues> customPropertySetValues, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.customPropertySetValues = Collections.unmodifiableList(customPropertySetValues);
    }

    @Override
    public List<ValuesRangeConflict> whenCreating(Range<Instant> newRange){
        return getConflicts(newRange, customPropertySetValues.stream());
    }

    @Override
    public List<ValuesRangeConflict> whenUpdating(Instant existingRangeStart, Range<Instant> newRange){
        return getConflicts(
                newRange,
                customPropertySetValues
                        .stream()
                        .filter(value -> !value.getEffectiveRange().contains(existingRangeStart)));
    }

    private List<ValuesRangeConflict> getConflicts(Range<Instant> newRange, Stream<CustomPropertySetValues> existingOtherValues) {
        List<ValuesRangeConflict> issues = new ArrayList<>();
        List<Range<Instant>> rangesAfterUpdate = new ArrayList<>();

        existingOtherValues.forEach(value -> {
            Range<Instant> r = value.getEffectiveRange();
            if (hasOverlap(r,newRange)) {
                if (newRange.encloses(r)) {
                    issues.add(getValuesRangeConflictDelete(value, getConflictingRangeOverlap(r, newRange)));
                } else if (hasEndOverlap(r, newRange)) {
                    if (!r.hasLowerBound()) {
                        rangesAfterUpdate.add(Range.lessThan(newRange.lowerEndpoint()));
                    } else{
                        rangesAfterUpdate.add(Range.closedOpen(r.lowerEndpoint(),newRange.lowerEndpoint()));
                    }
                    issues.add(getValuesRangeConflictUpdateEnd(value, getConflictingRangeOverlap(r,newRange)));
                } else if (hasStartOverlap(r,newRange)) {
                    if (!r.hasUpperBound()) {
                        rangesAfterUpdate.add(Range.atLeast(newRange.upperEndpoint()));
                    } else {
                        rangesAfterUpdate.add(Range.closedOpen(newRange.upperEndpoint(),r.upperEndpoint()));
                    }
                    issues.add(getValuesRangeConflictUpdateStart(value, getConflictingRangeOverlap(r,newRange)));
                } else {
                    issues.add(getValuesRangeConflictDelete(value, getConflictingRangeOverlap(r,newRange)));
                }
            } else {
                rangesAfterUpdate.add(r);
            }
        });

        rangesAfterUpdate.add(newRange);

        issues.addAll(getGaps(rangesAfterUpdate, newRange));

        if (!issues.isEmpty()) {
            issues.add(getValuesRangeInsertedValue(CustomPropertySetValues.emptyDuring(Interval.of(newRange)),newRange));
        }

        return issues;
    }

    private List<ValuesRangeConflict> getGaps(List<Range<Instant>> ranges, Range<Instant> newRange){
        List<ValuesRangeConflict> issues = new ArrayList<>();
        Collections.sort(ranges, getRangeComparator());
        for (int i = 0; i < ranges.size() - 1; i++) {
            if (!ranges.get(i).isConnected(ranges.get(i + 1))) {
                if (ranges.get(i).equals(newRange)) {
                    issues.add(getValuesRangeConflictGapBefore(getValuesFor(ranges.get(i+1)),getConflictingRangeGap(ranges.get(i),ranges.get(i+1))));
                } else {
                    issues.add(getValuesRangeConflictGapAfter(getValuesFor(ranges.get(i)),getConflictingRangeGap(ranges.get(i),ranges.get(i+1))));
                }
            }
        }
        return issues;
    }

    private ValuesRangeConflict getValuesRangeConflictUpdateStart(CustomPropertySetValues values, Range<Instant> conflictingRange){
        return new ValuesRangeConflictImpl(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START,
                conflictingRange, values, TranslationKeys.RANGE_OVERLAP_UPDATE_START, thesaurus);
    }

    private ValuesRangeConflict getValuesRangeConflictUpdateEnd(CustomPropertySetValues values, Range<Instant> conflictingRange){
        return new ValuesRangeConflictImpl(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END,
                                            conflictingRange, values, TranslationKeys.RANGE_OVERLAP_UPDATE_END, thesaurus);
    }

    private ValuesRangeConflict getValuesRangeConflictDelete(CustomPropertySetValues values, Range<Instant> conflictingRange){
        return new ValuesRangeConflictImpl(ValuesRangeConflictType.RANGE_OVERLAP_DELETE,
                conflictingRange, values, TranslationKeys.RANGE_OVERLAP_DELETE, thesaurus);
    }

    private ValuesRangeConflict getValuesRangeConflictGapBefore(CustomPropertySetValues values, Range<Instant> conflictingRange){
        return new ValuesRangeConflictImpl(ValuesRangeConflictType.RANGE_GAP_BEFORE,
                conflictingRange, values, TranslationKeys.RANGE_GAP_BEFORE, thesaurus);
    }

    private ValuesRangeConflict getValuesRangeConflictGapAfter(CustomPropertySetValues values, Range<Instant> conflictingRange){
        return new ValuesRangeConflictImpl(ValuesRangeConflictType.RANGE_GAP_AFTER,
                conflictingRange, values, TranslationKeys.RANGE_GAP_AFTER, thesaurus);
    }

    private ValuesRangeConflict getValuesRangeInsertedValue(CustomPropertySetValues values, Range<Instant> conflictingRange){
        return new ValuesRangeConflictImpl(ValuesRangeConflictType.RANGE_INSERTED,
                conflictingRange, values, TranslationKeys.RANGE_INSERT, thesaurus);
    }

    private Range<Instant> getConflictingRangeOverlap(Range<Instant> firstRange, Range<Instant> secondRange){
           return firstRange.intersection(secondRange);
    }

    private Range<Instant> getConflictingRangeGap (Range<Instant> firstRange, Range<Instant> secondRange){
        return Objects.compare(firstRange, secondRange, getRangeComparator())<0
                ? Range.closedOpen(firstRange.upperEndpoint(), secondRange.lowerEndpoint())
                : Range.closedOpen(secondRange.upperEndpoint(), firstRange.lowerEndpoint());
    }

    private boolean hasOverlap(Range<Instant> oldRange, Range<Instant> newRange){
        return oldRange.isConnected(newRange) && !oldRange.intersection(newRange).isEmpty();
    }

    private boolean hasStartOverlap(Range<Instant> oldRange, Range<Instant> newRange) {
        return newRange.hasUpperBound()
                && ((oldRange.contains(newRange.upperEndpoint()) && !oldRange.encloses(newRange))
                || (oldRange.hasUpperBound() && newRange.upperEndpoint().isBefore(oldRange.upperEndpoint()) && oldRange.encloses(newRange))
                || (!oldRange.hasUpperBound() && (!oldRange.hasLowerBound() || (oldRange.hasLowerBound() && oldRange.lowerEndpoint().isBefore(newRange.upperEndpoint())))));
    }

    private boolean hasEndOverlap(Range<Instant> oldRange, Range<Instant> newRange) {
        return newRange.hasLowerBound()
                && ((oldRange.contains(newRange.lowerEndpoint()) && !oldRange.encloses(newRange))
                || (oldRange.hasLowerBound() && newRange.lowerEndpoint().isAfter(oldRange.lowerEndpoint()) && oldRange.encloses(newRange))
                || (!oldRange.hasLowerBound() && (!oldRange.hasUpperBound() || (oldRange.hasUpperBound() && oldRange.upperEndpoint().isAfter(newRange.lowerEndpoint())))));
    }

    private Comparator<Range<Instant>> getRangeComparator(){
        return (Range<Instant> a, Range<Instant> b) ->
                a.hasLowerBound() && b.hasLowerBound() ? a.lowerEndpoint().compareTo(b.lowerEndpoint()) : Boolean.compare(a.hasLowerBound(), b.hasLowerBound());
    }

    private CustomPropertySetValues getValuesFor(Range<Instant>range){
        return customPropertySetValues.stream().filter(values -> values.getEffectiveRange().equals(range)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}