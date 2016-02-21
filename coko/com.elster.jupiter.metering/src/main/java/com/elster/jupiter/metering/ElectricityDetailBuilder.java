package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public interface ElectricityDetailBuilder extends UsagePointDetailBuilder{

    ElectricityDetailBuilder withCollar(Boolean collar);

    ElectricityDetailBuilder withGrounded(Boolean grounded);

    ElectricityDetailBuilder withNominalServiceVoltage(Quantity nominalServiceVoltage);

    ElectricityDetailBuilder withPhaseCode(PhaseCode phaseCode);

    ElectricityDetailBuilder withRatedCurrent(Quantity ratedCurrent);

    ElectricityDetailBuilder withRatedPower(Quantity ratedPower);

    ElectricityDetailBuilder withEstimatedLoad(Quantity estimatedLoad);

    ElectricityDetailBuilder withLimiter(Boolean limiter);

    ElectricityDetailBuilder withLoadLimit(Quantity loadLimit);

    ElectricityDetailBuilder withLoadLimiterType(String loadLimiterType);

    ElectricityDetailBuilder withInterruptible(Boolean interruptible);

    Optional<Boolean> getCollar();

    Boolean isGrounded();

    Quantity getNominalServiceVoltage();

    PhaseCode getPhaseCode();

    Quantity getRatedCurrent();

    Quantity getRatedPower();

    Quantity getEstimatedLoad();

    Boolean isLimiter();

    String getLoadLimiterType();

    Quantity getLoadLimit();

    Boolean isInterruptible();

    ElectricityDetail build();

}
