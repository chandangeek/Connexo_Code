package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.TransactionRequired;

//TODO delete this class when events will be sent by MDC
/**
 * This class can be used only in test purpose while MDC hasn't correct implementation
 */
@Deprecated
public enum IssueEventType {
    UNKNOWN_INBOUND_DEVICE("inboundcommunication/UNKNOWNDEVICE"),
    UNKNOWN_OUTBOUND_DEVICE("outboundcommunication/UNKNOWNSLAVEDEVICE"),
    DEVICE_COMMUNICATION_FAILURE("comtask/FAILURE"),
    DEVICE_CONNECTION_SETUP_FAILURE("connectiontasksetup/FAILURE"),
    DEVICE_CONNECTION_FAILURE("connectiontask/FAILURE"),
    DEVICE_EVENT("deviceevent/CREATED"){
        @Override
        EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.withProperty("eventIdentifier", ValueType.STRING, "eventIdentifier");
        }
    };

    private static final String NAMESPACE = "com/energyict/mdc/";
    private final String topic;

    private IssueEventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    public static IssueEventType getEventTypeByTopic(String topicName) {
        if (topicName != null) {
            for (IssueEventType column : IssueEventType.values()) {
                if (column.topic().equalsIgnoreCase(topicName)) {
                    return column;
                }
            }
        }
        return null;
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
