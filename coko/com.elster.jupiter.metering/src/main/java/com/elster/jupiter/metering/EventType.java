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
    READINGS_CREATED("reading/CREATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MeteringService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .create().save();
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
                    .create().save();
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
                    .create().save();
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
                    .create().save();
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
                    .create().save();
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
                    .create().save();
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
                    .create().save();

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
                    .create().save();
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
                    .create()
                    .save();
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
                    .create().save();
        }
    };

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
        addCustomProperties(builder).create().save();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }


}
