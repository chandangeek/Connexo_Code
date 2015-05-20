package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides factory services for the component that will
 * handle {@link SwitchStateMachineEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-20 (10:03)
 */
@Component(name = "com.elster.jupiter.metering.fsm.switch.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + SwitchStateMachineEvent.SUBSCRIBER,
                    "destination=" + SwitchStateMachineEvent.DESTINATION},
        immediate = true)
@SuppressWarnings("unused")
public class SwitchStateMachineEventHandlerFactory implements MessageHandlerFactory {

    private volatile JsonService jsonService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile EventService eventService;
    private volatile ServerMeteringService meteringService;

    // For OSGi purposes
    public SwitchStateMachineEventHandlerFactory() {
        super();
    }

    // For testing purposes
    public SwitchStateMachineEventHandlerFactory(JsonService jsonService, FiniteStateMachineService finiteStateMachineService, EventService eventService, ServerMeteringService meteringService) {
        this();
        this.setJsonService(jsonService);
        this.setFiniteStateMachineService(finiteStateMachineService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return StateMachineSwitcher.forHandling(this.meteringService.getDataModel(), this.jsonService, this.finiteStateMachineService, this.eventService);
    }

}