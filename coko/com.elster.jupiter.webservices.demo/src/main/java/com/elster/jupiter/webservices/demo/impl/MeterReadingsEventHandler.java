/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.webservices.demo.impl;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import ch.iec.tc57._2011.schema.message.HeaderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component(name = "com.elster.jupiter.webservices.demo.meterreadings.MeterReadingsEventHandler", service = TopicHandler.class, immediate = true)
public class MeterReadingsEventHandler implements TopicHandler {

    private static final Logger LOGGER = Logger.getLogger(MeterReadingsEventHandler.class.getName());
    private volatile SendMeterReadingsProvider sendMeterReadingsProvider;
    private volatile EndPointConfigurationService endPointConfigurationService;

    public MeterReadingsEventHandler() {
    }

    @Inject
    public MeterReadingsEventHandler(SendMeterReadingsProvider sendMeterReadingsProvider, EndPointConfigurationService endPointConfigurationService) {
        this();
        setSendMeterReadingsProvider(sendMeterReadingsProvider);
        setEndPointConfigurationService(endPointConfigurationService);
    }

    @Reference
    public void setSendMeterReadingsProvider(SendMeterReadingsProvider sendMeterReadingsProvider) {
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            if (endPointConfigurationService
                    .getEndPointConfigurationsForWebService(SendMeterReadingsProvider.NAME)
                    .stream()
                    .anyMatch(EndPointConfiguration::isActive)) {
                ReadingStorer readingStorer = (ReadingStorer) localEvent.getSource();
                sendMeterReadingsProvider.call(readingStorer.getReadings(), readingStorer.getStorerProcess().equals(StorerProcess.DEFAULT) ? HeaderType.Verb.CREATED : HeaderType.Verb.CHANGED);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.READINGS_CREATED.topic();
    }
}