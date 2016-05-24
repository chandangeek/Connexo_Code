package com.elster.jupiter.platform.api.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;

/**
 * Created by bvn on 4/11/16.
 */
public class WaterUsagePointInfo extends UsagePointInfo {
    public Quantity pressure;
    public Quantity physicalCapacity;
    public YesNoAnswer limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public YesNoAnswer bypass;
    public BypassStatus bypassStatus;
    public YesNoAnswer valve;
    public YesNoAnswer capped;
    public YesNoAnswer clamped;

    @Override
    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Instant instant) {
        return usagePoint.newWaterDetailBuilder(instant)
                .withCollar(collar)
                .withGrounded(grounded)
                .withPressure(pressure)
                .withPhysicalCapacity(physicalCapacity)
                .withLimiter(limiter)
                .withLoadLimit(loadLimit)
                .withLoadLimiterType(loadLimiterType)
                .withBypass(bypass)
                .withBypassStatus(bypassStatus)
                .withValve(valve)
                .withCap(capped)
                .withClamp(clamped);
    }

    @Override
    ServiceKind getServiceKind() {
        return ServiceKind.WATER;
    }

}
