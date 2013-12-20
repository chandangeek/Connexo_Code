package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class EventTypeImpl implements EventType, PersistenceAware {

    private String topic;
    private String component;
    private String scope;
    private String category;
    private String name;
    private boolean publish = true;
    private List<EventPropertyType> eventPropertyTypes;
    private transient boolean fromDB = true;

    @SuppressWarnings("unused")
	private EventTypeImpl() {
    }

    public EventTypeImpl(String topic) {
        this.topic = topic;
        fromDB = false;
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
        this.topic = topic;
    }

    private List<EventPropertyType> propertyTypes() {
        if (eventPropertyTypes == null) {
            eventPropertyTypes = new ArrayList<>(loadPropertyTypes());
        }
        return eventPropertyTypes;
    }

    private List<EventPropertyType> loadPropertyTypes() {
        return propertyFactory().find("eventType", this);
    }

    @Override
    public LocalEvent create(Object source) {
        return new LocalEventImpl(this, source);
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
            Bus.getOrmClient().getEventTypeFactory().update(this);
        } else {
            Bus.getOrmClient().getEventTypeFactory().persist(this);
            fromDB = true;
        }
    }

    private DataMapper<EventPropertyType> propertyFactory() {
        return Bus.getOrmClient().getEventTypePropertyFactory();
    }
}
