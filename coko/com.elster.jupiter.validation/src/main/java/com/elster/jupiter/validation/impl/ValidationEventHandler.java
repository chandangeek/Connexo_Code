package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;

@Component(name = "com.elster.jupiter.validation.validationeventhandler", service = Subscriber.class, immediate = true)
public class ValidationEventHandler extends EventHandler<LocalEvent> {

    private static final String CREATEDTOPIC = "com/elster/jupiter/metering/reading/CREATED";
    private static final String REMOVEDTOPIC = "com/elster/jupiter/metering/reading/DELETED";

    private volatile ValidationServiceImpl validationService;

    public ValidationEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = (ValidationServiceImpl) validationService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(CREATEDTOPIC)) {
            ReadingStorer storer = (ReadingStorer) event.getSource();
            handleReadingStorer(storer);
        }
        if (event.getType().getTopic().equals(REMOVEDTOPIC)) {
        	Channel.ReadingsDeletedEvent deleteEvent = (Channel.ReadingsDeletedEvent) event.getSource();
        	handleDeleteEvent(deleteEvent);
        }
    }

    private void handleReadingStorer(ReadingStorer storer) {
        Map<MeterActivation, Range<Instant>> map = determineScopePerMeterActivation(storer);
        map.entrySet().forEach(entry -> validationService.validate(entry.getKey(), entry.getValue().lowerEndpoint()));
    }

    private Map<MeterActivation, Range<Instant>> determineScopePerMeterActivation(ReadingStorer storer) {
        Map<MeterActivation, Range<Instant>> toValidate = new HashMap<>();
        for (Map.Entry<Channel, Range<Instant>> entry : storer.getScope().entrySet()) {
            MeterActivation meterActivation = entry.getKey().getMeterActivation();
            toValidate.merge(meterActivation, entry.getValue(), Range::span);            
        }
        return toValidate;
    }
    
    private void handleDeleteEvent(Channel.ReadingsDeletedEvent deleteEvent) {
    	validationService.validate(deleteEvent.getChannel().getMeterActivation(), deleteEvent.getRange().lowerEndpoint());
    }
    
}
