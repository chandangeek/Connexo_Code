package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcessStartEvent;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link TransitionBusinessProcessStartEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (16:05)
 */
public class TransitionBusinessProcessStartEventImpl implements TransitionBusinessProcessStartEvent {

    private final EventService eventService;
    private String deploymentId;
    private String processId;
    private long deviceId;
    private State state;

    @Inject
    public TransitionBusinessProcessStartEventImpl(EventService eventService) {
        super();
        this.eventService = eventService;
    }

    public TransitionBusinessProcessStartEventImpl initialize(TransitionBusinessProcess process, long deviceId, State state) {
        this.deploymentId = process.getDeploymentId();
        this.processId = process.getProcessId();
        this.deviceId = deviceId;
        this.state = state;
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
    public long deviceId() {
        return deviceId;
    }

    @Override
    public State state() {
        return state;
    }

    void publish() {
        this.eventService.postEvent(TransitionBusinessProcessStartEvent.TOPIC, this);
    }

}