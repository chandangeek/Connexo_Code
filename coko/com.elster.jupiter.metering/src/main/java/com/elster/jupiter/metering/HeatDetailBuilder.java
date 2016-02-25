package com.elster.jupiter.metering;

import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public interface HeatDetailBuilder  extends UsagePointDetailBuilder{

    HeatDetailBuilder withCollar(Boolean collar);

    HeatDetailBuilder withPressure(Quantity pressure);

    HeatDetailBuilder withPhysicalCapacity(Quantity physicalCapacity);

    HeatDetailBuilder withBypass(Boolean bypass);

    HeatDetailBuilder withBypassStatus(BypassStatus bypassStatus);

    HeatDetailBuilder withValve(Boolean valve);

    HeatDetailBuilder withInterruptible(Boolean interruptible);

    Optional<Boolean> getCollar();

    Quantity getPressure();

    Quantity getPhysicalCapacity();

    Optional<Boolean> getBypass();

    BypassStatus getBypassStatus();

    Optional<Boolean> getValve();

    Boolean isInterruptible();

    HeatDetail create();
}
