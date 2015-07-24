package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import org.osgi.service.event.EventConstants;

import java.util.Map;

/**
 * Handles events that are being sent when the priority of a {@link ComTaskEnablement} changes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
public class ComTaskEnablementPriorityMessageHandler implements MessageHandler {

    private static final String TOPIC = EventType.COMTASKENABLEMENT_PRIORITY_UPDATED.topic();

    private final JsonService jsonService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ServerCommunicationTaskService communicationTaskService;

    public ComTaskEnablementPriorityMessageHandler(JsonService jsonService, DeviceConfigurationService deviceConfigurationService, ServerCommunicationTaskService communicationTaskService) {
        super();
        this.jsonService = jsonService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        if (TOPIC.equals(topic)) {
            long comTaskEnablementId = this.getLong("comTaskEnablementId", messageProperties);
            ComTaskEnablement comTaskEnablement = this.deviceConfigurationService.findComTaskEnablement(comTaskEnablementId).get();
            DeviceConfiguration deviceConfiguration = comTaskEnablement.getDeviceConfiguration();
            Integer oldPriority = this.getInteger("oldPriority", messageProperties);
            Integer newPriority = this.getInteger("newPriority", messageProperties);
            this.communicationTaskService.preferredPriorityChanged(comTaskEnablement.getComTask(), deviceConfiguration, oldPriority, newPriority);
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

    private Integer getInteger(String key, Map<String, Object> messageProperties) {
        Object contents = messageProperties.get(key);
        if (contents instanceof Integer) {
            return (Integer) contents;
        } else {
            return null;
        }
    }

}