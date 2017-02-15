/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Dimension;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

/**
 * Provides support for unit conversion as part of data aggregation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-01 (11:12)
 */
public class UnitConversionSupport {

    /**
     * Tests if both {@link ReadingTypeUnit}s are compatible for unit conversion.
     *
     * @param first The first ReadingTypeUnit
     * @param second The second ReadingTypeUnit
     * @return A flag that indicates if both ReadingTypeUnit are compatible or not
     */
    public static boolean areCompatibleForAutomaticUnitConversion(ReadingTypeUnit first, ReadingTypeUnit second) {
        return (isVolumeRelated(first) && isFlowRelated(second))
                || (isFlowRelated(first) && isVolumeRelated(second))
                || (sameDimension(first, second));
    }

    static boolean isVolumeRelated(ReadingTypeUnit unit) {
        switch (unit) {
            // Explicitly enumerate all the ones that we consider flow related
            case CUBICMETER: // Intentional fall-through
            case CUBICFEET: // Intentional fall-through
            case CUBICFEETCOMPENSATED: // Intentional fall-through
            case CUBICFEETUNCOMPENSATED: // Intentional fall-through
            case CUBICMETERUNCOMPENSATED: // Intentional fall-through
            case CUBICMETERCOMPENSATED: // Intentional fall-through
            case CUBICYARD: // Intentional fall-through
            case USGALLON: // Intentional fall-through
            case IMPERIALGALLON: // Intentional fall-through
            case LITRE: // Intentional fall-through
            case LITREUNCOMPENSATED: // Intentional fall-through
            case LITRECOMPENSATED: // Intentional fall-through
            case VOLTAMPEREHOUR: // Intentional fall-through
            case WATTHOUR: // Intentional fall-through
            case AMPEREHOUR: // Intentional fall-through
            case VOLTAMPEREREACTIVEHOUR: // Intentional fall-through
            case VOLTHOUR: {
                return true;
            }
            case MOLE: { // Debatable but will not support this as volume for now
                return false;
            }
            default: {
                // All others are not volume related
                return false;
            }
        }
    }

    static boolean isFlowRelated(ReadingTypeUnit unit) {
        switch (unit) {
            // Explicitly enumerate all the ones that we consider flow related
            case WATT: // Intentional fall-through
            case VOLT: // Intentional fall-through
            case AMPERE: // Intentional fall-through
            case VOLTSQUARED: // Intentional fall-through
            case AMPERESQUARED: // Intentional fall-through
            case CUBICMETERPERHOUR: // Intentional fall-through
            case CUBICMETERPERHOURCOMPENSATED: // Intentional fall-through
            case CUBICMETERPERHOURUNCOMPENSATED: // Intentional fall-through
            case VOLTAMPERE: // Intentional fall-through
            case VOLTAMPEREREACTIVE: // Intentional fall-through
            case LITREPERHOUR: // Intentional fall-through
            case IMPERIALGALLONPERHOUR: // Intentional fall-through
            case USGALLONPERHOUR: {
                return true;
            }
            case QUANTITYPOWER: {
                /* Debatable but because the volume related unit (QUANTITYPOWERHOUR) is not there
                 * we will not consider it a flow unit for now. */
                return false;
            }
            default: {
                // All others are not flow related
                return false;
            }
        }
    }

    static boolean isTemperatureRelated(ReadingTypeUnit unit) {
        return unit.getUnit().getDimension().equals(Dimension.TEMPERATURE);
    }

    static boolean isPressureRelated(ReadingTypeUnit unit) {
        return unit.getUnit().getDimension().equals(Dimension.PRESSURE);
    }

    /**
     * Tests if both {@link VirtualReadingType}s are compatible for unit conversion.
     *
     * @param first The first VirtualReadingType
     * @param second The second VirtualReadingType
     * @return A flag that indicates if both VirtualReadingTypes are compatible or not
     * @see VirtualReadingType#getDimension()
     * @see #areCompatibleForAutomaticUnitConversion(Dimension, Dimension)
     */
    public static boolean areCompatibleForAutomaticUnitConversion(VirtualReadingType first, VirtualReadingType second) {
        return areCompatibleForAutomaticUnitConversion(first.getDimension(), second.getDimension());
    }

    /**
     * Tests if both {@link Dimension}s are compatible for unit conversion.
     *
     * @param first The first Dimension
     * @param second The second Dimension
     * @return A flag that indicates if both Dimension are compatible or not
     */
    public static boolean areCompatibleForAutomaticUnitConversion(Dimension first, Dimension second) {
        return (isDimensionless(first) || (isDimensionless(second))) ||
                (isVolumeRelated(first) && isFlowRelated(second))
                || (isFlowRelated(first) && isVolumeRelated(second))
                || (first.hasSameDimensions(second));
    }

    /**
     * Tests if the {@link Dimension} of a formula can be assigned to a given {@link ReadingType}
     *
     * @param readingType The ReadingType
     * @param dimension The Dimension
     * @return A flag that indicates if both Dimension are compatible or not
     */
    public static boolean isAssignable(ReadingType readingType, Dimension dimension) {
        Dimension dimensionOfReadingType = readingType.getUnit().getUnit().getDimension();
        return (isDimensionless(dimension) ||
                (isVolumeRelated(dimensionOfReadingType) && isFlowRelated(dimension))
                || (isFlowRelated(dimensionOfReadingType) && isVolumeRelated(dimension))
                || (dimensionOfReadingType.hasSameDimensions(dimension)));
    }

    private static boolean isDimensionless(Dimension dim) {
        return (dim.hasSameDimensions(Dimension.DIMENSIONLESS)) && (dim != Dimension.CURRENCY);
    }

    public static boolean isValidForAggregation(ReadingType readingType) {
        return isValidForAggregation(readingType.getUnit());
    }

    public static boolean isValidForAggregation(Set<ReadingTypeUnit> readingTypeUnits) {
        return readingTypeUnits.stream().allMatch(UnitConversionSupport::isValidForAggregation);
    }

    private static boolean isValidForAggregation(ReadingTypeUnit unit) {
        return (!unit.equals(ReadingTypeUnit.BOOLEAN)) &&
                (!unit.equals(ReadingTypeUnit.BOOLEANARRAY)) &&
                (!unit.equals(ReadingTypeUnit.ENCODEDVALUE)) &&
                (!unit.equals(ReadingTypeUnit.CHARACTERS)) &&
                (!unit.equals(ReadingTypeUnit.TIMESTAMP)) &&
                (!unit.equals(ReadingTypeUnit.ENDDEVICEEVENTCODE)) &&
                (!unit.equals(ReadingTypeUnit.NOTAPPLICABLE));
    }

    public static boolean isAssignable(IntervalLength deliverableIntervalLength, IntervalLength formulaIntervalLength) {
        return deliverableIntervalLength.isMultipleOf(formulaIntervalLength);
    }

    static boolean isVolumeRelated(Dimension dim) {
        switch (dim) {
            // Explicitly enumerate all the ones that we consider flow related
            case VOLUME: // Intentional fall-through
            case APPARENT_ENERGY: // Intentional fall-through
            case ENERGY: // Intentional fall-through
            case ELECTRIC_CHARGE: // Intentional fall-through
            case REACTIVE_ENERGY: // Intentional fall-through
            case MAGNETIC_FLUX: {
                return true;
            }
            case AMOUNT_OF_SUBSTANCE: { // Debatable but will not support this as volume for now
                return false;
            }
            default: {
                // All others are not volume related
                return false;
            }
        }
    }

    static boolean isFlowRelated(Dimension dim) {
        switch (dim) {
            // Explicitly enumerate all the ones that we consider flow related
            case POWER: // Intentional fall-through
            case ELECTRIC_POTENTIAL: // Intentional fall-through
            case ELECTRIC_CURRENT: // Intentional fall-through
            case ELECTRIC_POTENTIAL_SQUARED: // Intentional fall-through
            case ELECTRIC_CURRENT_SQUARED: // Intentional fall-through
            case VOLUME_FLOW: // Intentional fall-through
            case APPARENT_POWER: // Intentional fall-through
            case REACTIVE_POWER: {
                return true;
            }
            case DIMENSIONLESS: {
                /* Debatable but because the volume related unit (QUANTITYPOWERHOUR) is not there
                 * we will not consider it a flow unit for now. */
                return false;
            }
            default: {
                // All others are not flow related
                return false;
            }
        }
    }

    /**
     * Returns an expression tree that converts a variable from
     * the specified {@link Unit source unit} and {@link MetricMultiplier}
     * to the target Unit and MetricMultiplier.
     *
     * @param variable The VariableReferenceNode
     * @param source The source Unit
     * @param sourceMultiplier The source MetricMultiplier
     * @param target The target Unit
     * @param targetMetricMultiplier The targt MetricMultiplier
     * @return The expression tree that converts from source to target unit
     * @throws UnsupportedOperationException Thrown when source and target Unit are not of the same {@link Dimension}
     */
    static ServerExpressionNode unitConversion(SqlFragmentNode variable, ReadingTypeUnit source, MetricMultiplier sourceMultiplier, ReadingTypeUnit target, MetricMultiplier targetMetricMultiplier) {
        if (source.equals(target) && sourceMultiplier.equals(targetMetricMultiplier)) {
            return variable;
        } else if (!sameDimension(source, target)) {
            throw new UnsupportedOperationException("Unit conversion from " + source + " to " + target + " is not supported yet");
        } else {
            return unitConversion(variable, source.getUnit(), sourceMultiplier, target.getUnit(), targetMetricMultiplier);
        }
    }

    private static ServerExpressionNode unitConversion(ServerExpressionNode value, Unit source, MetricMultiplier sourceMetricMultiplier, Unit target, MetricMultiplier targetMetricMultiplier) {
        Unit siUnit = Unit.getSIUnit(source.getDimension());
        if (source.equals(siUnit)) {
            /* Converting from SI to other
             * value = (siValue - siDelta) * siDivisor / siMultiplier. */
            return fromZeroMultiplier(
                    Operator.DIVIDE.node(
                            Operator.MULTIPLY.node(
                                    Operator.MINUS.node(
                                            toZeroMultiplier(value, sourceMetricMultiplier),
                                            target.getSiDelta()),
                                    target.getSiDivisor()),
                            target.getSiMultiplier()),
                    targetMetricMultiplier);
        } else if (target.equals(siUnit)) {
            /* Converting to SI
             * siValue = (value * siMultiplier / siDivisor) + siDelta. */
            return fromZeroMultiplier(
                    Operator.PLUS.node(
                            Operator.DIVIDE.node(
                                    Operator.MULTIPLY.node(
                                            toZeroMultiplier(value, sourceMetricMultiplier),
                                            source.getSiMultiplier()),
                                    source.getSiDivisor()),
                            source.getSiDelta()),
                    targetMetricMultiplier);
        } else {
            /* Convert to source to si and si to target */
            return unitConversion(
                    unitConversion(
                            value,
                            source, sourceMetricMultiplier,
                            siUnit, MetricMultiplier.ZERO),
                    siUnit, MetricMultiplier.ZERO,
                    target, targetMetricMultiplier);
        }
    }

    private static ServerExpressionNode toZeroMultiplier(ServerExpressionNode value, MetricMultiplier multiplier) {
        if (multiplier.equals(MetricMultiplier.ZERO)) {
            return value;
        } else {
            return Operator.MULTIPLY.node(
                    value,
                    BigDecimal.ONE.scaleByPowerOfTen(multiplier.getMultiplier()));
        }
    }

    private static ServerExpressionNode fromZeroMultiplier(ServerExpressionNode value, MetricMultiplier multiplier) {
        if (multiplier.equals(MetricMultiplier.ZERO)) {
            return value;
        } else {
            return Operator.DIVIDE.node(
                    value,
                    BigDecimal.ONE.scaleByPowerOfTen(multiplier.getMultiplier()));
        }
    }

    static boolean sameDimension(ReadingTypeUnit first, ReadingTypeUnit second) {
        return toDimension(first).hasSameDimensions(toDimension(second));
    }

    public static Optional<Dimension> getMultiplicationDimension(Dimension first, Dimension second) {
        return IntermediateDimension.of(first).multiply(second).getDimension();
    }

    public static Optional<Dimension> getDivisionDimension(Dimension first, Dimension second) {
        return IntermediateDimension.of(first).divide(second).getDimension();
    }

    public static IntermediateDimension multiply(IntermediateDimension first, IntermediateDimension second) {
        return first.add(second);
    }

    public static IntermediateDimension divide(IntermediateDimension first, IntermediateDimension second) {
        return first.substract(second);
    }

    public static boolean isAllowedMultiplication(IntermediateDimension first, IntermediateDimension second) {
        return (first.isDimensionless() || (second.isDimensionless())) ||
                multiply(first, second).exists();
    }

    public static boolean isAllowedDivision(IntermediateDimension first, IntermediateDimension second) {
        return (first.isDimensionless() || (second.isDimensionless())) ||
                divide(first, second).exists();
    }

    private static Dimension toDimension(ReadingTypeUnit unit) {
        return unit.getUnit().getDimension();
    }

    // Hide utility class constructor
    private UnitConversionSupport() {
    }

}