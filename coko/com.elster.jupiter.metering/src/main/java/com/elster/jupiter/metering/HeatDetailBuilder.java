package com.elster.jupiter.metering;

import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

public interface HeatDetailBuilder  extends UsagePointDetailBuilder{

    HeatDetailBuilder withCollar(YesNoAnswer collar);

    HeatDetailBuilder withPressure(Quantity pressure);

    HeatDetailBuilder withPhysicalCapacity(Quantity physicalCapacity);

    HeatDetailBuilder withBypass(YesNoAnswer bypass);

    HeatDetailBuilder withBypassStatus(BypassStatus bypassStatus);

    HeatDetailBuilder withValve(YesNoAnswer valve);

    HeatDetailBuilder withInterruptible(boolean interruptible);

    HeatDetail create();
}
