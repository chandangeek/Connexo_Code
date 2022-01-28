package com.elster.jupiter.issue.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    BULK_CLOSE_ISSUE("BULK_CLOSE");

    private static final String NAMESPACE = "com/elster/jupiter/issues/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            EventTypeBuilder eventTypeBuilder = eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(IssueService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("issueIds", ValueType.LIST, "issueIds")
                    .withProperty("issueStatus", ValueType.STRING, "issueStatus")
                    .withProperty("comment", ValueType.STRING, "comment")
                    .withProperty("user", ValueType.STRING, "user")
                    .shouldPublish();
            eventTypeBuilder.create();
        }
    }
}
