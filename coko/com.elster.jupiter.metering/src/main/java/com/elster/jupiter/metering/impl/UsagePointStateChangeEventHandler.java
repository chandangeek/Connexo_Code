/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

@Component(name = "UsagePointStateChangeEventHandler",
        service = {TopicHandler.class},
        immediate = true)
public class UsagePointStateChangeEventHandler implements TopicHandler {
    private Logger logger = Logger.getLogger(UsagePointStateChangeEventHandler.class.getName());
    private Clock clock;
    private MeteringService meteringService;
    private FiniteStateMachineService stateMachineService;
    private UsagePointLifeCycleConfigurationService lifeCycleConfService;

    public UsagePointStateChangeEventHandler() {
    }

    @Inject
    public UsagePointStateChangeEventHandler(Clock clock,
                                             MeteringService meteringService,
                                             FiniteStateMachineService stateMachineService,
                                             UsagePointLifeCycleConfigurationService lifeCycleConfService) {
        this();
        this.setClock(clock);
        this.setMeteringService(meteringService);
        this.setStateMachineService(stateMachineService);
        this.setLifeCycleConfService(lifeCycleConfService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setStateMachineService(FiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Reference
    public void setLifeCycleConfService(UsagePointLifeCycleConfigurationService lifeCycleConfService) {
        this.lifeCycleConfService = lifeCycleConfService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        if (event.getSourceType().contains("UsagePoint")) {
            this.handleUsagePointStateChange(event);
        }
    }

    @Override
    public String getTopicMatcher() {
        return this.stateMachineService.stateTransitionChangeEventTopic();
    }

    public void handleUsagePointStateChange(StateTransitionChangeEvent event) {
        String usagePointRef = event.getSourceId();
        Optional<UsagePoint> usagePoint = this.meteringService.findUsagePointById(Long.parseLong(usagePointRef));
        if (usagePoint.isPresent()) {
            UsagePointState targetState = this.lifeCycleConfService.findUsagePointState(event.getNewState().getId()).get();
            ((UsagePointImpl) usagePoint.get()).setState(targetState, getTransitionTime(event));
        } else {
            this.logger.warning("No usage point with id = " + usagePointRef);
        }
    }

    private Instant getTransitionTime(StateTransitionChangeEvent event) {
        return event.getEffectiveTimestamp() != null ? event.getEffectiveTimestamp() : this.clock.instant();
    }
}
