package com.elster.jupiter.metering;

import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public interface WaterDetailBuilder {

    WaterDetailBuilder withCollar(Boolean collar);

    WaterDetailBuilder withGrounded(Boolean grounded);

    WaterDetailBuilder withPressure(Quantity pressure);

    WaterDetailBuilder withPhysicalCapacity(Quantity physicalCapacity);

    WaterDetailBuilder withLimiter(Boolean limiter);

    WaterDetailBuilder withLoadLimit(Quantity loadLimit);

    WaterDetailBuilder withLoadLimiterType(String loadLimiterType);

    WaterDetailBuilder withBypass(Boolean bypass);

    WaterDetailBuilder withBypassStatus(BypassStatus bypassStatus);

    WaterDetailBuilder withValve(Boolean valve);

    WaterDetailBuilder withCapped(Boolean capped);

    WaterDetailBuilder withClamped(Boolean clamped);

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

    WaterDetail build();

}
