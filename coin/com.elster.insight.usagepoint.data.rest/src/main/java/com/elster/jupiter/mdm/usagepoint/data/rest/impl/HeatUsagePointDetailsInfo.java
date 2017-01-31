/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;

@JsonIgnoreProperties(ignoreUnknown=true)
public class HeatUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
    public Quantity pressure;
    public Quantity physicalCapacity;
    public YesNoAnswer bypass;
    public BypassStatus bypassStatus;
    public YesNoAnswer valve;

    public HeatUsagePointDetailsInfo() {
    }

    public HeatUsagePointDetailsInfo(HeatDetail detail) {
        super(detail);
        this.physicalCapacity = detail.getPhysicalCapacity();
        this.pressure = detail.getPressure();
        this.bypass = detail.isBypassInstalled();
        this.bypassStatus = detail.getBypassStatus();
        this.valve = detail.isValveInstalled();
    }

    @Override
    public ServiceKind getKind() {
        return ServiceKind.HEAT;
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newHeatDetailBuilder(clock.instant())
                .withCollar(collar)
                .withPressure(pressure)
                .withPhysicalCapacity(physicalCapacity)
                .withBypass(bypass)
                .withBypassStatus(bypassStatus)
                .withValve(valve);
    }
}
