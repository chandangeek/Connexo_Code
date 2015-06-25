package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.MeteringService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * Responds to {@link StateTransitionChangeEvent} and set CIM device lifecycle dates
 * on the related {@link com.elster.jupiter.metering.EndDevice}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-11 (11:04)
 */
@Component(name = "com.elster.jupiter.metering.fsm.state.change.handler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class StateTransitionChangeEventTopicHandler implements TopicHandler {

    private Logger logger = Logger.getLogger(StateTransitionChangeEventTopicHandler.class.getName());
    private volatile FiniteStateMachineService stateMachineService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;

    // For OSGi purposes
    public StateTransitionChangeEventTopicHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public StateTransitionChangeEventTopicHandler(FiniteStateMachineService stateMachineService, MeteringService meteringService, Clock clock) {
        this();
        this.setStateMachineService(stateMachineService);
        this.setMeteringService(meteringService);
        this.setClock(clock);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        DefaultState
            .from(event.getNewState())
            .ifPresent(state -> this.handle(event, state));
    }

    private void handle(StateTransitionChangeEvent event, DefaultState newState) {
        String mRID = event.getSourceId();
        try {
            this.meteringService
                    .findEndDevice(mRID)
                    .ifPresent(d -> this.handle(event, d, newState));
        }
        catch (NumberFormatException e) {
            this.logger.fine(() -> "Unable to parse end device id '" + mRID + "' as a db identifier for an EndDevice from " + StateTransitionChangeEvent.class.getSimpleName());
        }
    }

    private void handle(StateTransitionChangeEvent event, EndDevice device, DefaultState newState) {
        LifecycleDates lifecycleDates = device.getLifecycleDates();
        Instant effectiveTimestamp = this.effectiveTimestampFrom(event);
        switch (newState) {
            case IN_STOCK: {
                // Initial newState is not posted as an event
                break;
            }
            case ACTIVE: {
                lifecycleDates.setInstalledDate(effectiveTimestamp);
                device.save();
                break;
            }
            case INACTIVE: {
                DefaultState
                    .from(event.getOldState())
                    .ifPresent(oldState -> this.handleDeactivation(device, oldState, lifecycleDates, effectiveTimestamp));
                break;
            }
            case DECOMMISSIONED: {
                lifecycleDates.setRetiredDate(effectiveTimestamp);
                device.save();
                break;
            }
            default: {
                // No CIM date for this newState so ignore it
            }
        }
    }

    private void handleDeactivation(EndDevice device, DefaultState oldState, LifecycleDates lifecycleDates, Instant effectiveTimestamp) {
        if (DefaultState.ACTIVE.equals(oldState)) {
            lifecycleDates.setRemovedDate(effectiveTimestamp);
            device.save();
        }
    }

    private Instant effectiveTimestampFrom(StateTransitionChangeEvent event) {
        Instant effectiveTimestamp = event.getEffectiveTimestamp();
        if (effectiveTimestamp == null) {
            effectiveTimestamp = this.clock.instant();
        }
        return effectiveTimestamp;
    }

    @Reference
    public void setStateMachineService(FiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getTopicMatcher() {
        return this.stateMachineService.stateTransitionChangeEventTopic();
    }

}