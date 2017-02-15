/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (20:04)
 */
public class EventTypeValueFactory implements ValueFactory<EventTypes.EventType> {

    private final EventTypes eventTypes;

    public EventTypeValueFactory(EventTypes eventTypes) {
        super();
        this.eventTypes = eventTypes;
    }

    @Override
    public EventTypes.EventType fromStringValue(String stringValue) {
        for (EventTypes.EventType eventType : eventTypes.getEventTypes()) {
            if (eventType.getId().equals(stringValue)) {
                return eventType;
            }
        }
        return null;
    }

    @Override
    public String toStringValue(EventTypes.EventType object) {
        return object.getId();
    }

    @Override
    public Class<EventTypes.EventType> getValueType() {
        return EventTypes.EventType.class;
    }

    @Override
    public EventTypes.EventType valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(EventTypes.EventType object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, EventTypes.EventType value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        }
        else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, EventTypes.EventType value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        }
        else {
            builder.addNull(Types.VARCHAR);
        }
    }

}