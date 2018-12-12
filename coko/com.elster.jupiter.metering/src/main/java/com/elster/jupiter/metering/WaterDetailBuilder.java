/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

public interface WaterDetailBuilder  extends UsagePointDetailBuilder{

    WaterDetailBuilder withCollar(YesNoAnswer collar);

    WaterDetailBuilder withGrounded(YesNoAnswer grounded);

    WaterDetailBuilder withPressure(Quantity pressure);

    WaterDetailBuilder withPhysicalCapacity(Quantity physicalCapacity);

    WaterDetailBuilder withLimiter(YesNoAnswer limiter);

    WaterDetailBuilder withLoadLimit(Quantity loadLimit);

    WaterDetailBuilder withLoadLimiterType(String loadLimiterType);

    WaterDetailBuilder withBypass(YesNoAnswer bypass);

    WaterDetailBuilder withBypassStatus(BypassStatus bypassStatus);

    WaterDetailBuilder withValve(YesNoAnswer valve);

    WaterDetailBuilder withCap(YesNoAnswer capped);

    WaterDetailBuilder withClamp(YesNoAnswer clamped);

    WaterDetail create();
}
