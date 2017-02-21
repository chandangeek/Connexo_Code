/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.ScheduledComTaskExecutionIdRange;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;

import org.osgi.service.event.EventConstants;

import java.util.Map;
import java.util.Optional;

/**
 * Handles the background process that completes the obsoletion of
 * {@link com.energyict.mdc.scheduling.model.ComSchedule}s
 * that are still in use by at least one Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-08 (16:12)
 */
public class ComScheduleBackgroundObsoleteHandler implements MessageHandler {

    static final String START_TOPIC = com.energyict.mdc.scheduling.events.EventType.COMSCHEDULES_OBSOLETED.topic();
    static final String RANGE_OBSOLETE_TOPIC = EventType.COMTASKEXECUTION_RANGE_OBSOLETE.topic();
    static final int RECALCULATION_BATCH_SIZE = 1000;

    private final JsonService jsonService;
    private final EventService eventService;
    private final ServerCommunicationTaskService communicationTaskService;

    ComScheduleBackgroundObsoleteHandler(JsonService jsonService, EventService eventService, ServerCommunicationTaskService communicationTaskService) {
        super();
        this.jsonService = jsonService;
        this.eventService = eventService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        if (START_TOPIC.equals(topic)) {
            this.startBackgroundObsoletion(messageProperties);
        } else if (RANGE_OBSOLETE_TOPIC.equals(topic)) {
            this.obsoleteComTaskExecutionsInRange(messageProperties);
        }
    }

    private void startBackgroundObsoletion(Map<String, Object> messageProperties) {
        long comScheduleId = this.getLong("id", messageProperties);
        Optional<ScheduledComTaskExecutionIdRange> idRange = this.communicationTaskService.getScheduledComTaskExecutionIdRange(comScheduleId);
        if (idRange.isPresent()) {
            this.postBatchJobs(idRange.get());
        }
    }

    private void postBatchJobs(ScheduledComTaskExecutionIdRange idRange) {
        long minId = idRange.minId;
        while (minId + RECALCULATION_BATCH_SIZE < idRange.maxId) {
            this.eventService.postEvent(RANGE_OBSOLETE_TOPIC, new ScheduledComTaskExecutionIdRange(idRange.comScheduleId, minId, minId + (RECALCULATION_BATCH_SIZE - 1)));
            minId = minId + RECALCULATION_BATCH_SIZE;
        }
        this.eventService.postEvent(RANGE_OBSOLETE_TOPIC, new ScheduledComTaskExecutionIdRange(idRange.comScheduleId, minId, idRange.maxId));
    }

    private void obsoleteComTaskExecutionsInRange(Map<String, Object> messageProperties) {
        long comScheduleId = this.getLong("comScheduleId", messageProperties);
        long minId = this.getLong("minId", messageProperties);
        long maxId = this.getLong("maxId", messageProperties);
        this.communicationTaskService.obsoleteComTaskExecutionsInRange(new ScheduledComTaskExecutionIdRange(comScheduleId, minId, maxId));
    }

    private Long getLong(String key, Map<String, Object> messageProperties) {
        Object contents = messageProperties.get(key);
        if (contents instanceof Long) {
            return (Long) contents;
        } else {
            return ((Integer) contents).longValue();
        }
    }

}