/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.cim.CimMessageHandlerFactory;
import com.elster.jupiter.metering.cim.OutputStreamProvider;
import com.elster.jupiter.metering.cim.Sender;
import com.elster.jupiter.util.json.JsonService;
import java.time.Clock;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;

@Component(name="com.elster.jupiter.metering.cim", service = { MessageHandlerFactory.class, CimMessageHandlerFactory.class }, property = {"subscriber=METERREADINGEXPORTER", "destination=" + EventService.JUPITER_EVENTS}, immediate = true)
public class MeterReadingCreatedMessageHandlerFactory implements MessageHandlerFactory, CimMessageHandlerFactory {

    private final CompositeOutputStreamProvider outputStreamProvider = new CompositeOutputStreamProvider(Collections.<OutputStreamProvider>emptyList());
    private volatile JsonService jsonService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
    private final Marshaller marshaller = new Marshaller();
    private CompositeSender messageSender = new CompositeSender(Arrays.asList(new SenderImpl(marshaller, outputStreamProvider)));

    @Override
    public MessageHandler newMessageHandler() {
        return new MeterReadingCreatedMessageHandler(jsonService, meteringService, new MessageGenerator(messageSender, clock));
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {

    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void addOutputStreamProvider(OutputStreamProvider provider) {
        outputStreamProvider.addProvider(provider);
    }

    @Override
    public void removeOutputStreamProvider(OutputStreamProvider provider) {
        outputStreamProvider.removeProvider(provider);
    }

    @Override
    public void addSender(Sender sender) {
        messageSender.addSender(sender);
    }

    @Override
    public void removeSender(Sender sender) {
        messageSender.removeSender(sender);
    }
}
