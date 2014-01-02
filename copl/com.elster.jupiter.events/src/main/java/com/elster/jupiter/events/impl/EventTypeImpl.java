package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class EventTypeImpl implements EventType, PersistenceAware {

    private String topic;
    private String component;
    private String scope;
    private String category;
    private String name;
    private boolean publish = true;
    private final List<EventPropertyType> eventPropertyTypes = new ArrayList<>();
    private transient boolean fromDB = true;
    private final DataModel dataModel;
    private final Clock clock;
    private final JsonService jsonService;
    private final EventConfiguration eventConfiguration;
    private final MessageService messageService;
    private final BeanService beanService;

    @SuppressWarnings("unused")
    @Inject
	EventTypeImpl(DataModel dataModel, Clock clock, JsonService jsonService, EventConfiguration eventConfiguration, MessageService messageService, BeanService beanService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.jsonService = jsonService;
        this.eventConfiguration = eventConfiguration;
        this.messageService = messageService;
        this.beanService = beanService;
    }

    static EventTypeImpl from(DataModel dataModel, Clock clock, JsonService jsonService, EventConfiguration eventConfiguration, MessageService messageService, BeanService beanService, String topic) {
        EventTypeImpl eventType = new EventTypeImpl(dataModel, clock, jsonService, eventConfiguration, messageService, beanService);
        eventType.fromDB = false;
        return eventType.init(topic);
    }

    private EventTypeImpl init(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getComponent() {
        return component;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean shouldPublish() {
        return publish;
    }

    @Override
    public List<EventPropertyType> getPropertyTypes() {
        return ImmutableList.copyOf(propertyTypes());
    }

    void setCategory(String category) {
        this.category = category;
    }

    void setComponent(String component) {
        this.component = component;
    }

    void setName(String name) {
        this.name = name;
    }

    void setScope(String scope) {
        this.scope = scope;
    }

    void setTopic(String topic) {
        init(topic);
    }

    private List<EventPropertyType> propertyTypes() {
        return eventPropertyTypes;
    }

    @Override
    public LocalEvent create(Object source) {
        return new LocalEventImpl(clock.now(), jsonService, eventConfiguration, messageService, beanService, this, source);
    }

    @Override
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    @Override
    public EventPropertyType addProperty(String name, ValueType valueType, String accessPath) {
        EventPropertyTypeImpl eventPropertyType = new EventPropertyTypeImpl(this, name, valueType, accessPath, propertyTypes().size());
        propertyTypes().add(eventPropertyType);
        return eventPropertyType;
    }

    @Override
    public void removePropertyType(EventPropertyType eventPropertyType) {
        propertyTypes().remove(eventPropertyType);
    }

    @Override
    public void postLoad() {
        propertyTypes();
    }

    @Override
    public void save() {
        if (fromDB) {
            dataModel.mapper(EventType.class).update(this);
        } else {
            dataModel.mapper(EventType.class).persist(this);
            fromDB = true;
        }
    }

}
