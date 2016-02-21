package com.elster.jupiter.metering;

import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public interface GasDetailBuilder  extends UsagePointDetailBuilder{

    GasDetailBuilder withCollar(Boolean collar);

    GasDetailBuilder withGrounded(Boolean grounded);

    GasDetailBuilder withPressure(Quantity pressure);

    GasDetailBuilder withPhysicalCapacity(Quantity physicalCapacity);

    GasDetailBuilder withLimiter(Boolean limiter);

    GasDetailBuilder withLoadLimit(Quantity loadLimit);

    GasDetailBuilder withLoadLimiterType(String loadLimiterType);

    GasDetailBuilder withBypass(Boolean bypass);

    GasDetailBuilder withBypassStatus(BypassStatus bypassStatus);

    GasDetailBuilder withValve(Boolean valve);

    GasDetailBuilder withCapped(Boolean capped);

    GasDetailBuilder withClamped(Boolean clamped);

    GasDetailBuilder withInterruptible(Boolean interruptible);

    Optional<Boolean> getCollar();

    Optional<Boolean> getClamped();

    Boolean isGrounded();

    Quantity getPressure();

    Quantity getPhysicalCapacity();

    Boolean isLimiter();

    String getLoadLimiterType();

    Quantity getLoadLimit();

    Optional<Boolean> getBypass();

    BypassStatus getBypassStatus();

    Optional<Boolean> getValve();

    Optional<Boolean> getCapped();

    Boolean isInterruptible();

    GasDetail build();

}
