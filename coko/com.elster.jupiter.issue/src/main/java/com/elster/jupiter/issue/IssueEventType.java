package com.elster.jupiter.issue;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

//TODO delete this class when events will be sent by MDC
public enum IssueEventType {
    UNKNOWN_INBOUND_DEVICE("inboundcommunication/UNKNOWNDEVICE"),
    UNKNOWN_OUTBOUND_DEVICE("outboundcommunication/UNKNOWNSLAVEDEVICE"),
    DEVICE_COMMUNICATION_FAILURE("comtask/FAILURE"),
    DEVICE_CONNECTION_SETUP_FAILURE("connectiontasksetup/FAILURE"),
    DEVICE_CONNECTION_FAILURE("connectiontask/FAILURE");

    private static final String NAMESPACE = "com/energyict/mdc/isu/";
    private final String topic;

    IssueEventType(String topic) {
        this.topic = topic;
    }

    IssueEventType(String topic, String reason) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    public static IssueEventType getEventTypeByTopic(Object topicName) {
        IssueEventType type = null;
        if(UNKNOWN_OUTBOUND_DEVICE.topic().equals(topicName)) {
            type = UNKNOWN_OUTBOUND_DEVICE;
        } else if (UNKNOWN_INBOUND_DEVICE.topic().equals(topicName)) {
            type = UNKNOWN_INBOUND_DEVICE;
        } else if (DEVICE_COMMUNICATION_FAILURE.topic().equals(topicName)) {
            type = DEVICE_COMMUNICATION_FAILURE;
        } else if (DEVICE_CONNECTION_SETUP_FAILURE.topic().equals(topicName)) {
            type = DEVICE_CONNECTION_SETUP_FAILURE;
        } /*else if (DEVICE_CONNECTION_FAILURE.topic().equals(topicName)) {
            type = DEVICE_CONNECTION_FAILURE;
        }*/
        return type;
    }

        @TransactionRequired
        public void install(EventService eventService) {
            EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(IssueService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System")
                    .shouldPublish()
                    .withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier");

            addCustomProperties(builder).create().save();
        }
    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
