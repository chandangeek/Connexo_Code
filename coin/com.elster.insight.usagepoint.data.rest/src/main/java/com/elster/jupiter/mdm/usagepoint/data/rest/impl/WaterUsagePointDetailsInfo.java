package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.units.Quantity;

import java.time.Clock;

public class WaterUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
    public Boolean grounded;
    public Quantity pressure;
    public Quantity physicalCapacity;
    public Boolean limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public Boolean bypass;
    public BypassStatus bypassStatus;
    public Boolean valve;
    public Boolean capped;
    public Boolean clamped;

    public WaterUsagePointDetailsInfo() {
    }

    public WaterUsagePointDetailsInfo(WaterDetail detail) {
        super(detail);
        this.grounded = detail.isGrounded();
        this.physicalCapacity = detail.getPhysicalCapacity();
        this.pressure = detail.getPressure();
        this.limiter = detail.isLimiter();
        this.loadLimiterType = detail.getLoadLimiterType();
        this.loadLimit = detail.getLoadLimit();
        this.bypass = detail.getBypass().orElse(null);
        this.bypassStatus = detail.getBypassStatus();
        this.valve = detail.getValve().orElse(null);
        this.capped = detail.getCapped().orElse(null);
        this.clamped = detail.getClamped().orElse(null);
    }

    @Override
    public ServiceKind getKind() {
        return ServiceKind.WATER;
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newWaterDetailBuilder(clock.instant())
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
                .withCapped(capped)
                .withClamped(clamped);
    }


}
