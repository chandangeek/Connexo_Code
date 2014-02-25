package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class EventPropertyTypeImpl implements EventPropertyType {

    private String eventTypeTopic; // used for persistence TODO : replace with Reference to EventType
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
    
}
