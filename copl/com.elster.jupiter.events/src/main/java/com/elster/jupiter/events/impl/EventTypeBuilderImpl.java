package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;

public class EventTypeBuilderImpl implements EventTypeBuilder {

    private final EventTypeImpl underConstruction;
    private final Clock clock;
    private final JsonService jsonService;
    private final EventConfiguration eventConfiguration;
    private final MessageService messageService;
    private final BeanService beanService;
    private final DataModel dataModel;

    public EventTypeBuilderImpl(DataModel dataModel, Clock clock, JsonService jsonService, EventConfiguration eventConfiguration, MessageService messageService, BeanService beanService, String topic) {
        this.clock = clock;
        this.jsonService = jsonService;
        this.eventConfiguration = eventConfiguration;
        this.messageService = messageService;
        this.beanService = beanService;
        this.dataModel = dataModel;
        underConstruction = EventTypeImpl.from(this.dataModel, this.clock, this.jsonService, this.eventConfiguration, this.messageService, this.beanService, topic);
    }

    @Override
    public EventTypeBuilder component(String component) {
        underConstruction.setComponent(component);
        return this;
    }

    @Override
    public EventTypeBuilder scope(String scope) {
        underConstruction.setScope(scope);
        return this;
    }

    @Override
    public EventTypeBuilder category(String category) {
        underConstruction.setCategory(category);
        return this;
    }

    @Override
    public EventTypeBuilder name(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public EventTypeBuilder shouldPublish() {
        underConstruction.setPublish(true);
        return this;
    }

    @Override
    public EventTypeBuilder shouldNotPublish() {
        underConstruction.setPublish(false);
        return this;
    }

    @Override
    public EventTypeBuilder withProperty(String name, ValueType valueType, String accessPath) {
        underConstruction.addProperty(name, valueType, accessPath);
        return this;
    }

    @Override
    public EventType create() {
        return underConstruction;
    }
}
