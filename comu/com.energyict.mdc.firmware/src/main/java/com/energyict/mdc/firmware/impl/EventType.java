/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.firmware.FirmwareService;

public enum EventType {

    FIRMWARE_VERSION_CREATED("firmwareversion/CREATED"),
    FIRMWARE_VERSION_UPDATED("firmwareversion/UPDATED"),
    FIRMWARE_VERSION_DELETED("firmwareversion/DELETED"),
    ACTIVATED_FIRMWARE_VERSION_CREATED("activatedfirmwareversion/CREATED"),
    ACTIVATED_FIRMWARE_VERSION_UPDATED("activatedfirmwareversion/UPDATED"),
    ACTIVATED_FIRMWARE_VERSION_DELETED("activatedfirmwareversion/DELETED"),
    DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATE_TIME_BOUNDARIES("firmwarecampaign/device/UPDATETIMEBOUNDARIES") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder
                    .withProperty("deviceId", ValueType.LONG, "device.id")
                    .withProperty("firmwareCampaignId", ValueType.LONG, "firmwareCampaign.id")
                    .shouldPublish();
        }
    },
    FIRMWARE_CAMPAIGN_EDITED("firmwarecampaign/EDITED");

    private static final String NAMESPACE = "com/energyict/mdc/firmware/";
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
                .component(FirmwareService.COMPONENTNAME)
                .category("Crud")
                .scope("System");
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.withProperty("id", ValueType.LONG, "id");
    }


}
