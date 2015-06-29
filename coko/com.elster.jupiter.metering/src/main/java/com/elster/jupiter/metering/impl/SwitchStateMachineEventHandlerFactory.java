package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

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

    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile EventService eventService;
    private volatile ServerMeteringService meteringService;

    // For OSGi purposes
    public SwitchStateMachineEventHandlerFactory() {
        super();
    }

    // For testing purposes
    @Inject
    public SwitchStateMachineEventHandlerFactory(MessageService messageService, JsonService jsonService, FiniteStateMachineService finiteStateMachineService, EventService eventService, ServerMeteringService meteringService) {
        this();
        this.setMessageService(messageService);
        this.setEventService(eventService);
        this.setJsonService(jsonService);
        this.setFiniteStateMachineService(finiteStateMachineService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
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
        return StateMachineSwitcher.forHandling(this.meteringService.getDataModel(), this.eventService, this.jsonService, this.finiteStateMachineService);
    }

}