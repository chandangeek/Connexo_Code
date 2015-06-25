package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an implementation for the {@link StateTransitionChangeEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (13:34)
 */
public class StateTransitionChangeEventImpl implements StateTransitionChangeEvent {

    private final EventService eventService;
    private final State from;
    private final State to;
    private final String sourceId;
    private final Instant effectiveTimestamp;
    private final Map<String, Object> properties;

    public StateTransitionChangeEventImpl(EventService eventService, State from, State to, String sourceId, Instant effectiveTimestamp, Map<String, Object> properties) {
        super();
        this.eventService = eventService;
        this.from = from;
        this.to = to;
        this.sourceId = sourceId;
        this.effectiveTimestamp = effectiveTimestamp;
        this.properties = new HashMap<>(properties);
    }

    @Override
    public State getOldState() {
        return from;
    }

    @Override
    public State getNewState() {
        return to;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public Instant getEffectiveTimestamp() {
        return effectiveTimestamp;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void publish() {
        this.eventService.postEvent(EventType.CHANGE_EVENT.topic(), this);
    }

}