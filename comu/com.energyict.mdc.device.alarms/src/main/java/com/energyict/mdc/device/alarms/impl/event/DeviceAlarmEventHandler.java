/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.Injector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class DeviceAlarmEventHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(DeviceAlarmEventHandler.class.getName());
    private final Injector injector;

    public DeviceAlarmEventHandler(Injector injector) {
        this.injector = injector;
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
        Map<?, ?> map = getJsonService().deserialize(message.getPayload(), Map.class);
        Optional<IssueEvent> event = createEvent(map);
        if (event.isPresent()) {
            getIssueCreationService().dispatchCreationEvent(Collections.singletonList(event.get()));
        }
    }

    private Optional<IssueEvent> createEvent(Map<?, ?> map) {
        return Arrays.asList(DeviceAlarmEventDescription.values()).stream()
                .filter(eventDescription -> eventDescription.matches(map))
                .findFirst()
                .map(eventDescription -> createEventsBasedOnDescription(map, eventDescription))
                .orElse(Optional.empty());
    }

    private Optional<IssueEvent> createEventsBasedOnDescription(Map<?, ?> map, DeviceAlarmEventDescription description) {
        DeviceAlarmEvent event = injector.getInstance(description.getEventClass());
        try {
            event.init(map);
            event.wrap(map, description, getDeviceFromEventMap(map));
        } catch (UnableToCreateEventException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }

    private Device getDeviceFromEventMap(Map<?, ?> map) {
        Optional<Long> amrId = getLong(map, ModuleConstants.DEVICE_IDENTIFIER);
        if (amrId.isPresent()) {
            return getDeviceService().findDeviceById(amrId.get()).orElse(null);
        } else {
            return null; // providing no device requires the event implementation to 'identify' the device in another way
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
