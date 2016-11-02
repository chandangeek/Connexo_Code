package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;

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
    private UsagePointLifeCycleService lifeCycleService;
    private ServerUsagePointLifeCycleExecutionService lifeCycleExecutionService;

    @SuppressWarnings("unused") //OSGI
    public UsagePointStateChangeEventHandler() {
    }

    @Inject // Tests
    public UsagePointStateChangeEventHandler(Clock clock,
                                             MeteringService meteringService,
                                             FiniteStateMachineService stateMachineService,
                                             UsagePointLifeCycleService lifeCycleService,
                                             ServerUsagePointLifeCycleExecutionService lifeCycleExecutionService) {
        this.setClock(clock);
        this.setMeteringService(meteringService);
        this.setStateMachineService(stateMachineService);
        this.setLifeCycleService(lifeCycleService);
        this.setLifeCycleExecutionService(lifeCycleExecutionService);
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
    public void setLifeCycleService(UsagePointLifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    @Reference
    public void setLifeCycleExecutionService(ServerUsagePointLifeCycleExecutionService lifeCycleExecutionService) {
        this.lifeCycleExecutionService = lifeCycleExecutionService;
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
        Optional<UsagePoint> usagePoint = this.meteringService.findUsagePoint(usagePointRef);
        if (usagePoint.isPresent()) {
            UsagePointState targetState = this.lifeCycleService.findUsagePointState(event.getNewState().getId()).get();
            this.lifeCycleExecutionService.performTransition(usagePoint.get(), targetState, getTransitionTime(event));
        } else {
            this.logger.warning("No usage point with id = " + usagePointRef);
        }
    }

    private Instant getTransitionTime(StateTransitionChangeEvent event) {
        return event.getEffectiveTimestamp() != null ? event.getEffectiveTimestamp() : this.clock.instant();
    }
}
