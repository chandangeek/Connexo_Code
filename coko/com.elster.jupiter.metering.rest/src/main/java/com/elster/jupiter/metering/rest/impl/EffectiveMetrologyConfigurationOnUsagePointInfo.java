package com.elster.jupiter.metering.rest.impl;



import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;

public class EffectiveMetrologyConfigurationOnUsagePointInfo {
    public long id;
    public String name;
    public Long start;
    public Long end;

    EffectiveMetrologyConfigurationOnUsagePointInfo(EffectiveMetrologyConfigurationOnUsagePoint config){
        id = config.getMetrologyConfiguration().getId();
        name = config.getMetrologyConfiguration().getName();
        this.start = config.getStart() == null
                ? null
                : config.getStart().toEpochMilli();
        this.end = config.getEnd() == null
                ? null
                : config.getEnd().toEpochMilli();
    }
}
