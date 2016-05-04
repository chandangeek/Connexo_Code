package com.energyict.mdc.protocol.api.calendars;

import java.util.Arrays;
import java.util.Optional;

public enum ProtocolSupportedCalendarOptions {
    UPLOAD_CALENDAR_AND_ACTIVATE_LATER("install"),
    UPLOAD_CALENDAR_AND_ACTIVATE_IMMEDIATE("active"),
    UPLOAD_CALENDAR_AND_ACTIVATE_WITH_DATE("activateOnDate");

    private String id;

    ProtocolSupportedCalendarOptions(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Optional<ProtocolSupportedCalendarOptions> from(String id) {
        return Arrays.stream(values()).filter(option -> option.getId().equals(id)).findFirst();
    }
}
