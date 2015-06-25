package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.fsm.StateTransitionEventType;

import javax.inject.Inject;
import java.time.Instant;
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
    private FiniteStateMachine finiteStateMachine;
    private String sourceId;
    private String sourceCurrentStateName;
    private Instant effectiveTimestamp;
    private Map<String, Object> properties;

    @Inject
    public StateTransitionTriggerEventImpl(EventService eventService) {
        super();
        this.eventService = eventService;
    }

    StateTransitionTriggerEventImpl initialize(StateTransitionEventType eventType, FiniteStateMachine finiteStateMachine, String sourceId, Instant effectiveTimestamp, Map<String, Object> properties, String sourceCurrentStateName) {
        this.eventType = eventType;
        this.finiteStateMachine = finiteStateMachine;
        this.sourceId = sourceId;
        this.sourceCurrentStateName = sourceCurrentStateName;
        this.effectiveTimestamp = effectiveTimestamp;
        this.properties = new HashMap<>(properties);
        return this;
    }

    @Override
    public StateTransitionEventType getType() {
        return this.eventType;
    }

    @Override
    public FiniteStateMachine getFiniteStateMachine() {
        return this.finiteStateMachine;
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
    public Instant getEffectiveTimestamp() {
        return effectiveTimestamp;
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