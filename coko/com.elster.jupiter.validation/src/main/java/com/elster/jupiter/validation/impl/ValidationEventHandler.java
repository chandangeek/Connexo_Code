package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;
import org.joda.time.DateTimeConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
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
        Map<MeterActivation, Interval> map = determineScopePerMeterActivation(storer);
        for (Map.Entry<MeterActivation, Interval> entry : map.entrySet()) {
            validationService.validateForNewData(entry.getKey(), entry.getValue());
        }
    }

    private Map<MeterActivation, Interval> determineScopePerMeterActivation(ReadingStorer storer) {
        Map<MeterActivation, Interval> toValidate = new HashMap<>();
        for (Map.Entry<Channel, Interval> entry : storer.getScope().entrySet()) {
            MeterActivation meterActivation = entry.getKey().getMeterActivation();
            Interval adjustedInterval = adjust(entry.getKey(), entry.getValue());
            if (!toValidate.containsKey(meterActivation)) {
                toValidate.put(meterActivation, adjustedInterval);
            } else {
                Interval span = toValidate.get(meterActivation).spanToInclude(adjustedInterval);
                toValidate.put(meterActivation, span);
            }
        }
        return toValidate;
    }

    private Interval adjust(Channel channel, Interval interval) {
        int minutes = channel.getMainReadingType().getMeasuringPeriod().getMinutes();
        if (minutes == 0) {
            return interval;
        }
        return interval.withStart(new Date(interval.getStart().getTime() - DateTimeConstants.MILLIS_PER_MINUTE * minutes));
    }
}
