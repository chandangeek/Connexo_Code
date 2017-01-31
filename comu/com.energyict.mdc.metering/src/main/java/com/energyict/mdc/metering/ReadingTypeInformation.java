/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

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
