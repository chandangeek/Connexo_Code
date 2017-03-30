/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Responds to {@link StateTransitionChangeEvent} and effectively changes the
 * {@link com.elster.jupiter.fsm.State} of the related {@link com.elster.jupiter.metering.EndDevice}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (17:34)
 */
@Component(name = "com.elster.jupiter.metering.fsm.state.change.handler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class StateTransitionChangeEventTopicHandler implements TopicHandler {

    private Logger logger = Logger.getLogger(StateTransitionChangeEventTopicHandler.class.getName());
    private volatile Clock clock;
    private volatile FiniteStateMachineService stateMachineService;
    private volatile MeteringService meteringService;

    // For OSGi purposes
    public StateTransitionChangeEventTopicHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public StateTransitionChangeEventTopicHandler(Clock clock, FiniteStateMachineService stateMachineService, MeteringService meteringService) {
        this();
        this.setClock(clock);
        this.setStateMachineService(stateMachineService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setStateMachineService(FiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        if (event.getSourceType().contains("Device")) {
            this.handle(event);
        }
        else {
            this.logger.fine(() -> "Ignoring event for id '" + event.getSourceId() + "' because it does not relate to a device but to an obejct of type " + event.getSourceType());
        }
    }

    public void handle(StateTransitionChangeEvent event) {
        String deviceId = event.getSourceId();
        try {
            Query<EndDevice> endDeviceQuery = meteringService.getEndDeviceQuery();
            Condition condition = where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId()).and(where("amrId").isEqualTo(deviceId));
            endDeviceQuery.select(condition)
                    .stream()
                    .findFirst()
                    .filter(endDevice -> Objects.equals(event.getNewState()
                            .getFiniteStateMachine()
                            .getId(), endDevice.getFiniteStateMachine().get().getId()))
                    .ifPresent(d -> this.handle(event, (ServerEndDevice) d));
        }
        catch (NumberFormatException e) {
            this.logger.fine(() -> "Unable to parse end device id '" + deviceId + "' as a db identifier for an EndDevice from " + StateTransitionChangeEvent.class.getSimpleName());
        }
    }

    private void handle(StateTransitionChangeEvent event, ServerEndDevice endDevice) {
        endDevice.changeState(event.getNewState(), this.effectiveTimestampFrom(event));
    }

    private Instant effectiveTimestampFrom(StateTransitionChangeEvent event) {
        Instant effectiveTimestamp = event.getEffectiveTimestamp();
        if (effectiveTimestamp == null) {
            effectiveTimestamp = this.clock.instant();
        }
        return effectiveTimestamp;
    }

    @Override
    public String getTopicMatcher() {
        return this.stateMachineService.stateTransitionChangeEventTopic();
    }

}