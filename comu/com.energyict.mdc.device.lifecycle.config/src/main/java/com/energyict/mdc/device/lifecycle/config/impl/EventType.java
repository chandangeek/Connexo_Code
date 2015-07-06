package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.fsm.impl.FiniteStateMachineServiceImpl;
import com.elster.jupiter.orm.TransactionRequired;

/**
 * Models the different event types that are produced
 * by this device life cycle configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (15:53)
 */
public enum EventType {

    START_BPM("bpm/START");

    private static final String NAMESPACE = "com/energyict/mdc/device/lifecycle/config/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(FiniteStateMachineServiceImpl.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
        this.shouldPublish(builder).create().save();
    }

    EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.shouldNotPublish();
    }

}