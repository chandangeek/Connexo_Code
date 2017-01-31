/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    SERVICELOCATION_CREATED("servicelocation/CREATED", true),
    SERVICELOCATION_UPDATED("servicelocation/UPDATED", true),
    SERVICELOCATION_DELETED("servicelocation/DELETED", true),
    USAGEPOINT_CREATED("usagepoint/CREATED", true),
    USAGEPOINT_UPDATED("usagepoint/UPDATED", true),
    USAGEPOINT_DELETED("usagepoint/DELETED", true),
    CHANNEL_CREATED("channel/CREATED"),
    CHANNEL_UPDATED("channel/UPDATED"),
    CHANNEL_DELETED("channel/DELETED"),
    METER_CREATED("meter/CREATED", true),
    METER_UPDATED("meter/UPDATED", true),
    METER_DELETED("meter/DELETED", true),
    METER_ACTIVATED("meter/ACTIVATED", true),
    READINGS_CREATED("reading/CREATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .create();
        }
    },
    READINGS_DELETED("reading/DELETED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("start", ValueType.LONG, "startMillis")
                    .withProperty("end", ValueType.LONG, "endMillis")
                    .withProperty("channelId", ValueType.LONG, "channelId")
                    .create();
        }
    },
    METERREADING_CREATED("meterreading/CREATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("start", ValueType.LONG, "start")
                    .withProperty("end", ValueType.LONG, "end")
                    .withProperty("meterId", ValueType.LONG, "meterId")
                    .create();
        }
    },
    READING_QUALITY_CREATED("readingquality/CREATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("readingTimestamp", ValueType.LONG, "readingTimestamp")
                    .withProperty("channelId", ValueType.LONG, "channelId")
                    .withProperty("readingQualityTypeCode", ValueType.STRING, "typeCode")
                    .create();
        }
    },
    READING_QUALITY_UPDATED("readingquality/UPDATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("readingTimestamp", ValueType.LONG, "readingTimestamp")
                    .withProperty("channelId", ValueType.LONG, "channelId")
                    .withProperty("readingQualityTypeCode", ValueType.STRING, "typeCode")
                    .create();
        }
    },
    READING_QUALITY_DELETED("readingquality/DELETED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("readingTimestamp", ValueType.LONG, "readingTimestamp")
                    .withProperty("channelId", ValueType.LONG, "channelId")
                    .withProperty("readingType", ValueType.STRING, "readingType")
                    .withProperty("readingQualityTypeCode", ValueType.STRING, "typeCode")
                    .create();
        }
    },
    END_DEVICE_EVENT_CREATED("enddeviceevent/CREATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("endDeviceId", ValueType.LONG, "endDevice.id")
                    .withProperty("endDeviceEventType", ValueType.STRING, "eventTypeCode")
                    .withProperty("eventTimestamp", ValueType.LONG, "createdDateTime.epochSecond")
                    .create();

        }
    },
    END_DEVICE_EVENT_UPDATED("enddeviceevent/UPDATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("endDeviceId", ValueType.LONG, "endDevice.id")
                    .withProperty("endDeviceEventType", ValueType.STRING, "eventTypeCode")
                    .withProperty("eventTimestamp", ValueType.LONG, "createdDateTime.epochSecond")
                    .create();
        }
    },
    SWITCH_STATE_MACHINE_FAILED("enddevice/fsm/switch/FAILED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("fsm")
                    .scope("System")
                    .withProperty("endDeviceId", ValueType.LONG, "endDeviceId")
                    .withProperty("endDeviceMRID", ValueType.STRING, "endDevice.mRID")
                    .withProperty("endDeviceStateName", ValueType.STRING, "endDeviceStateName")
                    .withProperty("oldFiniteStateMachineId", ValueType.LONG, "oldFiniteStateMachineId")
                    .withProperty("newFiniteStateMachineId", ValueType.LONG, "newFiniteStateMachineId")
                    .shouldPublish()
                    .create();
        }
    },
    METER_ACTIVATION_ADVANCED("meteractivation/ADVANCED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .shouldNotPublish()
                    .create();
        }
    },
    METROLOGYCONFIGURATION_CREATED("metrologyconfiguration/CREATED"),
    METROLOGYCONFIGURATION_UPDATED("metrologyconfiguration/UPDATED"),
    METROLOGYCONFIGURATION_DELETED("metrologyconfiguration/DELETED"),
    METROLOGY_PURPOSE_DELETED("metrologypurpose/DELETED"),
    READING_TYPE_DELIVERABLE_CREATED("readingtypedeliverable/CREATED"),
    READING_TYPE_DELIVERABLE_UPDATED("readingtypedeliverable/UPDATED"),
    READING_TYPE_DELIVERABLE_DELETED("readingtypedeliverable/DELETED"),
    METROLOGY_CONTRACT_DELETED("metrologycontract/DELETED"),;

    private static final String NAMESPACE = "com/elster/jupiter/metering/";
    private final String topic;
    private boolean hasMRID;

    EventType(String topic) {
        this.topic = topic;
    }

    EventType(String topic, boolean mRID) {
        this.topic = topic;
        this.hasMRID = mRID;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(MeteringService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id")
                .withProperty("version", ValueType.LONG, "version");
        if (hasMRID) {
            builder.withProperty("MRID", ValueType.STRING, "MRID");
        }
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

    public static class MeterActivationAdvancedEvent {
        private final MeterActivation advanced;
        private final MeterActivation shrunk;

        public MeterActivationAdvancedEvent(MeterActivation advanced, MeterActivation shrunk) {
            this.advanced = advanced;
            this.shrunk = shrunk;
        }

        public MeterActivation getAdvanced() {
            return advanced;
        }

        public MeterActivation getShrunk() {
            return shrunk;
        }
    }
}
