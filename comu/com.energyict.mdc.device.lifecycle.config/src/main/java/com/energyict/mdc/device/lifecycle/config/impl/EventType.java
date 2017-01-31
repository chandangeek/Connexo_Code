/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import static com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService.EVENT_NAMESPACE;

/**
 * Models the different event types that are produced
 * by this device life cycle configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (15:53)
 */
public enum EventType {

    START_BPM("bpm/START");

    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return EVENT_NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(DeviceLifeCycleConfigurationService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldNotPublish()
                .create();
    }

}