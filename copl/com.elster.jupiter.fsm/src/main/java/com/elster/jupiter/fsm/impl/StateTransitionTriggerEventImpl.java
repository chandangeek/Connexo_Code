package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.fsm.StateTransitionEventType;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an implementation for the {@link StateTransitionTriggerEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:03)
 */
public class StateTransitionTriggerEventImpl implements StateTransitionTriggerEvent {
    private final EventService eventService;
    private StateTransitionEventType eventType;
    private FinateStateMachine finateStateMachine;
    private String sourceId;
    private String sourceCurrentStateName;
    private Map<String, Object> properties;

    @Inject
    public StateTransitionTriggerEventImpl(EventService eventService) {
        super();
        this.eventService = eventService;
    }

    StateTransitionTriggerEventImpl initialize(StateTransitionEventType eventType, FinateStateMachine finateStateMachine, String sourceId, Map<String, Object> properties, String sourceCurrentStateName) {
        this.eventType = eventType;
        this.finateStateMachine = finateStateMachine;
        this.sourceId = sourceId;
        this.sourceCurrentStateName = sourceCurrentStateName;
        this.properties = new HashMap<>(properties);
        return this;
    }

    @Override
    public StateTransitionEventType getType() {
        return this.eventType;
    }

    @Override
    public FinateStateMachine getFinateStateMachine() {
        return this.finateStateMachine;
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }

    @Override
    public String getSourceCurrentStateName() {
        return this.sourceCurrentStateName;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public void publish() {
        this.eventService.postEvent(EventType.TRIGGER_EVENT.topic(), this);
    }

}