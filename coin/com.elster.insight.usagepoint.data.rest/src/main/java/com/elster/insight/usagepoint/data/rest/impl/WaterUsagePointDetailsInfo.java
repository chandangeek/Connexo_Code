package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetail;
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
        this.bypass = detail.getBypass().isPresent() ? detail.getBypass().get() : null;
        this.bypassStatus = detail.getBypassStatus();
        this.valve = detail.getValve().isPresent() ? detail.getValve().get() : null;
        this.capped = detail.getCapped().isPresent() ? detail.getCapped().get() : null;
        this.clamped = detail.getClamped().isPresent() ? detail.getClamped().get() : null;
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
