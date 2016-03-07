package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.util.units.Dimension;
import com.elster.jupiter.util.units.Unit;

import java.util.Arrays;
import java.util.Optional;

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

    public static boolean isVolumeRelated(ReadingTypeUnit unit) {
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

    public static boolean isFlowRelated(ReadingTypeUnit unit) {
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

    public static boolean sameDimension(ReadingTypeUnit first, ReadingTypeUnit second) {
        return toDimension(first).hasSameDimensions(toDimension(second));
    }

    public static boolean isAllowedMultiplication(ReadingTypeUnit first, ReadingTypeUnit second) {
        return isAllowedMultiplicationOrDivision(first, second, true);
    }

    public static boolean isAllowedDivision(ReadingTypeUnit first, ReadingTypeUnit second) {
        return isAllowedMultiplicationOrDivision(first, second, false);
    }

    public static Optional<ReadingTypeUnit> getMultiplicationUnit(ReadingTypeUnit first, ReadingTypeUnit second) {
        return getMultiplicationOrDivisionUnit(first, second, true);
    }

    public static Optional<ReadingTypeUnit> getDivisionUnit(ReadingTypeUnit first, ReadingTypeUnit second) {
        return getMultiplicationOrDivisionUnit(first, second, false);
    }

    public static Optional<ReadingTypeUnit> getMultiplicationOrDivisionUnit(ReadingTypeUnit first, ReadingTypeUnit second, boolean multiplication) {
        Optional<Dimension> dimension = getMultiplicationOrDivisionDimension(first, second, multiplication);
        if (dimension.isPresent()) {
            Unit unit = Unit.getSIUnit(dimension.get());
            return ReadingTypeUnit.get(unit);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Dimension> getMultiplicationOrDivisionDimension(ReadingTypeUnit first, ReadingTypeUnit second, boolean multiplication) {
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
        return Arrays.asList(values).stream().filter(dim ->
                (dim.getLengthDimension() == length) &&
                        (dim.getMassDimension() == mass) &&
                        (dim.getTimeDimension() == time) &&
                        (dim.getCurrentDimension() == current) &&
                        (dim.getTemperatureDimension() == temp) &&
                        (dim.getAmountDimension() == amount) &&
                        (dim.getLuminousIntensityDimension() == luminous)).findAny();
    }


    private static boolean isAllowedMultiplicationOrDivision(ReadingTypeUnit first, ReadingTypeUnit second, boolean multiplication) {
        return getMultiplicationOrDivisionDimension(first, second, multiplication).isPresent();
    }

    private static Dimension toDimension(ReadingTypeUnit unit) {
        return unit.getUnit().getDimension();
    }

    // Hide utility class constructor
    private UnitConversionSupport() {}

}