package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.validation.validationeventhandler", service = Subscriber.class, immediate = true)
public class ValidationEventHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = "com/elster/jupiter/metering/reading/CREATED";

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
        if (event.getType().getTopic().equals(TOPIC)) {
            ReadingStorer storer = (ReadingStorer) event.getSource();
            handleReadingStorer(storer);
        }
    }

    private void handleReadingStorer(ReadingStorer storer) {
        Map<MeterActivation, Range<Instant>> map = determineScopePerMeterActivation(storer);
        for (Map.Entry<MeterActivation, Range<Instant>> entry : map.entrySet()) {
            validationService.validateForNewData(entry.getKey(), entry.getValue());
        }
    }

    private Map<MeterActivation, Range<Instant>> determineScopePerMeterActivation(ReadingStorer storer) {
        Map<MeterActivation, Range<Instant>> toValidate = new HashMap<>();
        for (Map.Entry<Channel, Range<Instant>> entry : storer.getScope().entrySet()) {
            MeterActivation meterActivation = entry.getKey().getMeterActivation();
            Range<Instant> adjustedInterval = adjust(entry.getKey(), entry.getValue());
            if (!toValidate.containsKey(meterActivation)) {
                toValidate.put(meterActivation, adjustedInterval);
            } else {
                Range<Instant> span = toValidate.get(meterActivation).span(adjustedInterval);
                toValidate.put(meterActivation, span);
            }
        }
        return toValidate;
    }

    private Range<Instant> adjust(Channel channel, Range<Instant> interval) {
        int minutes = channel.getMainReadingType().getMeasuringPeriod().getMinutes();
        if (minutes == 0) {
            return interval;
        }
        return Range.greaterThan(interval.lowerEndpoint().minus(minutes, ChronoUnit.MINUTES));
    }
}
