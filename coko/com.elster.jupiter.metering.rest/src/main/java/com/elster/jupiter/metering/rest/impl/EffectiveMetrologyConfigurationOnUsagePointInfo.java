package com.elster.jupiter.metering.rest.impl;



import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EffectiveMetrologyConfigurationOnUsagePointInfo {
//    public long id;
//    public String name;
    public MetrologyConfigurationInfo metrologyConfiguration;
    public Long start;
    public Long end;
//    public List<ReadingTypeInfo> readingTypes;

    public EffectiveMetrologyConfigurationOnUsagePointInfo() {
    }

    EffectiveMetrologyConfigurationOnUsagePointInfo(EffectiveMetrologyConfigurationOnUsagePoint config){
//        id = config.getMetrologyConfiguration().getId();
//        name = config.getMetrologyConfiguration().getName();
        this.start = config.getStart() == null
                ? null
                : config.getStart().toEpochMilli();
        this.end = config.getEnd() == null
                ? null
                : config.getEnd().toEpochMilli();

        this.metrologyConfiguration = new MetrologyConfigurationInfo();
        this.metrologyConfiguration.id = config.getMetrologyConfiguration().getId();
        this.metrologyConfiguration.name = config.getMetrologyConfiguration().getName();
        this.metrologyConfiguration.version = config.getMetrologyConfiguration().getVersion();
//        readingTypes = config.getMetrologyConfiguration().getDeliverables()
//                .stream()
//                .map(ReadingTypeDeliverable::getReadingType)
//                .map(ReadingTypeInfo::new)
//                .collect(Collectors.toList());
    }
}
