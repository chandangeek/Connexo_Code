package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableMap;
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
        Map<MeterActivation, Map<Channel, Range<Instant>>> map = determineScopePerMeterActivation(storer);
        map.entrySet().forEach(entry -> validationService.validate(entry.getKey(), entry.getValue()));
    }
    
    private Map<MeterActivation, Map<Channel, Range<Instant>>> determineScopePerMeterActivation(ReadingStorer storer) {
    	// java compiler's type inference does not swallow the one liner construction
    	Collector<Map.Entry<Channel,Range<Instant>>, ?, Map<MeterActivation, Map<Channel,Range<Instant>>>> collector =
    		Collectors.groupingBy(
    			entry -> entry.getKey().getMeterActivation(), 
    			Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    	return storer.getScope().entrySet().stream().collect(collector);    	
    }
     
    private void handleDeleteEvent(Channel.ReadingsDeletedEvent deleteEvent) {
    	validationService.validate(deleteEvent.getChannel().getMeterActivation(), ImmutableMap.of(deleteEvent.getChannel(), deleteEvent.getRange()));
    }
    
}
