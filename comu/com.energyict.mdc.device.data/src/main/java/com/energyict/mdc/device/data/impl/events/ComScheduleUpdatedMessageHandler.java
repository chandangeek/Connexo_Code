package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

@Component(name="com.energyict.mdc.device.data.comschedule.update.messagehandler", service = MessageHandler.class, immediate = true)
public class ComScheduleUpdatedMessageHandler implements MessageHandler {

    private static final String TOPIC = com.energyict.mdc.scheduling.events.EventType.COMSCHEDULES_UPDATED.topic();

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
            try (PreparedStatement preparedStatement = this.dataModel.getConnection(true).prepareStatement("SELECT MIN(id), MAX(id) FROM " + TableSpecs.MDCCOMTASKEXEC.name() + " WHERE comschedule = ?")){
                ResultSet resultSet = preparedStatement.getResultSet();
                resultSet.first();
                long minId = resultSet.getLong(0);
                long maxId = resultSet.getLong(1);
                propagateRecalculation(comScheduleId, minId, maxId);
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void propagateRecalculation(long comScheduleId, long minId, long maxId) {
        while(minId+1000<maxId) {
            eventService.postEvent(UpdateEventType.COMSCHEDULE.topic(), new IdRange(comScheduleId, minId, minId+999));
            minId+=1000;
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
        }
        else {
            return ((Integer) contents).longValue();
        }
    }

}