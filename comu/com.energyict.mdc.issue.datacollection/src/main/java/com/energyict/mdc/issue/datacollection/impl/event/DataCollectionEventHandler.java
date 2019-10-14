/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataCollectionEventHandler implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(DataCollectionEventHandler.class.getName());
    private final Injector injector;
    private final MeteringService meteringService;
    private final EventService eventService;
    private final IssueDataCollectionService issueDataCollectionService;

    public DataCollectionEventHandler(Injector injector, MeteringService meteringService, IssueDataCollectionService issueDataCollectionService, EventService eventService) {
        this.injector = injector;
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.issueDataCollectionService = issueDataCollectionService;
    }

    protected IssueCreationService getIssueCreationService() {
        return injector.getInstance(IssueCreationService.class);
    }

    protected JsonService getJsonService() {
        return injector.getInstance(JsonService.class);
    }

    protected DeviceService getDeviceService() {
        return injector.getInstance(DeviceService.class);
    }

    protected Thesaurus getThesaurus() {
        return injector.getInstance(Thesaurus.class);
    }

    @Override
    public void process(Message message) {
        List<IssueEvent> events = createEvents(getJsonService().deserialize(message.getPayload(), Map.class));
        if (events != null && !events.isEmpty()) {

            List<IssueEvent> eventsWithEndDevice = events.stream()
                    .filter(e -> e.getEndDevice().isPresent())
                    .collect(Collectors.toList());
            if (eventsWithEndDevice.isEmpty()) {
                getIssueCreationService().dispatchCreationEvent(events);
            } else {
                List<IssueEvent> filteredEvents = eventsWithEndDevice.stream()
                        .filter(e -> e.getEndDevice().get().getState().isPresent())
                        .filter(e -> e.getEndDevice().get().getState().get().getStage().isPresent())
                        .filter(e -> e.getEndDevice().get().getState().get().getStage().get().getName().equals(EndDeviceStage.OPERATIONAL.getKey()))
                        .collect(Collectors.toList());

                if (!filteredEvents.isEmpty()) {
                    getIssueCreationService().dispatchCreationEvent(filteredEvents);
                }
            }
        }
    }

    private List<IssueEvent> createEvents(Map<?, ?> map) {
        // make sure you only load the device once!
        Device device = getDeviceFromEventMap(map);

        List<IssueEvent> events = new ArrayList<>();
        Stream.concat(Arrays.stream(DataCollectionEventDescription.values()),
                Arrays.stream(DataCollectionResolveEventDescription.values()))
                .filter(description -> description.validateEvent(map))
                .forEach(description -> createEventsBasedOnDescription(events, map, description, device));
        return events;
    }

    private Device getDeviceFromEventMap(Map<?, ?> map) {
        Optional<Long> amrId = getLong(map, ModuleConstants.DEVICE_IDENTIFIER);
        if (amrId.isPresent()) {
            return getDeviceService().findDeviceById(amrId.get()).orElse(null);
        } else {
            return null; // providing no device requires the event implementation to 'identify' the device in another way
        }
    }

    private void createEventsBasedOnDescription(List<IssueEvent> events, Map<?, ?> map, EventDescription description, Device device) {
        for (Map<?, ?> mapForSingleEvent : description.splitEvents(map)) {
            issueDataCollectionService.logDataCollectionEventDescription(device, (String) map.get("event.topics"), (Long) map.get("timestamp"));
            DataCollectionEvent dcEvent = injector.getInstance(description.getEventClass());
            try {
                dcEvent.wrap(mapForSingleEvent, description, device);
                events.add(dcEvent);
            } catch (UnableToCreateIssueException e) {
                LOG.severe(e.getMessage());
            }
        }
    }

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents != null && contents instanceof Number) {
            return Optional.of(((Number) contents).longValue());
        }
        return Optional.empty();
    }
}
