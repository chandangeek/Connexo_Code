package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * Remodels a {@link com.elster.jupiter.metering.ReadingType}
 * to focus on the aspects of data aggregation and to allow
 * the data aggregation component to use ReadingTypes in intermediate
 * steps that may not have been defined in the database.
 * To avoid <code>null</code> values to deal with unsupported
 * reading types, this class introduces a static constructor
 * for the unsupported reading type and a test method to verify
 * that a VirtualReadingType is in fact that unsupported reading type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-29 (14:26)
 */
class VirtualReadingType implements Comparable<VirtualReadingType> {
    private final IntervalLength intervalLength;
    private final MetricMultiplier unitMultiplier;
    private final ReadingTypeUnit unit;

    static VirtualReadingType from(ReadingType readingType) {
        return new VirtualReadingType(IntervalLength.from(readingType), readingType.getMultiplier(), readingType.getUnit());
    }

    static VirtualReadingType from(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit) {
        return new VirtualReadingType(intervalLength, unitMultiplier, unit);
    }

    static VirtualReadingType notSupported() {
        return new VirtualReadingType(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.NOTAPPLICABLE);
    }

    private VirtualReadingType(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit) {
        this.intervalLength = intervalLength;
        this.unitMultiplier = unitMultiplier;
        this.unit = unit;
    }

    /**
     * Tests if this VirtualReadingType represents <code>null</code>.
     *
     * @return A flag that indicates if this VirtualReadingType represents <code>null</code>
     */
    boolean isUnsupported() {
        return this.intervalLength == IntervalLength.NOT_SUPPORTED
            && this.unit == ReadingTypeUnit.NOTAPPLICABLE
            && this.unitMultiplier == MetricMultiplier.ZERO;
    }

    IntervalLength getIntervalLength() {
        return intervalLength;
    }

    VirtualReadingType withIntervalLength(IntervalLength intervalLength) {
        return new VirtualReadingType(intervalLength, this.unitMultiplier, this.unit);
    }

    MetricMultiplier getUnitMultiplier() {
        return unitMultiplier;
    }

    VirtualReadingType withMetricMultiplier(MetricMultiplier unitMultiplier) {
        return new VirtualReadingType(this.intervalLength, unitMultiplier, this.unit);
    }

    ReadingTypeUnit getUnit() {
        return unit;
    }

    VirtualReadingType withUnit(ReadingTypeUnit unit) {
        return new VirtualReadingType(this.intervalLength, this.unitMultiplier, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VirtualReadingType that = (VirtualReadingType) o;
        return intervalLength == that.intervalLength &&
                unitMultiplier == that.unitMultiplier &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervalLength, unitMultiplier, unit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("intervalLength", intervalLength)
                .add("unitMultiplier", unitMultiplier)
                .add("unit", unit)
                .toString();
    }

    @Override
    public int compareTo(VirtualReadingType other) {
        int intervalLengthCompareResult = this.intervalLength.compareTo(other.intervalLength);
        if (intervalLengthCompareResult == 0) {
            // Same interval length: consider unit conversion
            int unitCompareResult = this.unit.compareTo(other.unit);
            if (unitCompareResult == 0 || UnitConversionSupport.areCompatibleForAutomaticUnitConversion(this.unit, other.unit)) {
                // Same or compatible units: consider unit multiplier
                return this.unitMultiplier.compareTo(other.unitMultiplier);
            }
            else {
                /* Not the same unit or incompatible units,
                 * is this an error of the matching algorithm? */
                return unitCompareResult;
            }
        }
        else {
            return intervalLengthCompareResult;
        }
    }

}