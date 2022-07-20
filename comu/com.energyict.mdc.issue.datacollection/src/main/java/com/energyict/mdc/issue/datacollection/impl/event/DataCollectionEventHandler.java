/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.DataCollectionEventMetadata;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;

import com.google.inject.Injector;
import org.osgi.service.event.EventConstants;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE;

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
                        .filter(e -> isDeviceOperational(e))
                        .collect(Collectors.toList());

                if (!filteredEvents.isEmpty()) {
                    getIssueCreationService().dispatchCreationEvent(filteredEvents);
                }
            }
        }
    }

    private boolean isDeviceOperational(IssueEvent issueEvent) {
        return issueEvent.getEndDevice()
                .flatMap(EndDevice::getState)
                .flatMap(State::getStage)
                .map(stage -> stage.getName().equals(EndDeviceStage.OPERATIONAL.getKey()))
                .orElse(Boolean.FALSE);
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
        Optional<Device> device;
        Optional<String> eventTopics = getString(map, EventConstants.EVENT_TOPIC);
        if (eventTopics.isPresent() && eventTopics.get().equals(UNKNOWN_OUTBOUND_DEVICE.getTopic())) {
            device = getString(map, ModuleConstants.MASTER_DEVICE_IDENTIFIER)
                    .flatMap(aString -> getDeviceService().findDeviceByMrid(aString));
        } else {
            device = getLong(map, ModuleConstants.DEVICE_IDENTIFIER)
                    .flatMap(aLong -> getDeviceService().findDeviceById(aLong));
        }
        if (device.isPresent()) {
            return device.get();
        } else {
            return null; // providing no device requires the event implementation to 'identify' the device in another way
        }
    }

    private void createEventsBasedOnDescription(List<IssueEvent> events, Map<?, ?> map, EventDescription description, Device device) {
        for (Map<?, ?> mapForSingleEvent : description.splitEvents(map)) {
            DataCollectionEvent dcEvent = injector.getInstance(description.getEventClass());

            if (description.getName().equalsIgnoreCase(DataCollectionResolveEventDescription.CONNECTION_LOST_AUTO_RESOLVE.getName())) {

                LOG.info("[CONM2772]UNABLE_TO_CONNECT_AUTO_RESOLVE event received for device");
                LOG.info("[CONM2772]Fetching events for device...");
                List<DataCollectionEventMetadata> eventsForDevice = getExistingEventsForDevice(device);

                eventsForDevice.forEach(eventMetaData -> {
                    LOG.info("[CONM2772]Deleting existing event : " + eventMetaData.getEventType() +
                            ", event creation time : " + eventMetaData.getCreateDateTime().atZone(ZoneId.systemDefault()) + "...");
                    eventMetaData.delete();
                });
            }

            try {
                dcEvent.wrap(mapForSingleEvent, description, device);
                if (description.validateEvent(dcEvent)) {
                    events.add(dcEvent);
                    issueDataCollectionService.logDataCollectionEventDescription(device, description.getName(), (Long) map.get("timestamp"));
                }
            } catch (UnableToCreateIssueException e) {
                LOG.severe(e.getMessage());
            }
        }
    }

    private List<DataCollectionEventMetadata> getExistingEventsForDevice(Device device){
        List<DataCollectionEventMetadata> eventsForDevice = null;

        try {
            eventsForDevice = issueDataCollectionService.getDataCollectionEventsForDevice(device);
            LOG.info("[CONM2772]############################# Existing events for device : (" + device.getId() + device.getName() + ") count is : " + eventsForDevice.size());
            eventsForDevice.forEach(eventMetaData ->
                    LOG.info("[CONM2772]############################# Existing events type : " + eventMetaData.getEventType() + ", device id : "+ eventMetaData.getDevice().getId() +
                            ", event creation time : " + eventMetaData.getCreateDateTime().atZone(ZoneId.systemDefault()))
            );
        } catch(Exception e) {
            LOG.severe(e.getMessage());
        }

        return eventsForDevice;
    }

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents != null && contents instanceof Number) {
            return Optional.of(((Number) contents).longValue());
        }
        return Optional.empty();
    }

    protected Optional<String> getString(Map<?, ?> map, String key) {
        return Optional.ofNullable(map.get(key))
                .map(String.class::cast);
    }
}
