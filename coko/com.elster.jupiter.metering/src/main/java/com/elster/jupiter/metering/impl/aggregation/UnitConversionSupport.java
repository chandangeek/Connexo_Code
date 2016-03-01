package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.util.units.Dimension;

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

    static boolean sameDimension(ReadingTypeUnit first, ReadingTypeUnit second) {
        return toDimension(first) == toDimension(second);
    }

    private static Dimension toDimension(ReadingTypeUnit unit) {
        return unit.getUnit().getDimension();
    }

    // Hide utility class constructor
    private UnitConversionSupport() {}

}