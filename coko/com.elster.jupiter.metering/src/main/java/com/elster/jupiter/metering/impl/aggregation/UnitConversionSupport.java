package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.util.units.Dimension;
import com.elster.jupiter.util.units.Unit;

import java.util.stream.Stream;

/**
 * Provides support for unit conversion as part of data aggregation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-01 (11:12)
 */
class UnitConversionSupport {

    /**
     * Tests if both {@link ReadingTypeUnit}s are compatible for unit conversion.
     *
     * @param first The first ReadingTypeUnit
     * @param second The second ReadingTypeUnit
     * @return A flag that indicates if both ReadingTypeUnit are compatible or not
     */
    static boolean areCompatibleForAutomaticUnitConversion(ReadingTypeUnit first, ReadingTypeUnit second) {
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
     * Returns an expression tree that converts a variable from
     * the specified {@link Unit source unit} to the target Unit.
     *
     * @param variable The VariableReferenceNode
     * @param source The source Unit
     * @param target The target Unit
     * @return The expression tree that converts from source to target unit
     * @throws UnsupportedOperationException Thrown when source and target Unit are not of the same {@link Dimension}
     */
    static ServerExpressionNode unitConversion(VariableReferenceNode variable, ReadingTypeUnit source, ReadingTypeUnit target) {
        if (source.equals(target)) {
            return variable;
        } else if (!sameDimension(source, target)) {
            throw new UnsupportedOperationException("Unit conversion from " + source + " to " + target + " is not supported yet");
        } else {
            return unitConversion(variable, source.getUnit(), target.getUnit());
        }
    }

    private static ServerExpressionNode unitConversion(ServerExpressionNode value, Unit source, Unit target) {
        Unit siUnit = Unit.getSIUnit(source.getDimension());
        if (source.equals(siUnit)) {
            /* Converting from SI to other
             * value = (siValue - siDelta) * siDivisor / siMultiplier. */
            return Operator.DIVIDE.node(
                        Operator.MULTIPLY.node(
                                Operator.MINUS.node(
                                        value,
                                        target.getSiDelta()),
                                target.getSiDivisor()),
                        target.getSiMultiplier());
        } else if (target.equals(siUnit)) {
            /* Converting to SI
             * siValue = (value * siMultiplier / siDivisor) + siDelta. */
            return Operator.PLUS.node(
                    Operator.DIVIDE.node(
                            Operator.MULTIPLY.node(
                                    value,
                                    source.getSiMultiplier()),
                            source.getSiDivisor()),
                    source.getSiDelta());
        } else {
            /* Convert to source to si and si to target */
            return unitConversion(
                        unitConversion(value, source, siUnit),
                        siUnit,
                        target);
        }
    }

    static boolean sameDimension(ReadingTypeUnit first, ReadingTypeUnit second) {
        return toDimension(first).hasSameDimensions(toDimension(second));
    }

    static boolean isAllowedMultiplication(ReadingTypeUnit first, ReadingTypeUnit second) {
        return isAllowedMultiplicationOrDivision(first, second, true);
    }

    static boolean isAllowedDivision(ReadingTypeUnit first, ReadingTypeUnit second) {
        return isAllowedMultiplicationOrDivision(first, second, false);
    }

    private static boolean isAllowedMultiplicationOrDivision(ReadingTypeUnit first, ReadingTypeUnit second, boolean multiplication) {
        Dimension firstDim = toDimension(first);
        Dimension secondDim = toDimension(second);

        int factor = (multiplication) ? 1 : -1;

        int length = firstDim.getLengthDimension() + (secondDim.getLengthDimension() * factor);
        int mass = firstDim.getMassDimension() + (secondDim.getMassDimension() * factor);
        int time = firstDim.getTimeDimension() + (secondDim.getTimeDimension() * factor);
        int current = firstDim.getCurrentDimension() + (secondDim.getCurrentDimension() * factor);
        int temp = firstDim.getTemperatureDimension() + (secondDim.getTemperatureDimension() * factor);
        int amount = firstDim.getAmountDimension() + (secondDim.getAmountDimension() * factor);
        int luminous = firstDim.getLuminousIntensityDimension() + (secondDim.getLuminousIntensityDimension() * factor);

        Dimension[] values = Dimension.values();
        return Stream.of(values)
                .filter(dim -> dim.getLengthDimension() == length)
                .filter(dim -> dim.getMassDimension() == mass)
                .filter(dim -> dim.getTimeDimension() == time)
                .filter(dim -> dim.getCurrentDimension() == current)
                .filter(dim -> dim.getTemperatureDimension() == temp)
                .filter(dim -> dim.getAmountDimension() == amount)
                .filter(dim -> dim.getLuminousIntensityDimension() == luminous)
                .findAny()
                .isPresent();
    }

    private static Dimension toDimension(ReadingTypeUnit unit) {
        return unit.getUnit().getDimension();
    }

    // Hide utility class constructor
    private UnitConversionSupport() {}

}