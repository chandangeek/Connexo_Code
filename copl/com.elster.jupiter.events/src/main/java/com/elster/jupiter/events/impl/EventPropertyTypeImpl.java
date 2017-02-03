/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.Objects;

public final class EventPropertyTypeImpl implements EventPropertyType {

    @SuppressWarnings("unused")
	private String eventTypeTopic;
    private String name;
    private ValueType valueType;
    private String accessPath;
    private int position;

    private final Reference<EventType> eventType = ValueReference.absent();

    @Inject
	private EventPropertyTypeImpl() {
    }

    EventPropertyTypeImpl(EventType eventType, String name, ValueType valueType, String accessPath, int position) {
        this.eventType.set(eventType);
        this.eventTypeTopic = eventType.getTopic();
        this.name = name;
        this.valueType = valueType;
        this.accessPath = accessPath;
        this.position = position;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public String getAccessPath() {
        return accessPath;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public EventType getEventType() {
        return eventType.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventPropertyTypeImpl that = (EventPropertyTypeImpl) o;
        return Objects.equals(eventTypeTopic, that.eventTypeTopic) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventTypeTopic, name);
    }

}