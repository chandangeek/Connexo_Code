/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.eventpropagator.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.json.JsonDeserializeException;
import com.elster.jupiter.util.json.JsonService;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeteringMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final MeteringService meteringService;
    private final BpmService bpmService;
    private final Clock clock;

    private static final Logger LOGGER = Logger.getLogger(MeteringMessageHandler.class.getName());

    public MeteringMessageHandler(JsonService jsonService, BpmService bpmService, MeteringService meteringService, Clock clock) {
        this.jsonService = jsonService;
        this.bpmService = bpmService;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);

        Stream.of(EventType.METER_UPDATED,
                EventType.USAGEPOINT_UPDATED,
                EventType.METERREADING_CREATED)
                .filter(et -> (messageProperties.containsKey("id")
                        || messageProperties.containsKey("meterId"))
                        && messageProperties.containsKey("event.topics")
                        && et.topic().equals(messageProperties.get("event.topics").toString()))
                .findFirst()
                .ifPresent(eventType -> {
                    switch (eventType) {
                        case USAGEPOINT_UPDATED:
                            meteringService.findUsagePointById(Long.valueOf(messageProperties.get("id").toString()))
                                    .ifPresent(usagePoint -> sendSignalToActiveProcesses(usagePoint.getMRID(), eventType.name()));
                            break;
                        case METER_UPDATED:
                            meteringService.findMeterById(Long.valueOf(messageProperties.get("id").toString()))
                                    .map(meter -> meter.getUsagePoint(clock.instant()))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .ifPresent(usagePoint -> sendSignalToActiveProcesses(usagePoint.getMRID(), eventType.name()));
                            break;
                        case METERREADING_CREATED:
                            meteringService.findMeterById(Long.valueOf(messageProperties.get("meterId").toString()))
                                    .map(meter -> meter.getUsagePoint(clock.instant()))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .ifPresent(usagePoint -> sendSignalToActiveProcesses(usagePoint.getMRID(), eventType.name()));
                    }
                });
    }

    private void sendSignalToActiveProcesses(String usagePointMrid, String signal) {
        getUsagePointProcessSignalUrls(usagePointMrid).stream()
                .map(url -> url.concat(signal))
                .forEach(url -> bpmService.getBpmServer().doPost(url, null));
    }

    private List<String> getUsagePointProcessSignalUrls(String usagePointMrid) {
        ProcessInstanceInfos activeProcesses = bpmService.getRunningProcesses(null, "?variableid=usagePointId&variablevalue=" + usagePointMrid);

        List<ProcessDefinitionInfo> processes = getDeployments().map(processDefinitionInfos -> processDefinitionInfos.processDefinitionList)
                .orElse(Collections.emptyList());

        return activeProcesses.processes.stream()
                .filter(process -> processes.stream().anyMatch(processInfo -> processInfo.name.equals(process.name)))
                .map(process -> "/rest/runtime/" + processes.stream()
                        .filter(processInfo -> processInfo.name.equals(process.name))
                        .findFirst()
                        .get().deploymentId + "/process/instance/" + process.processId + "/signal?signal=")
                .collect(Collectors.toList());
    }

    private Optional<ProcessDefinitionInfos> getDeployments() {
        try {
            String jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes");
            if (!"".equals(jsonContent)) {
                return Optional.ofNullable(jsonService.deserialize(jsonContent, ProcessDefinitionInfos.class));
            }
        } catch (JsonDeserializeException e){
            LOGGER.log(Level.SEVERE, "JSON deserialization error", e);
            return Optional.empty();
        }catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Unable to connect to Flow: " + e.getMessage(), e);
            return Optional.empty();
        }
        return Optional.empty();
    }
}
