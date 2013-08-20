package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.InvalidPropertyTypeException;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.DestinationSpec;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocalEventImpl implements LocalEvent {

    private final Object source;
    private final EventType type;
    private final Date dateTime;

    public LocalEventImpl(EventType type, Object source) {
        this.type = type;
        this.source = source;
        dateTime = Bus.getClock().now();
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
        String payload = Bus.getJsonService().serialize(extractProperties());
        getEventDestination().message(payload).send();

    }

    @Override
    public Object getSource() {
        return source;
    }

    private DestinationSpec getEventDestination() {
        String name = Bus.getEventConfiguration().getEventDestinationName();
        return Bus.getMessageService().getDestinationSpec(name).get();
    }

    private Map<String, Object> extractProperties() {
        Map<String, Object> result = new HashMap<>();
        for (EventPropertyType eventPropertyType : getType().getPropertyTypes()) {
            Object value = getValue(eventPropertyType);
            result.put(eventPropertyType.getName(), value);
        }
        result.put(EventConstants.EVENT_TOPIC, getType().getTopic());
        result.put(EventConstants.TIMESTAMP, dateTime.getTime());
        return result;
    }

    private Object getValue(EventPropertyType eventPropertyType) {
        Object value = evaluateAccessPath(eventPropertyType);
        if (value != null && !eventPropertyType.getValueType().getType().isInstance(value)) {
            throw new InvalidPropertyTypeException(source, eventPropertyType.getAccessPath(), eventPropertyType.getValueType().getType(), value.getClass());
        }
        return value;
    }

    private Object evaluateAccessPath(EventPropertyType eventPropertyType) {
        String[] accessPath = eventPropertyType.getAccessPath().split("\\.");
        Object current = source;
        for (String property : accessPath) {
            current = Bus.getBeanService().get(current, property);
        }
        return current;
    }
}
