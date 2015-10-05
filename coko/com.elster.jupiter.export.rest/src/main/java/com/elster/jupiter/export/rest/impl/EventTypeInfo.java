package com.elster.jupiter.export.rest.impl;

public class EventTypeInfo {

    public String eventFilterCode;

    public static EventTypeInfo of(String code) {
        EventTypeInfo eventTypeInfo = new EventTypeInfo();
        eventTypeInfo.eventFilterCode = code;
        return eventTypeInfo;
    }
}
