package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.MeteringService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
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
    private volatile FiniteStateMachineService stateMachineService;
    private volatile MeteringService meteringService;

    // For OSGi purposes
    public StateTransitionChangeEventTopicHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public StateTransitionChangeEventTopicHandler(FiniteStateMachineService stateMachineService, MeteringService meteringService) {
        this();
        this.setStateMachineService(stateMachineService);
        this.setMeteringService(meteringService);
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
        String endDeviceId = event.getSourceId();
        try {
            this.meteringService
                    .findEndDevice(Long.parseLong(endDeviceId))
                    .map(ServerEndDevice.class::cast)
                    .ifPresent(d -> this.handle(event, d));
        }
        catch (NumberFormatException e) {
            this.logger.fine(() -> "Unable to parse end device id '" + endDeviceId + "' as a db identifier for an EndDevice from " + StateTransitionChangeEvent.class.getSimpleName());
        }
    }

    private void handle(StateTransitionChangeEvent event, ServerEndDevice endDevice) {
        endDevice.changeState(event.getNewState());
    }

    @Override
    public String getTopicMatcher() {
        return this.stateMachineService.stateTransitionChangeEventTopic();
    }

}