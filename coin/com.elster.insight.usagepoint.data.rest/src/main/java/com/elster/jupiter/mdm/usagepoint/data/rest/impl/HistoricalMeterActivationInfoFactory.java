/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import java.net.URL;
import java.time.Clock;
import java.util.stream.Collectors;

public class HistoricalMeterActivationInfoFactory {
    private final BpmService bpmService;
    private final Clock clock;

    @Inject
    public HistoricalMeterActivationInfoFactory(BpmService bpmService, Clock clock) {
        this.bpmService = bpmService;
        this.clock = clock;
    }

    public HistoricalMeterActivationInfo from(MeterActivation meterActivation, UsagePoint usagePoint, String auth) {
        HistoricalMeterActivationInfo info = new HistoricalMeterActivationInfo();
        info.id = meterActivation.getId();
        info.start = meterActivation.getStart() == null ? null : meterActivation.getStart();
        info.end = meterActivation.getEnd() == null ? null : meterActivation.getEnd();
        info.current = meterActivation.isEffectiveAt(clock.instant());
        meterActivation.getMeter().ifPresent(meter -> {
            info.meter = meter.getName();
            info.url = meter.getHeadEndInterface()
                    .flatMap(he -> he.getURLForEndDevice(meter))
                    .map(URL::toString)
                    .orElse(null);
            String correlationId = usagePoint.getMRID() + ":processOnLinkedMeter:" + meter.getMRID();
            info.ongoingProcesses = bpmService.getRunningProcesses(auth, filterFor(correlationId)).processes.stream()
                    .map(processInstanceInfo -> new IdWithNameInfo(processInstanceInfo.processId, processInstanceInfo.name))
                    .collect(Collectors.toList());
        });
        meterActivation.getMeterRole().ifPresent(meterRole -> info.meterRole = meterRole.getDisplayName());
        return info;
    }

    private String filterFor(String correlationId) {
        return "?variableid=correlationId&variablevalue=" + correlationId;
    }
}
