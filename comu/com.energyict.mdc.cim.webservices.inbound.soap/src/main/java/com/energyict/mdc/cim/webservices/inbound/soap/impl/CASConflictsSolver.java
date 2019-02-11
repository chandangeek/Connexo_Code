package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.util.Ranges;

import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

public class CASConflictsSolver {

    private final CustomPropertySetService customPropertySetService;

    public CASConflictsSolver(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    public Range<Instant> solveConflictsForCreate(Device businessObject,
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
            CustomPropertySetInfo newCustomProperySetInfo) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService
                .calculateOverlapsFor(customPropertySet, businessObject);
        Optional<Instant> startTime = Optional.ofNullable(newCustomProperySetInfo.getFromDate());
        Optional<Instant> endTime = Optional.ofNullable(newCustomProperySetInfo.getEndDate());
        Range<Instant> range = Ranges.closedOpen(newCustomProperySetInfo.getFromDate(),
                newCustomProperySetInfo.getEndDate());
        for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenCreating(range)) {
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                range = getRangeToCreate(Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()), endTime);
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                range = getRangeToCreate(startTime,
                        Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()));
            }
        }
        return range;
    }

    public Range<Instant> solveConflictsForUpdate(Device device,
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
            Optional<Instant> startTime, Optional<Instant> endTime, Instant versionId,
            CustomPropertySetValues existingValues) {
        Range<Instant> range = getRangeToUpdate(startTime, endTime, existingValues.getEffectiveRange());
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService
                .calculateOverlapsFor(customPropertySet, device);

        for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenUpdating(versionId, range)) {
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                range = getRangeToUpdate(Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()), endTime,
                        existingValues.getEffectiveRange());
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                range = getRangeToUpdate(startTime, Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()),
                        existingValues.getEffectiveRange());
            }
        }
        return range;
    }

    private Range<Instant> getRangeToCreate(Optional<Instant> startTimeOptional, Optional<Instant> endTimeOptional) {
        if (notDefinedOrIsInfinite(startTimeOptional) && notDefinedOrIsInfinite(endTimeOptional)) {
            return Range.all();
        } else if (notDefinedOrIsInfinite(startTimeOptional) && isNotInfinite(endTimeOptional)) {
            return Range.lessThan(endTimeOptional.get());
        } else if (notDefinedOrIsInfinite(endTimeOptional)) {
            return Range.atLeast(startTimeOptional.get());
        } else {
            return Range.closedOpen(startTimeOptional.get(), endTimeOptional.get());
        }
    }

    private Range<Instant> getRangeToUpdate(Optional<Instant> startTime, Optional<Instant> endTime,
            Range<Instant> oldRange) {
        if (!startTime.isPresent() && !endTime.isPresent()) {
            return oldRange;
        } else if (!startTime.isPresent()) {
            if (oldRange.hasLowerBound()) {
                if (!endTime.get().equals(Instant.EPOCH)) {
                    return Range.closedOpen(oldRange.lowerEndpoint(), endTime.get());
                } else {
                    return Range.atLeast(oldRange.lowerEndpoint());
                }
            } else if (endTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.lessThan(endTime.get());
            }
        } else if (!endTime.isPresent()) {
            if (oldRange.hasUpperBound()) {
                if (!startTime.get().equals(Instant.EPOCH)) {
                    return Range.closedOpen(startTime.get(), oldRange.upperEndpoint());
                } else {
                    return Range.lessThan(oldRange.upperEndpoint());
                }
            } else if (startTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.atLeast(startTime.get());
            }
        } else {
            return getRangeToCreate(startTime, endTime);
        }
    }

    private boolean isNotInfinite(Optional<Instant> endTime) {
        return endTime.isPresent() && !endTime.get().equals(Instant.EPOCH);
    }

    private boolean notDefinedOrIsInfinite(Optional<Instant> dateTime) {
        return !dateTime.isPresent() || dateTime.get().equals(Instant.EPOCH);
    }
}