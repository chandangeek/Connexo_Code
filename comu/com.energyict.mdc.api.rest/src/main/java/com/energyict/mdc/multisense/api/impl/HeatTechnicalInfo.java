package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import java.time.Clock;

/**
 * Created by bvn on 4/11/16.
 */
public class HeatTechnicalInfo extends UsagePointInfo {
    public Quantity pressure;
    public Quantity physicalCapacity;
    public YesNoAnswer bypass;
    public BypassStatus bypassStatus;
    public YesNoAnswer valve;

    @Override
    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newHeatDetailBuilder(clock.instant())
                .withCollar(collar)
                .withPressure(pressure)
                .withPhysicalCapacity(physicalCapacity)
                .withBypass(bypass)
                .withBypassStatus(bypassStatus)
                .withValve(valve);
    }

    @Override
    ServiceKind getServiceKind() {
        return ServiceKind.HEAT;
    }


}
