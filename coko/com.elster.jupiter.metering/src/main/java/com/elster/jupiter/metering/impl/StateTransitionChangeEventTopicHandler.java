package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.MeteringService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.logging.Logger;

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
        String mRID = event.getSourceId();
        try {
            this.meteringService
                    .findEndDevice(mRID)
                    .map(ServerEndDevice.class::cast)
                    .ifPresent(d -> this.handle(event, d));
        }
        catch (NumberFormatException e) {
            this.logger.fine(() -> "Unable to parse end device id '" + mRID + "' as a db identifier for an EndDevice from " + StateTransitionChangeEvent.class.getSimpleName());
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