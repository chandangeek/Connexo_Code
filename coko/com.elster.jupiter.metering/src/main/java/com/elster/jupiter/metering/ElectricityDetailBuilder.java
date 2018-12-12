/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

public interface ElectricityDetailBuilder extends UsagePointDetailBuilder{

    ElectricityDetailBuilder withCollar(YesNoAnswer collar);

    ElectricityDetailBuilder withGrounded(YesNoAnswer grounded);

    ElectricityDetailBuilder withNominalServiceVoltage(Quantity nominalServiceVoltage);

    ElectricityDetailBuilder withPhaseCode(PhaseCode phaseCode);

    ElectricityDetailBuilder withRatedCurrent(Quantity ratedCurrent);

    ElectricityDetailBuilder withRatedPower(Quantity ratedPower);

    ElectricityDetailBuilder withEstimatedLoad(Quantity estimatedLoad);

    ElectricityDetailBuilder withLimiter(YesNoAnswer limiter);

    ElectricityDetailBuilder withLoadLimit(Quantity loadLimit);

    ElectricityDetailBuilder withLoadLimiterType(String loadLimiterType);

    ElectricityDetailBuilder withInterruptible(YesNoAnswer interruptible);

    ElectricityDetail create();

}
