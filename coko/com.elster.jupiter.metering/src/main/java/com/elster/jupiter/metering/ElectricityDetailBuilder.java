package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

public interface ElectricityDetailBuilder extends UsagePointDetailBuilder{

    ElectricityDetailBuilder withCollar(YesNoAnswer collar);

    ElectricityDetailBuilder withGrounded(boolean grounded);

    ElectricityDetailBuilder withNominalServiceVoltage(Quantity nominalServiceVoltage);

    ElectricityDetailBuilder withPhaseCode(PhaseCode phaseCode);

    ElectricityDetailBuilder withRatedCurrent(Quantity ratedCurrent);

    ElectricityDetailBuilder withRatedPower(Quantity ratedPower);

    ElectricityDetailBuilder withEstimatedLoad(Quantity estimatedLoad);

    ElectricityDetailBuilder withLimiter(boolean limiter);

    ElectricityDetailBuilder withLoadLimit(Quantity loadLimit);

    ElectricityDetailBuilder withLoadLimiterType(String loadLimiterType);

    ElectricityDetailBuilder withInterruptible(boolean interruptible);

    ElectricityDetail create();

}
