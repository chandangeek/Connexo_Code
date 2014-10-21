package com.elster.jupiter.time;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    CATEGORY_USAGE_DELETED("relativeperiodcategoryusage/DELETED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder etb) {
            EventTypeBuilder eventTypeBuilder = super.addCustomProperties(etb);
            eventTypeBuilder.withProperty("relativePeriodId", ValueType.LONG, "relativePeriodId");
            eventTypeBuilder.withProperty("relativePeriodCategoryId", ValueType.LONG, "relativePeriodCategoryId");
            return eventTypeBuilder;
        }
    };

    private static final String NAMESPACE = "com/elster/jupiter/time/";
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
                .component(TimeService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldPublish();
        this.addCustomProperties(builder).create().save();
    }

    @TransactionRequired
    public void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
