package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.cim.CimMessageHandlerFactory;
import com.elster.jupiter.metering.cim.OutputStreamProvider;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;

@Component(name="com.elster.jupiter.metering.cim", service = { MessageHandlerFactory.class, CimMessageHandlerFactory.class }, property = {"subscriber=METERREADINGEXPORTER", "destination=" + EventService.JUPITER_EVENTS}, immediate = true)
public class MeterReadingCreatedMessageHandlerFactory implements MessageHandlerFactory, CimMessageHandlerFactory {

    private final CompositeOutputStreamProvider outputStreamProvider = new CompositeOutputStreamProvider(Collections.<OutputStreamProvider>emptyList());
    private volatile JsonService jsonService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
    private final Marshaller marshaller = new Marshaller();

    @Override
    public MessageHandler newMessageHandler() {
        Sender messageSender = new SenderImpl(marshaller, outputStreamProvider);
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
}
