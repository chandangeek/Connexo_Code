package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.validation.validationeventhandler", service = Subscriber.class, immediate = true)
public class ValidationEventHandler extends EventHandler<LocalEvent> {

    private static final String CREATEDTOPIC = EventType.READINGS_CREATED.topic();
    private static final String REMOVEDTOPIC = EventType.READINGS_DELETED.topic();
    private static final String ADVANCEDTOPIC = EventType.METER_ACTIVATION_ADVANCED.topic();

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
            if (!StorerProcess.ESTIMATION.equals(storer.getStorerProcess()) && !StorerProcess.CONFIRM.equals(storer.getStorerProcess())) {
                handleReadingStorer(storer);
            }
        }
        if (event.getType().getTopic().equals(REMOVEDTOPIC)) {
        	Channel.ReadingsDeletedEvent deleteEvent = (Channel.ReadingsDeletedEvent) event.getSource();
        	handleDeleteEvent(deleteEvent);
        }
        if (event.getType().getTopic().equals(ADVANCEDTOPIC)) {
            EventType.MeterActivationAdvancedEvent advanceEvent = (EventType.MeterActivationAdvancedEvent) event.getSource();
            handleAdvanceEvent(advanceEvent);
        }
    }

    private void handleAdvanceEvent(EventType.MeterActivationAdvancedEvent advanceEvent) {
        validationService.getStoredChannelsContainerValidations(advanceEvent.getAdvanced())
                .stream()
                .forEach(iMeterActivationValidation -> {
                    iMeterActivationValidation.getChannelValidations()
                            .forEach(iChannelValidation -> iChannelValidation.updateLastChecked(advanceEvent.getAdvanced().getStart()));
                    iMeterActivationValidation.save();
                });
        if (advanceEvent.getShrunk() != null) {
            validationService.getStoredChannelsContainerValidations(advanceEvent.getShrunk())
                    .stream()
                    .forEach(iMeterActivationValidation -> {
                        iMeterActivationValidation.getChannelValidations()
                                .forEach(iChannelValidation -> {
                                    Instant end = advanceEvent.getShrunk().getEnd();
                                    if (iChannelValidation.getLastChecked() != null && end.isBefore(iChannelValidation.getLastChecked())) {
                                        iChannelValidation.updateLastChecked(end);
                                    }
                                    ;
                                });
                        iMeterActivationValidation.save();
                    });
        }
    }

    private void handleReadingStorer(ReadingStorer storer) {
        Map<ChannelsContainer, Map<Channel, Range<Instant>>> map = determineScopePerChannelContainer(storer);
        map.entrySet().forEach(entry -> validationService.validate(entry.getKey(), entry.getValue()));
    }

    private Map<ChannelsContainer, Map<Channel, Range<Instant>>> determineScopePerChannelContainer(ReadingStorer storer) {
        Map<CimChannel, Range<Instant>> scope = storer.getScope();

        //Collector<Map.Entry<CimChannel, Range<Instant>>, Range<Instant>, Range<Instant>> merger =
        Map<Channel, Range<Instant>> byChannel = scope.entrySet().stream().collect(
                HashMap::new,
                (map, entry) -> {
                    map.computeIfPresent(entry.getKey().getChannel(), (channel, range) -> range.span(entry.getValue()));
                    map.computeIfAbsent(entry.getKey().getChannel(), channel -> entry.getValue());
                },
                (map1, map2) -> {} // no combiner, since we don't do this in parallel
        );
        return byChannel.entrySet().stream().collect(
                HashMap::new,
                (map, entry) -> {
                    map.computeIfAbsent(entry.getKey().getChannelsContainer(), meterActivation -> new HashMap<>());
                    map.computeIfPresent(entry.getKey().getChannelsContainer(), (meterActivation, map1) -> {
                        map1.put(entry.getKey(), entry.getValue());
                        return map1;
                    });
                },
                (map1, map2) -> {} // no combiner, since we don't do this in parallel
        );
    }

    private void handleDeleteEvent(Channel.ReadingsDeletedEvent deleteEvent) {
        validationService.validate(deleteEvent.getChannel().getChannelsContainer(), ImmutableMap.of(deleteEvent.getChannel(), deleteEvent.getRange()));
    }

}
