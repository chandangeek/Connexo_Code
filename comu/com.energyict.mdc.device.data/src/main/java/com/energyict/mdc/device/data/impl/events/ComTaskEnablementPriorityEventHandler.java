package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

import java.util.Map;

/**
 * Handles events that are being sent when the priority of a {@link ComTaskEnablement} changes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.update.comtaskenablement.priority.messagehandler", service = MessageHandler.class, immediate = true)
public class ComTaskEnablementPriorityEventHandler implements MessageHandler {

    private static final String TOPIC = "com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED";

    private volatile JsonService jsonService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ServerDeviceDataService deviceDataService;

    protected ComTaskEnablementPriorityEventHandler() {
        super();
    }

    // For testing purposes
    ComTaskEnablementPriorityEventHandler(JsonService jsonService, DeviceConfigurationService deviceConfigurationService, ServerDeviceDataService deviceDataService) {
        this();
        this.setJsonService(jsonService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = (ServerDeviceDataService) deviceDataService;
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
            this.deviceDataService.preferredPriorityChanged(comTaskEnablement.getComTask(), deviceConfiguration, oldPriority, newPriority);
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

    private Integer getInteger(String key, Map<String, Object> messageProperties) {
        Object contents = messageProperties.get(key);
        if (contents instanceof Integer) {
            return (Integer) contents;
        }
        else {
            return null;
        }
    }

}