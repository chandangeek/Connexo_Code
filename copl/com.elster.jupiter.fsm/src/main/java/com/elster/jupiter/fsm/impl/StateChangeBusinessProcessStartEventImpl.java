package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.*;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link StateChangeBusinessProcessStartEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (11:13)
 */
public class StateChangeBusinessProcessStartEventImpl implements StateChangeBusinessProcessStartEvent {

    private final EventService eventService;
    private String deploymentId;
    private String processId;
    private String sourceId;
    private State state;
    private StateChangeBusinessProcessStartEvent.Type type;

    @Inject
    public StateChangeBusinessProcessStartEventImpl(EventService eventService) {
        super();
        this.eventService = eventService;
    }

    public StateChangeBusinessProcessStartEventImpl initialize(StateChangeBusinessProcess process, String sourceId, State state, Type type) {
        this.deploymentId = process.getDeploymentId();
        this.processId = process.getProcessId();
        this.sourceId = sourceId;
        this.state = state;
        this.type = type;
        return this;
    }

    @Override
    public String deploymentId() {
        return deploymentId;
    }

    @Override
    public String processId() {
        return processId;
    }

    @Override
    public String sourceId() {
        return sourceId;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public Type type() {
        return type;
    }

    void publish() {
        this.eventService.postEvent(StateChangeBusinessProcessStartEvent.TOPIC, this);
    }

}