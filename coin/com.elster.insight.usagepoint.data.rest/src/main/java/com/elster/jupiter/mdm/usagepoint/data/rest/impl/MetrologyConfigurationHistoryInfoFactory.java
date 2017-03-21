/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import java.time.Clock;
import java.util.stream.Collectors;

public class MetrologyConfigurationHistoryInfoFactory {

    private final BpmService bpmService;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final Clock clock;

    @Inject
    public MetrologyConfigurationHistoryInfoFactory(BpmService bpmService, ReadingTypeInfoFactory readingTypeInfoFactory, Clock clock) {
        this.bpmService = bpmService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.clock = clock;
    }

    public MetrologyConfigurationHistoryInfo from(EffectiveMetrologyConfigurationOnUsagePoint metrologyConfiguration, UsagePoint usagePoint, String auth) {
        MetrologyConfigurationHistoryInfo info = new MetrologyConfigurationHistoryInfo();
        info.start = metrologyConfiguration.getStart();
        info.end = metrologyConfiguration.getEnd();
        info.metrologyConfiguration = new IdWithNameInfo(metrologyConfiguration.getId(), metrologyConfiguration.getMetrologyConfiguration()
                .getName());
        info.current = metrologyConfiguration.isEffectiveAt(clock.instant());

        metrologyConfiguration.getMetrologyConfiguration().getContracts()
                .forEach(metrologyContract -> info.purposesWithReadingTypes.put(metrologyContract.getMetrologyPurpose().getName(),
                        metrologyContract.getDeliverables()
                                .stream()
                                .map(ReadingTypeDeliverable::getReadingType)
                                .map(readingTypeInfoFactory::from)
                                .collect(Collectors.toList())));

        String correlationId = usagePoint.getMRID() + ":processOnMetrologyConfig:" + metrologyConfiguration.getId();
        info.ongoingProcesses = bpmService.getRunningProcesses(auth, filterFor(correlationId)).processes
                .stream()
                .map(processInstanceInfo -> new IdWithNameInfo(processInstanceInfo.processId, processInstanceInfo.name))
                .collect(Collectors.toList());

        info.ongoingProcessesNumber = info.ongoingProcesses.size();
        return info;
    }

    private String filterFor(String correlationId) {
        return "?variableid=correlationId&variableValue=" + correlationId;
    }
}
