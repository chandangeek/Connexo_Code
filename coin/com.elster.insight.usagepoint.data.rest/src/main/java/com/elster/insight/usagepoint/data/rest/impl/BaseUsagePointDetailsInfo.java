package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.time.Clock;

@JsonSubTypes({
        @JsonSubTypes.Type(value = ElectricityUsagePointDetailsInfo.class, name = "ELECTRICITY"),
        @JsonSubTypes.Type(value = GasUsagePointDetailsInfo.class, name = "GAS"),
        @JsonSubTypes.Type(value = WaterUsagePointDetailsInfo.class, name = "WATER"),
        @JsonSubTypes.Type(value = HeatUsagePointDetailsInfo.class, name = "HEAT")
})
public abstract class BaseUsagePointDetailsInfo {
    public Boolean collar;

    public BaseUsagePointDetailsInfo() {
    }

    public BaseUsagePointDetailsInfo(UsagePointDetail detail) {
        this.collar = detail.getCollar().isPresent() ? detail.getCollar().get() : null;
    }

    public abstract UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock);
}
