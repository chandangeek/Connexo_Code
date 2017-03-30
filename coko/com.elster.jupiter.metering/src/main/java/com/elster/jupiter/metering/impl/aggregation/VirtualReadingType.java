/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.max;
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
    private final Accumulation accumulation;
    private final Commodity commodity;
    private final int timeOfUseBucket;
    private final Marker marker;

    static VirtualReadingType from(ReadingType readingType) {
        return from(IntervalLength.from(readingType), readingType.getMultiplier(), readingType.getUnit(), readingType.getAccumulation(), readingType.getCommodity(), readingType.getTou());
    }

    static VirtualReadingType from(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit, Accumulation accumulation, Commodity commodity) {
        return new VirtualReadingType(intervalLength, unitMultiplier, unit, accumulation, commodity, 0, null);
    }

    static VirtualReadingType from(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit, Accumulation accumulation, Commodity commodity, int timeOfUse) {
        return new VirtualReadingType(intervalLength, unitMultiplier, unit, accumulation, commodity, timeOfUse, null);
    }

    static VirtualReadingType from(IntervalLength intervalLength, Dimension dimension, Accumulation accumulation, Commodity commodity) {
        return from(intervalLength, MetricMultiplier.ZERO, readingTypeUnitFrom(dimension, commodity), accumulation, commodity, 0);
    }

    private static ReadingTypeUnit readingTypeUnitFrom(Dimension dimension, Commodity commodity) {
        if (isElectricalEnergy(dimension, commodity)) {
            return ReadingTypeUnit.WATTHOUR;
        } else {
            return Stream
                    .of(ReadingTypeUnit.values())
                    .filter(readingTypeUnit -> readingTypeUnit.getUnit().getDimension().equals(dimension))
                    .findAny()
                    .orElse(ReadingTypeUnit.NOTAPPLICABLE);
        }
    }

    private static boolean isElectricalEnergy(Dimension dimension, Commodity commodity) {
        return Dimension.ENERGY.equals(dimension)
                && isElectricity(commodity);
    }

    public boolean isElectricity() {
        return isElectricity(this.commodity);
    }

    private static boolean isElectricity(Commodity commodity) {
        return Commodity.ELECTRICITY_PRIMARY_METERED.equals(commodity)
                || Commodity.ELECTRICITY_SECONDARY_METERED.equals(commodity);
    }

    public boolean isGas() {
        return isGas(this.commodity);
    }

    public static boolean isGas(Commodity commodity) {
        return Commodity.NATURALGAS.equals(commodity);
    }

    static VirtualReadingType notSupported() {
        return new VirtualReadingType(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.NOTAPPLICABLE, null, null, 0, Marker.UNSUPPORTED);
    }

    static VirtualReadingType dontCare() {
        return new VirtualReadingType(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.NOTAPPLICABLE, null, null, 0, Marker.DONTCARE);
    }

    private VirtualReadingType(IntervalLength intervalLength, MetricMultiplier unitMultiplier, ReadingTypeUnit unit, Accumulation accumulation, Commodity commodity, int timeOfUseBucket, Marker marker) {
        this.intervalLength = intervalLength;
        this.unitMultiplier = unitMultiplier;
        this.unit = unit;
        this.accumulation = accumulation;
        this.commodity = commodity;
        this.timeOfUseBucket = timeOfUseBucket;
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

    boolean isPrimaryMetered() {
        return this.commodity != null && this.commodity.equals(Commodity.ELECTRICITY_PRIMARY_METERED);
    }

    boolean isSecondaryMetered() {
        return this.commodity != null && this.commodity.equals(Commodity.ELECTRICITY_SECONDARY_METERED);
    }

    boolean isRegular() {
        return !this.intervalLength.equals(IntervalLength.NOT_SUPPORTED);
    }

    IntervalLength getIntervalLength() {
        return intervalLength;
    }

    VirtualReadingType withIntervalLength(IntervalLength intervalLength) {
        return new VirtualReadingType(intervalLength, this.unitMultiplier, this.unit, this.accumulation, this.commodity, this.timeOfUseBucket, this.marker);
    }

    MetricMultiplier getUnitMultiplier() {
        return unitMultiplier;
    }

    VirtualReadingType withMetricMultiplier(MetricMultiplier unitMultiplier) {
        return new VirtualReadingType(this.intervalLength, unitMultiplier, this.unit, this.accumulation, this.commodity, this.timeOfUseBucket, this.marker);
    }

    ReadingTypeUnit getUnit() {
        return unit;
    }

    VirtualReadingType withUnit(ReadingTypeUnit unit) {
        return new VirtualReadingType(this.intervalLength, this.unitMultiplier, unit, this.accumulation, this.commodity, this.timeOfUseBucket, this.marker);
    }

    Accumulation getAccumulation() {
        return accumulation;
    }

    Commodity getCommodity() {
        return this.commodity;
    }

    VirtualReadingType withCommondity(Commodity commondity) {
        return new VirtualReadingType(this.intervalLength, this.unitMultiplier, this.unit, this.accumulation, commondity, this.timeOfUseBucket, this.marker);
    }

    Dimension getDimension() {
        return this.getUnit().getUnit().getDimension();
    }

    VirtualReadingType withDimension(Dimension dimension) {
        return this.withUnit(readingTypeUnitFrom(dimension, this.commodity));
    }

    int getTimeOfUseBucket() {
        return timeOfUseBucket;
    }

    VirtualReadingType withTimeOfUseBucketIfNotNull(int timeOfUseBucket) {
        return new VirtualReadingType(this.intervalLength, this.unitMultiplier, this.unit, this.accumulation, this.commodity, max(this.timeOfUseBucket, timeOfUseBucket), this.marker);
    }

    /**
     * Builds and returns the appropriate SQL constructs to achieve unit conversion for the specified
     * expression from this VirtualReadingType to the specified target VirtualReadingType.
     *
     * @param mode The Mode
     * @param expression The expression
     * @param targetReadingType The target VirtualReadingType
     */
    SqlFragment buildSqlUnitConversion(Formula.Mode mode, SqlFragment expression, VirtualReadingType targetReadingType) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        if (this.isDontCare()) {
            return expression;
        } else if (this.getUnit().equals(targetReadingType.getUnit())) {
            // Unit is the same, consider multiplier
            if (this.getUnitMultiplier().equals(targetReadingType.getUnitMultiplier())) {
                // Same multiplier, just append the expression and we're done
                return expression;
            } else {
                Loggers.SQL.debug(() -> "Rescaling " + expression + " from " + this.getUnitMultiplier() + " to " + targetReadingType.getUnitMultiplier());
                sqlBuilder.append("(");
                sqlBuilder.add(expression);
                sqlBuilder.append(" * ");
                BigDecimal multiplierConversionFactor = ONE.scaleByPowerOfTen(this.getUnitMultiplier().getMultiplier() - targetReadingType.getUnitMultiplier().getMultiplier());
                sqlBuilder.append(multiplierConversionFactor.toString());
                sqlBuilder.append(")");
            }
        } else if (UnitConversionSupport.areCompatibleForAutomaticUnitConversion(this.getUnit(), targetReadingType.getUnit())) {
            this.applyUnitConversion(mode, expression, targetReadingType, sqlBuilder);
        } else if (mode.equals(Formula.Mode.EXPERT)) {
            return expression;
        } else {
            throw new UnsupportedOperationException("Unsuported unit conversion from " + this + " to " + targetReadingType);
        }
        return sqlBuilder;
    }

    /**
     * Builds and returns the appropriate SQL constructs to achieve unit conversion for the specified
     * expression from this VirtualReadingType to the specified target VirtualReadingType.
     *
     * @param mode The Mode
     * @param expression The expression
     * @param targetReadingType The target VirtualReadingType
     */
    String buildSqlUnitConversion(Formula.Mode mode, String expression, VirtualReadingType targetReadingType) {
        return buildSqlUnitConversion(mode, new TextFragment(expression), targetReadingType).getText();
    }

    private void applyUnitConversion(Formula.Mode mode, SqlFragment expression, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
        if (this.isFlowRelated() && targetReadingType.isVolumeRelated()) {
            this.applyFlowToVolumeConversion(expression, targetReadingType, sqlBuilder);
        } else if (this.isVolumeRelated() && targetReadingType.isFlowRelated()) {
            this.applyVolumeToFlowConversion(expression, targetReadingType, sqlBuilder);
        } else {
            ServerExpressionNode conversionExpression =
                    UnitConversionSupport.unitConversion(
                            new SqlFragmentNode(expression),
                            this.getUnit(),
                            this.getUnitMultiplier(),
                            targetReadingType.getUnit(),
                            targetReadingType.getUnitMultiplier());
            String convertedExpression = conversionExpression.accept(new ExpressionNodeToString(mode));
            Loggers.SQL.debug(() -> "Applying unit conversion to " + expression + " to convert from " + this.toString() + " to " + targetReadingType.toString() + " using: " + convertedExpression);
            sqlBuilder.append(convertedExpression);
        }
    }

    private void applyVolumeToFlowConversion(SqlFragment expression, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
        Loggers.SQL.debug(() -> "Applying volume to flow conversion to " + expression + " to convert from " + this.toString() + " to " + targetReadingType.toString());
        sqlBuilder.append("(");
        sqlBuilder.add(expression);
        sqlBuilder.append(" * ");
        BigDecimal intervalConversionFactor = this.getIntervalLength().getVolumeFlowConversionFactor();
        if (!this.getUnitMultiplier().equals(targetReadingType.getUnitMultiplier())) {
            BigDecimal multiplierConversionFactor = ONE.scaleByPowerOfTen(this.getUnitMultiplier().getMultiplier() - targetReadingType.getUnitMultiplier().getMultiplier());
            sqlBuilder.append(intervalConversionFactor.multiply(multiplierConversionFactor).toString());
        } else {
            sqlBuilder.append(intervalConversionFactor.toString());
        }
        sqlBuilder.append(")");
    }

    private void applyFlowToVolumeConversion(SqlFragment expression, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
        Loggers.SQL.debug(() -> "Applying flow to volume conversion to " + expression + " to convert from " + this.toString() + " to " + targetReadingType.toString());
        boolean withRescaling = !this.getUnitMultiplier().equals(targetReadingType.getUnitMultiplier());
        if (withRescaling) {
            sqlBuilder.append("((");
        } else {
            sqlBuilder.append("(");
        }
        sqlBuilder.add(expression);
        sqlBuilder.append(" / ");
        BigDecimal intervalConversionFactor = this.getIntervalLength().getVolumeFlowConversionFactor();
        sqlBuilder.append(intervalConversionFactor.toString());
        if (withRescaling) {
            sqlBuilder.append(") * ");
            BigDecimal multiplierConversionFactor = ONE.scaleByPowerOfTen(this.getUnitMultiplier().getMultiplier() - targetReadingType.getUnitMultiplier().getMultiplier());
            sqlBuilder.append(multiplierConversionFactor.toString());
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
                commodity == that.commodity &&
                marker == that.marker;
    }

    boolean equalsIgnoreCommodity(Object o) {
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
        return Objects.hash(intervalLength, unitMultiplier, unit, commodity, marker);
    }

    @Override
    public String toString() {
        if (this.isDontCare()) {
            return "DONT_CARE";
        }
        if (this.isUnsupported()) {
            return "UNSUPPORTED";
        } else {
            return MoreObjects.toStringHelper(this)
                    .add("intervalLength", intervalLength)
                    .add("unitMultiplier", unitMultiplier)
                    .add("unit", unit)
                    .add("commodity", commodity)
                    .add("tou", timeOfUseBucket)
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
            } else {
                /* Not the same unit or incompatible units,
                 * is this an error of the matching algorithm? */
                return unitCompareResult;
            }
        } else {
            return intervalLengthCompareResult;
        }
    }

    private enum Marker {
        UNSUPPORTED, DONTCARE;
    }

}