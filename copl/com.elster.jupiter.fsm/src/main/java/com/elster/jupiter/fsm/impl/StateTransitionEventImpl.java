package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.StateTransitionEvent;
import com.elster.jupiter.fsm.StateTransitionEventType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an implementation for the {@link StateTransitionEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:03)
 */
public class StateTransitionEventImpl implements StateTransitionEvent {
    private StateTransitionEventType eventType;
    private String sourceId;
    private Map<String, Object> properties;

    public StateTransitionEventImpl() {
        super();
    }

    StateTransitionEventImpl initialize(StateTransitionEventType eventType, String sourceId, Map<String, Object> properties) {
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.properties = new HashMap<>(properties);
        return this;
    }

    @Override
    public StateTransitionEventType getType() {
        return this.eventType;
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

}