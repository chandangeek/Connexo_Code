package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Objects;

import static java.math.BigDecimal.ONE;

/**
 * Remodels a {@link com.elster.jupiter.metering.ReadingType}
 * to focus on the aspects of data aggregation and to allow
 * the data aggregation component to use ReadingTypes in intermediate
 * steps that may not have been defined in the database.
 * There are two "marker" VirtualReadingType that were both
 * introduced to avoid <code>null</code> values.
 * The first supports components to return a marker that no
 * VirtualReadingType can be supported,
 * the second supports components to return a marker that they
 * do not care which VirtualReadingType is used.
 * This class introduces a static constructor and a test method
 * for each of these markers.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-29 (14:26)
 */
class VirtualReadingType implements Comparable<VirtualReadingType> {
    private final IntervalLength intervalLength;
    private final MetricMultiplier unitMultiplier;
    private final ReadingTypeUnit unit;
    private Marker marker;

    static VirtualReadingType from(ReadingType readingType) {
        return from(IntervalLength.from(readingType), readingType.getMultiplier(), readingType.getUnit());
    }

    static VirtualReadingType from(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit) {
        return new VirtualReadingType(intervalLength, unitMultiplier, unit, null);
    }

    static VirtualReadingType notSupported() {
        return new VirtualReadingType(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.NOTAPPLICABLE, Marker.UNSUPPORTED);
    }

    static VirtualReadingType dontCare() {
        return new VirtualReadingType(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.NOTAPPLICABLE, Marker.DONTCARE);
    }

    private VirtualReadingType(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit, Marker marker) {
        this.intervalLength = intervalLength;
        this.unitMultiplier = unitMultiplier;
        this.unit = unit;
        this.marker = marker;
    }

    /**
     * Tests if this VirtualReadingType is the marker for an unsupported reading type.
     *
     * @return <code>true</code> iff this VirtualReadingType is the marker for an unsupported reading type
     */
    boolean isUnsupported() {
        return this.marker != null && this.marker.equals(Marker.UNSUPPORTED);
    }

    /**
     * Tests if this VirtualReadingType is the marker for no preferred reading type.
     *
     * @return <code>true</code> iff this VirtualReadingType is the marker for no preffered reading type
     */
    boolean isDontCare() {
        return this.marker != null && this.marker.equals(Marker.DONTCARE);
    }

    AggregationFunction aggregationFunction() {
        if (this.isFlowRelated()) {
            return AggregationFunction.AVG;
        } else if (this.isVolumeRelated()) {
            return AggregationFunction.SUM;
        } else if (this.isTemperatureRelated() || this.isPressureRelated()) {
            return AggregationFunction.AVG;
        } else {
            return AggregationFunction.SUM;
        }
    }

    boolean isFlowRelated() {
        return UnitConversionSupport.isFlowRelated(this.getUnit());
    }

    boolean isVolumeRelated() {
        return UnitConversionSupport.isVolumeRelated(this.getUnit());
    }

    boolean isTemperatureRelated() {
        return UnitConversionSupport.isTemperatureRelated(this.getUnit());
    }

    boolean isPressureRelated() {
        return UnitConversionSupport.isPressureRelated(this.getUnit());
    }

    IntervalLength getIntervalLength() {
        return intervalLength;
    }

    VirtualReadingType withIntervalLength(IntervalLength intervalLength) {
        return new VirtualReadingType(intervalLength, this.unitMultiplier, this.unit, this.marker);
    }

    MetricMultiplier getUnitMultiplier() {
        return unitMultiplier;
    }

    VirtualReadingType withMetricMultiplier(MetricMultiplier unitMultiplier) {
        return new VirtualReadingType(this.intervalLength, unitMultiplier, this.unit, this.marker);
    }

    ReadingTypeUnit getUnit() {
        return unit;
    }

    VirtualReadingType withUnit(ReadingTypeUnit unit) {
        return new VirtualReadingType(this.intervalLength, this.unitMultiplier, unit, this.marker);
    }

    /**
     * Builds and returns the appropriate SQL constructs to achieve unit conversion for the specified
     * expression from this VirtualReadingType to the specified target VirtualReadingType.
     *
     * @param expression The expression
     * @param targetReadingType The target VirtualReadingType
     */
    String buildSqlUnitConversion(String expression, VirtualReadingType targetReadingType) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (this.getUnit().equals(targetReadingType.getUnit())) {
            // Unit is the same, consider multiplier
            if (this.getUnitMultiplier().equals(targetReadingType.getUnitMultiplier())) {
                // Same multiplier, just append the expression and we're done
                sqlBuilder.append(expression);
            }
            else {
                sqlBuilder.append("(");
                sqlBuilder.append(expression);
                sqlBuilder.append(" * ");
                BigDecimal multiplierConversionFactor = ONE.scaleByPowerOfTen(targetReadingType.getUnitMultiplier().getMultiplier() - this.getUnitMultiplier().getMultiplier());
                sqlBuilder.append(multiplierConversionFactor.toString());
                sqlBuilder.append(")");
            }
        }
        else if (UnitConversionSupport.areCompatibleForAutomaticUnitConversion(this.getUnit(), targetReadingType.getUnit())) {
            this.applyUnitConversion(expression, targetReadingType, sqlBuilder);
        }
        else {
            throw new UnsupportedOperationException("Unsuported unit conversion from " + this + " to " + targetReadingType);
        }
        return sqlBuilder.toString();
    }

    private void applyUnitConversion(String expression, VirtualReadingType targetReadingType, StringBuilder sqlBuilder) {
        if (this.isFlowRelated() && targetReadingType.isVolumeRelated()) {
            this.applyVolumeFlowConversion(expression, " / ", targetReadingType, sqlBuilder);
        }
        else if (this.isVolumeRelated() && targetReadingType.isFlowRelated()) {
            this.applyVolumeFlowConversion(expression, " * ", targetReadingType, sqlBuilder);
        }
        else {
            ServerExpressionNode conversionExpression =
                    UnitConversionSupport.unitConversion(
                            new VariableReferenceNode(expression),
                            this.getUnit(),
                            targetReadingType.getUnit());
            sqlBuilder.append(conversionExpression.accept(new ExpressionNodeToString()));
        }
    }

    private void applyVolumeFlowConversion(String expression, String operator, VirtualReadingType targetReadingType, StringBuilder sqlBuilder) {
        sqlBuilder.append("(");
        sqlBuilder.append(expression);
        sqlBuilder.append(operator);
        BigDecimal intervalConversionFactor = this.getIntervalLength().getVolumeFlowConversionFactor();
        if (!this.getUnitMultiplier().equals(targetReadingType.getUnitMultiplier())) {
            BigDecimal multiplierConversionFactor = ONE.scaleByPowerOfTen(targetReadingType.getUnitMultiplier().getMultiplier() - this.getUnitMultiplier().getMultiplier());
            sqlBuilder.append(intervalConversionFactor.multiply(multiplierConversionFactor).toString());
        }
        else {
            sqlBuilder.append(intervalConversionFactor.toString());
        }
        sqlBuilder.append(")");
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
                unit == that.unit &&
                marker == that.marker;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervalLength, unitMultiplier, unit, marker);
    }

    @Override
    public String toString() {
        if (this.isDontCare()) {
            return "DONT_CARE";
        } if (this.isUnsupported()) {
            return "UNSUPPORTED";
        } else {
            return MoreObjects.toStringHelper(this)
                    .add("intervalLength", intervalLength)
                    .add("unitMultiplier", unitMultiplier)
                    .add("unit", unit)
                    .toString();
        }
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

    private enum Marker {
        UNSUPPORTED, DONTCARE;
    }

}