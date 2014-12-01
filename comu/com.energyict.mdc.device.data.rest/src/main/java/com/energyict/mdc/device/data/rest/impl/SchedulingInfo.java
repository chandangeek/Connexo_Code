package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchedulingInfo {
    @JsonProperty("id")
    public long id;
    @JsonProperty("schedule")
    public TemporalExpressionInfo schedule;

    public SchedulingInfo() {
    }

    public SchedulingInfo(long id, TemporalExpressionInfo schedule) {
        this.id = id;
        this.schedule = schedule;
    }
}
