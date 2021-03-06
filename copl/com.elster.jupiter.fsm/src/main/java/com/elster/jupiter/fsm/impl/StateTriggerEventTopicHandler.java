/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.fsm.StateTransitionWebServiceClient;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles {@link StateTransitionTriggerEvent}s by building the computational
 * model for the related {@link FiniteStateMachine}
 * and triggering the actual event to calculate the new current state.
 * If there is a new current state, that change is then published
 * as a {@link StateTransitionChangeEvent}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (10:40)
 */
@Component(name = "com.elster.jupiter.fsm.state.trigger", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class StateTriggerEventTopicHandler extends StateTransitionTriggerEventTopicHandler {


    private Logger logger = Logger.getLogger(StateTriggerEventTopicHandler.class.getName());
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile EventService eventService;
    private volatile BpmService bpmService;
    private volatile StateTransitionPropertiesProvider propertiesProvider;

    // For OSGi purposes
    public StateTriggerEventTopicHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public StateTriggerEventTopicHandler(EventService eventService, BpmService bpmService,
                                         StateTransitionPropertiesProvider propertiesProvider) {
        this();
        this.setEventService(eventService);
        this.setBpmService(bpmService);
        this.setPropertiesProvider(propertiesProvider);
        this.setFiniteStateMachineService(finiteStateMachineService);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setPropertiesProvider(StateTransitionPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }


    @Override
    public void handle(LocalEvent localEvent) {
        Map<String, Object> messageProperties = (Map<String, Object>) localEvent.getSource();

        String stateId = this.getString("stateId", messageProperties);
        String sourceId = this.getString("sourceId", messageProperties);
        String sourceType = this.getString("sourceType", messageProperties);

        finiteStateMachineService.findFiniteStateById(Long.parseLong(stateId))
                .ifPresent(state -> {
                    new StateTransitionTriggerEventTopicHandler.StartExternalProcessesOnEntry(bpmService, propertiesProvider, state.getOnEntryProcesses(), sourceId, state, sourceType).startAll();
                    new StateTransitionTriggerEventTopicHandler.CallWebServiceClientOnEntry(finiteStateMachineService.getStateTransitionWebServiceClients(), state.getOnEntryEndPointConfigurations(), sourceId, state, sourceType).callAll();

                });
            }

    @Override
    public String getTopicMatcher() {
        return EventType.STATE_INIT.topic();
    }

    private String getString(String key, Map<String, Object> messageProperties) {
        Object contents = messageProperties.get(key);
        return contents.toString();
    }
}
