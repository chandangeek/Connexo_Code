package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.validation.validationeventhandler", service = Subscriber.class, immediate = true)
public class ValidationEventHandler extends EventHandler<LocalEvent> {

    private static final String CREATEDTOPIC = "com/elster/jupiter/metering/reading/CREATED";
    private static final String REMOVEDTOPIC = "com/elster/jupiter/metering/reading/DELETED";

    private volatile ValidationService validationService;

    public ValidationEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
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
        map.entrySet().forEach(entry -> validationService.validateForNewData(entry.getKey(), entry.getValue()));
    }

    private Map<MeterActivation, Range<Instant>> determineScopePerMeterActivation(ReadingStorer storer) {
        Map<MeterActivation, Range<Instant>> toValidate = new HashMap<>();
        for (Map.Entry<Channel, Range<Instant>> entry : storer.getScope().entrySet()) {
            MeterActivation meterActivation = entry.getKey().getMeterActivation();
            Range<Instant> adjustedInterval = adjust(entry.getKey(), entry.getValue());
            toValidate.merge(meterActivation, adjustedInterval, Range::span);            
        }
        return toValidate;
    }

    private Range<Instant> adjust(Channel channel, Range<Instant> interval) {
        int minutes = channel.getMainReadingType().getMeasuringPeriod().getMinutes();
        if (minutes == 0 || !interval.hasLowerBound()) {
            return interval;
        }
        Instant adjustedlLowerBound = interval.lowerEndpoint().minus(minutes, ChronoUnit.MINUTES);
        if (interval.hasUpperBound()) {
            return Range.range(adjustedlLowerBound, BoundType.OPEN, interval.upperEndpoint(), interval.upperBoundType());
        }
        return Range.greaterThan(adjustedlLowerBound);
    }
    
    private void handleDeleteEvent(Channel.ReadingsDeletedEvent deleteEvent) {
    	((ValidationServiceImpl) validationService).getMeterActivationValidations(deleteEvent.getChannel())
    		.forEach(meterActivationValidation -> handle(meterActivationValidation, deleteEvent));
    }
    
    private void handle(MeterActivationValidation meterActivationValidation, Channel.ReadingsDeletedEvent deleteEvent) {
    	ChannelValidation channelValidation = meterActivationValidation.getChannelValidation(deleteEvent.getChannel()).get();
    	Instant lastChecked = channelValidation.getLastChecked();
    	if (lastChecked == null) {
    		return;
    	}
    	if (deleteEvent.getReadingTimeStamps().contains(lastChecked)) {
    		Instant newLastChecked = deleteEvent.getChannel().getReadingsOnOrBefore(lastChecked, 1).stream().findFirst().map(BaseReading::getTimeStamp).orElse(null);
    		if (newLastChecked != lastChecked) {
    			((IChannelValidation) channelValidation).updateLastChecked(newLastChecked);
    			meterActivationValidation.save();
    		}
    	}
    }
    
}
