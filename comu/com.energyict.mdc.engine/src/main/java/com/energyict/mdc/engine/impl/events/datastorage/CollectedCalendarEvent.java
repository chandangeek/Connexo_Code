/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedCalendarDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Provides an implementation for the {@link CollectedDataProcessingEvent} interface
 * that relates to a {@link CollectedCalendarDeviceCommand}.
 */
public class CollectedCalendarEvent extends AbstractCollectedDataProcessingEventImpl<CollectedCalendar> {

    public CollectedCalendarEvent(ServiceProvider serviceProvider, CollectedCalendar calendar) {
        super(serviceProvider, calendar);
    }

    @Override
    public String getDescription() {
        return CollectedCalendarDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedCalendar calendar = getPayload();

        writer.key("collectedCalendar");
        writer.object();
        writer.key("deviceIdentifier").value(calendar.getDeviceIdentifier().toString());
        if (calendar.getActiveCalendar().isPresent()) {
            writer.key("activeCalendar").value(calendar.getActiveCalendar().get());
        }
        if (calendar.getPassiveCalendar().isPresent()) {
            writer.key("passiveCalendar").value(calendar.getPassiveCalendar().get());
        }
        writer.endObject();
    }

}