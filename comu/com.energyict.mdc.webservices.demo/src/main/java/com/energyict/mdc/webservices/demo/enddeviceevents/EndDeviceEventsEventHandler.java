/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.webservices.demo.enddeviceevents;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.webservices.demo.enddeviceevents.EndDeviceEventsEventHandler", service = TopicHandler.class, immediate = true)
public class EndDeviceEventsEventHandler implements TopicHandler {
    private static final Logger LOGGER = Logger.getLogger(EndDeviceEventsEventHandler.class.getName());
    private volatile EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;

    // OSGi
    public EndDeviceEventsEventHandler() {
        super();
    }

    @Inject
    // For testing purposes only
    public EndDeviceEventsEventHandler(EndDeviceEventsServiceProvider endDeviceEventsServiceProvider) {
        this();
        setEndDeviceEventsServiceProvider(endDeviceEventsServiceProvider);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            EndDeviceEventRecord endDeviceEventRecord = (EndDeviceEventRecord) localEvent.getSource();
            this.endDeviceEventsServiceProvider.call(endDeviceEventRecord);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.END_DEVICE_EVENT_CREATED.topic();
    }

    @Reference
    public void setEndDeviceEventsServiceProvider(EndDeviceEventsServiceProvider endDeviceEventsServiceProvider) {
        this.endDeviceEventsServiceProvider = endDeviceEventsServiceProvider;
    }
}