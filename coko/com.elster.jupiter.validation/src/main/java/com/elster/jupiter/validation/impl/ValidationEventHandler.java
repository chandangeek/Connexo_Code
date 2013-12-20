package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

@Component(name="com.elster.jupiter.validation.validationeventhandler", service = Subscriber.class, immediate = true)
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
            validationService.validate(entry.getKey(), entry.getValue());
        }
    }

    private Map<MeterActivation, Interval> determineScopePerMeterActivation(ReadingStorer storer) {
        Map<MeterActivation, Interval> toValidate = new HashMap<>();
        for (Map.Entry<Channel, Interval> entry : storer.getScope().entrySet()) {
            MeterActivation meterActivation = entry.getKey().getMeterActivation();
            if (!toValidate.containsKey(meterActivation)) {
                toValidate.put(meterActivation, entry.getValue());
            } else {
                Interval span = toValidate.get(meterActivation).spanToInclude(entry.getValue());
                toValidate.put(meterActivation, span);
            }
        }
        return toValidate;
    }
}
