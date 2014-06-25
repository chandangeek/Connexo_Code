package com.elster.jupiter.properties;

import java.util.TimeZone;

public class TimeZoneFactory implements ValueFactory<TimeZone> {

    @Override
    public Class<TimeZone> getValueType () {
        return TimeZone.class;
    }

    @Override
    public TimeZone fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return TimeZone.getTimeZone(stringValue);
        }
    }

    @Override
    public String toStringValue (TimeZone timeZone) {
        return timeZone.getID();
    }
    
    @Override
    public boolean isReference() {
        return false;
    }
}