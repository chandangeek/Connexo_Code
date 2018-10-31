/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.webservices.demo.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component(name = "MeterReadingsEventHandler", service = TopicHandler.class, immediate = true)
public class MeterReadingsEventHandler implements TopicHandler {

    private static final Logger LOGGER = Logger.getLogger(MeterReadingsEventHandler.class.getName());
    private volatile SendMeterReadingsProvider sendMeterReadingsProvider;

    public MeterReadingsEventHandler() {
    }

    @Inject
    public MeterReadingsEventHandler(SendMeterReadingsProvider sendMeterReadingsProvider) {
        this();
        setSendMeterReadingsProvider(sendMeterReadingsProvider);
    }

    @Reference
    public void setSendMeterReadingsProvider(SendMeterReadingsProvider sendMeterReadingsProvider) {
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            ReadingStorer readingStorer = (ReadingStorer) localEvent.getSource();
            sendMeterReadingsProvider.send(readingStorer, readingStorer.getStorerProcess().equals(StorerProcess.DEFAULT));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.READINGS_CREATED.topic();
    }
}