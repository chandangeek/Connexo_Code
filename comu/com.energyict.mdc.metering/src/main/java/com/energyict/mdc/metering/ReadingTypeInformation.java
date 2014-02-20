package com.energyict.mdc.metering;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;

/**
 * Groups an {@link ObisCode}, {@link Unit} and {@link TimeDuration}
 * derived from a {@link com.elster.jupiter.metering.ReadingType}
 *
 * Copyrights EnergyICT
 * Date: 17/01/14
 * Time: 16:08
 */
public class ReadingTypeInformation {

    private final ObisCode obisCode;
    private final Unit unit;
    private final TimeDuration timeDuration;

    public ReadingTypeInformation(ObisCode obisCode, Unit unit, TimeDuration timeDuration) {
        this.obisCode = obisCode;
        this.unit = unit;
        this.timeDuration = timeDuration;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public Unit getUnit() {
        return unit;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }
}
