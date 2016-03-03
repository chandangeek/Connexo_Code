package com.elster.jupiter.metering;

import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

public interface GasDetailBuilder  extends UsagePointDetailBuilder{

    GasDetailBuilder withCollar(YesNoAnswer collar);

    GasDetailBuilder withGrounded(boolean grounded);

    GasDetailBuilder withPressure(Quantity pressure);

    GasDetailBuilder withPhysicalCapacity(Quantity physicalCapacity);

    GasDetailBuilder withLimiter(boolean limiter);

    GasDetailBuilder withLoadLimit(Quantity loadLimit);

    GasDetailBuilder withLoadLimiterType(String loadLimiterType);

    GasDetailBuilder withBypass(YesNoAnswer bypass);

    GasDetailBuilder withBypassStatus(BypassStatus bypassStatus);

    GasDetailBuilder withValve(YesNoAnswer valve);

    GasDetailBuilder withCap(YesNoAnswer capped);

    GasDetailBuilder withClamp(YesNoAnswer clamped);

    GasDetailBuilder withInterruptible(boolean interruptible);

    GasDetail create();

}
