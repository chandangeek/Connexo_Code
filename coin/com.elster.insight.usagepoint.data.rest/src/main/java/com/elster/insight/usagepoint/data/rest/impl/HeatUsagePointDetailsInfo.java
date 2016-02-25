package com.elster.insight.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.units.Quantity;

import java.time.Clock;

public class HeatUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
    public Quantity pressure;
    public Quantity physicalCapacity;
    public Boolean bypass;
    public BypassStatus bypassStatus;
    public Boolean valve;
    public Boolean interruptible;

    public HeatUsagePointDetailsInfo() {
    }

    public HeatUsagePointDetailsInfo(HeatDetail detail) {
        super(detail);
        this.physicalCapacity = detail.getPhysicalCapacity();
        this.pressure = detail.getPressure();
        this.bypass = detail.getBypass().orElse(null);
        this.bypassStatus = detail.getBypassStatus();
        this.valve = detail.getValve().orElse(null);
        this.interruptible = detail.isInterruptible();
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newHeatDetailBuilder(clock.instant())
                .withCollar(collar)
                .withPressure(pressure)
                .withPhysicalCapacity(physicalCapacity)
                .withBypass(bypass)
                .withBypassStatus(bypassStatus)
                .withValve(valve)
                .withInterruptible(interruptible);
    }
}
