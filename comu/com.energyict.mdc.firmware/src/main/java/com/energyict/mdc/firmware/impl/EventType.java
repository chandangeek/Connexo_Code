package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.firmware.FirmwareService;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 10:43
 */
public enum EventType {

    FIRMWARE_VERSION_CREATED("firmwareversion/CREATED"),
    FIRMWARE_VERSION_UPDATED("firmwareversion/UPDATED"),
    FIRMWARE_VERSION_DELETED("firmwareversion/DELETED"),
    ACTIVATED_FIRMWARE_VERSION_CREATED("activatedfirmwareversion/CREATED"),
    ACTIVATED_FIRMWARE_VERSION_UPDATED("activatedfirmwareversion/UPDATED"),
    ACTIVATED_FIRMWARE_VERSION_DELETED("activatedfirmwareversion/DELETED"),
    FIRMWARE_CAMPAIGN_CREATED("firmwarecampaign/CREATED"){
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).withProperty("deviceGroupId", ValueType.LONG, "deviceGroup.id").shouldPublish();
        }
    },
    FIRMWARE_CAMPAIGN_CANCELLED("firmwarecampaign/CANCELLED"){
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).withProperty("deviceGroupId", ValueType.LONG, "deviceGroup.id").shouldPublish();
        }
    },
    DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED("firmwarecampaign/device/CREATED"){
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder
                    .withProperty("deviceId", ValueType.LONG, "device.id")
                    .withProperty("firmwareCampaignId", ValueType.LONG, "firmwareCampaign.id")
                    .shouldPublish();
        }
    },
    DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED("firmwarecampaign/device/UPDATED"){
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).shouldPublish();
        }
    },
    DEVICE_IN_FIRMWARE_CAMPAIGN_CANCEL("firmwarecampaign/device/CANCEL"){
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder
                    .withProperty("deviceId", ValueType.LONG, "device.id")
                    .withProperty("firmwareCampaignId", ValueType.LONG, "firmwareCampaign.id")
                    .shouldPublish();
        }
    }
    ;

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
        this.addCustomProperties(builder).create().save();
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
