/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class EventTypeImpl implements EventType, PersistenceAware {

    private String topic;
    private String component;
    private String scope;
    private String category;
    private String name;
    private boolean publish = false;
    private boolean fsmEnabled = false;
    private final List<EventPropertyType> eventPropertyTypes = new ArrayList<>();
    private final DataModel dataModel;
    private final Clock clock;
    private final JsonService jsonService;
    private final EventConfiguration eventConfiguration;
    private final MessageService messageService;
    private final BeanService beanService;
    private final Thesaurus thesaurus;

    //audit columns: all managed by ORM
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    EventTypeImpl(DataModel dataModel, Clock clock, JsonService jsonService, EventConfiguration eventConfiguration, MessageService messageService, BeanService beanService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.jsonService = jsonService;
        this.eventConfiguration = eventConfiguration;
        this.messageService = messageService;
        this.beanService = beanService;
        this.thesaurus = thesaurus;
    }

    static EventTypeImpl from(DataModel dataModel, String topic) {
        EventTypeImpl eventType = dataModel.getInstance(EventTypeImpl.class);
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
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    @Override
    public boolean isEnabledForUseInStateMachines() {
        return this.fsmEnabled;
    }

    @Override
    public void enableForUseInStateMachines() {
        this.fsmEnabled = true;
    }

    @Override
    public void disableForUseInStateMachines() {
        this.fsmEnabled = false;
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
        return new LocalEventImpl(clock.instant(), jsonService, eventConfiguration, messageService, beanService, this, source, thesaurus);
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
    public void update() {
        dataModel.mapper(EventType.class).update(this);
    }

    void save() {
        dataModel.mapper(EventType.class).persist(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventTypeImpl eventType = (EventTypeImpl) o;
        return Objects.equals(topic, eventType.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

}