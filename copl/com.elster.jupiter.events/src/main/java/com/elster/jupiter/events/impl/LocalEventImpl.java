package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.InvalidPropertyTypeException;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocalEventImpl implements LocalEvent {

    private final Object source;
    private final EventType type;
    private final Date dateTime;
    private final JsonService jsonService;
    private final EventConfiguration eventService;
    private final MessageService messageService;
    private final BeanService beanService;
    private final Thesaurus thesaurus;

    LocalEventImpl(Date dateTime, JsonService jsonService, EventConfiguration eventService, MessageService messageService, BeanService beanService, EventType type, Object source, Thesaurus thesaurus) {
        this.type = type;
        this.source = source;
        this.jsonService = jsonService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.beanService = beanService;
        this.dateTime = dateTime;
        this.thesaurus = thesaurus;
    }

    @Override
    public Date getDateTime() {
        return dateTime;
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public Event toOsgiEvent() {
        return new Event(getType().getTopic(), extractProperties());
    }

    @Override
    public void publish() {
        String payload = jsonService.serialize(extractProperties());
        getEventDestination().message(payload).send();

    }

    @Override
    public Object getSource() {
        return source;
    }

    private DestinationSpec getEventDestination() {
        String name = eventService.getEventDestinationName();
        return messageService.getDestinationSpec(name).get();
    }

    private Map<String, Object> extractProperties() {
        Map<String, Object> result = new HashMap<>();
        for (EventPropertyType eventPropertyType : getType().getPropertyTypes()) {
            Object value = getValue(eventPropertyType);
            result.put(eventPropertyType.getName(), value);
        }        
        result.put(EventConstants.TIMESTAMP, dateTime.getTime());
        return result;
    }

    private Object getValue(EventPropertyType eventPropertyType) {
        Object value = evaluateAccessPath(eventPropertyType);
        if (value != null && !eventPropertyType.getValueType().getType().isInstance(value)) {
            throw new InvalidPropertyTypeException(source, eventPropertyType.getAccessPath(), eventPropertyType.getValueType().getType(), value.getClass(), thesaurus);
        }
        return value;
    }

    private Object evaluateAccessPath(EventPropertyType eventPropertyType) {
        String[] accessPath = eventPropertyType.getAccessPath().split("\\.");
        Object current = source;
        for (String property : accessPath) {
            current = beanService.get(current, property);
            if (current == null) {
            	return current;
            }
        }
        return current;
    }
}
