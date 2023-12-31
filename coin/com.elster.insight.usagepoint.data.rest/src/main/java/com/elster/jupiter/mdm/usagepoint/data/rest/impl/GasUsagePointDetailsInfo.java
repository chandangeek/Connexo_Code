/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GasUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
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
    public YesNoAnswer interruptible;

    public GasUsagePointDetailsInfo() {
    }

    public GasUsagePointDetailsInfo(GasDetail detail) {
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
        this.interruptible = detail.isInterruptible();
    }

    @Override
    public ServiceKind getKind() {
        return ServiceKind.GAS;
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newGasDetailBuilder(clock.instant())
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
                .withClamp(clamped)
                .withInterruptible(interruptible);
    }

    @Override
    public boolean isEqual(UsagePoint usagePoint, Clock clock){
        return usagePoint.getDetail(clock.instant())
                .map(upd -> {
                    GasDetail detail = (GasDetail)upd;
                    return isEqual(grounded, detail.isGrounded()) &&
                            isEqual(pressure, detail.getPressure()) &&
                            isEqual(physicalCapacity, detail.getPhysicalCapacity()) &&
                            isEqual(limiter, detail.isLimiter()) &&
                            isEqual(loadLimiterType, detail.getLoadLimiterType()) &&
                            isEqual(loadLimit, detail.getLoadLimit()) &&
                            isEqual(bypass, detail.isBypassInstalled()) &&
                            isEqual(bypassStatus, detail.getBypassStatus()) &&
                            isEqual(valve, detail.isValveInstalled()) &&
                            isEqual(capped, detail.isCapped()) &&
                            isEqual(clamped, detail.isClamped()) &&
                            isEqual(interruptible, detail.isInterruptible());
                })
                .orElseGet(() -> false);
    }
}
