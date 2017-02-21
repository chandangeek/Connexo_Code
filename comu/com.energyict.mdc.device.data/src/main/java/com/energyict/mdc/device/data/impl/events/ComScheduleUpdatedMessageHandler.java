/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.UpdateEventType;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class ComScheduleUpdatedMessageHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(ComScheduleUpdatedMessageHandler.class.getName());
    private static final String TOPIC = com.energyict.mdc.scheduling.events.EventType.COMSCHEDULES_UPDATED.topic();
    private static final int RECALCULATION_BATCH_SIZE = 1000;

    private volatile DataModel dataModel;
    private volatile JsonService jsonService;
    private volatile EventService eventService;

    public ComScheduleUpdatedMessageHandler() {
        super();
    }

    // For testing purposes
    ComScheduleUpdatedMessageHandler(JsonService jsonService, EventService eventService, DataModel dataModel) {
        this();
        this.dataModel = dataModel;
        this.setJsonService(jsonService);
        this.setEventService(eventService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        if (TOPIC.equals(topic)) {
            long comScheduleId = this.getLong("id", messageProperties);

            try (Connection connection = this.dataModel.getConnection(true);
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT MIN(id), MAX(id) FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " WHERE comschedule = ?")) {
                try (ResultSet resultSet = preparedStatement.getResultSet()) {
                    resultSet.first();
                    long minId = resultSet.getLong(0);
                    long maxId = resultSet.getLong(1);
                    propagateRecalculation(comScheduleId, minId, maxId);
                    resultSet.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void propagateRecalculation(long comScheduleId, long minId, long maxId) {
        while (minId + RECALCULATION_BATCH_SIZE < maxId) {
            eventService.postEvent(UpdateEventType.COMSCHEDULE.topic(), new IdRange(comScheduleId, minId, minId + (RECALCULATION_BATCH_SIZE - 1)));
            minId += RECALCULATION_BATCH_SIZE;
        }
        eventService.postEvent(UpdateEventType.COMSCHEDULE.topic(), new IdRange(comScheduleId, minId, maxId));
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    class IdRange {
        public long comScheduleId;
        public long minId;
        public long maxId;

        IdRange(long comScheduleId, long minId, long maxId) {
            this.comScheduleId = comScheduleId;
            this.minId = minId;
            this.maxId = maxId;
        }
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