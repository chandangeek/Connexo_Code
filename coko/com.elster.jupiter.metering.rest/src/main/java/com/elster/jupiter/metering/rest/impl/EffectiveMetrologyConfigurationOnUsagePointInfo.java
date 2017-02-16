/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;



import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EffectiveMetrologyConfigurationOnUsagePointInfo {
    public long id;
    public MetrologyConfigurationInfo metrologyConfiguration;
    public Long start;
    public Long end;
    public Long newStart;
    public Long newEnd;
    public boolean editable;
    public boolean current;

    public EffectiveMetrologyConfigurationOnUsagePointInfo() {
    }

    EffectiveMetrologyConfigurationOnUsagePointInfo(EffectiveMetrologyConfigurationOnUsagePoint config, Clock clock){
        this.id = config.getId();
        this.start = config.getStart() == null
                ? null
                : config.getStart().toEpochMilli();
        this.end = config.getEnd() == null
                ? null
                : config.getEnd().toEpochMilli();
        this.editable = config.getStart().isAfter(clock.instant());
        this.current = config.getRange().contains(clock.instant());
        this.metrologyConfiguration = new MetrologyConfigurationInfo();
        this.metrologyConfiguration.id = config.getMetrologyConfiguration().getId();
        this.metrologyConfiguration.name = config.getMetrologyConfiguration().getName();
        this.metrologyConfiguration.version = config.getMetrologyConfiguration().getVersion();
    }
}
