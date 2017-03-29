/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.handler;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.StateChangeBusinessProcessStartEvent;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

@Component(name="com.elster.jupiter.fsm.handler", service = TopicHandler.class, immediate = true)
public class StateChangeBusinessProcessStartEventTopicHandler implements TopicHandler {

    private volatile BpmService bpmService;

    // For OSGi purposes
    public StateChangeBusinessProcessStartEventTopicHandler() {
        super();
    }

    // For testing purposes
    public StateChangeBusinessProcessStartEventTopicHandler(BpmService bpmService) {
        this();
        this.setBpmService(bpmService);
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Override
    public String getTopicMatcher() {
        return StateChangeBusinessProcessStartEvent.TOPIC;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((StateChangeBusinessProcessStartEvent) localEvent.getSource());
    }

    public void handle(StateChangeBusinessProcessStartEvent event) {
        Map<String, Object> parameters = ImmutableMap.of(
                "sourceId", event.sourceId(),
                "stateId", event.state().getId(),
                "changeType", event.type().parameterValue());
        this.bpmService.startProcess(event.deploymentId(), event.processId(), parameters);
    }

}