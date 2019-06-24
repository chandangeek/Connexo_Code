/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WaterUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
    public YesNoAnswer grounded;
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
        this.bypass = detail.isBypassInstalled();
        this.bypassStatus = detail.getBypassStatus();
        this.valve = detail.isValveInstalled();
        this.capped = detail.isCapped();
        this.clamped = detail.isClamped();
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
                .withCap(capped)
                .withClamp(clamped);
    }

    @Override
    public boolean isEqual(UsagePoint usagePoint, Clock clock){
        return usagePoint.getDetail(clock.instant())
                .map(upd -> {
                    WaterDetail detail = (WaterDetail)upd;
                    return isEqual(collar, detail.isCollarInstalled()) &&
                            isEqual(grounded, detail.isGrounded()) &&
                            isEqual(pressure, detail.getPressure()) &&
                            isEqual(physicalCapacity, detail.getPhysicalCapacity()) &&
                            isEqual(limiter, detail.getLoadLimit()) &&
                            isEqual(loadLimiterType, detail.getLoadLimiterType()) &&
                            isEqual(loadLimit, detail.getLoadLimit()) &&
                            isEqual(bypass, detail.isBypassInstalled()) &&
                            isEqual(bypassStatus, detail.getBypassStatus()) &&
                            isEqual(valve, detail.isValveInstalled()) &&
                            isEqual(capped, detail.isCapped()) &&
                            isEqual(clamped, detail.isClamped());
                })
                .orElseGet(() -> false);
    }
}
