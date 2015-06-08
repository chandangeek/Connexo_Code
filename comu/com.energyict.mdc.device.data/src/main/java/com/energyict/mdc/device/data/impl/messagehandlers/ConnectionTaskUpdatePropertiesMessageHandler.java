package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.UpdateConnectionTaskPropertiesQueueMessage;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This message handler will trigger connections to rerun.
 * Created by bvn on 3/25/15.
 */
public class ConnectionTaskUpdatePropertiesMessageHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(ConnectionTaskUpdatePropertiesMessageHandler.class.getSimpleName());

    private ConnectionTaskService connectionTaskService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        UpdateConnectionTaskPropertiesQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), UpdateConnectionTaskPropertiesQueueMessage.class);
        Optional<ConnectionTask> connectionTaskOptional = connectionTaskService.findConnectionTask(queueMessage.connectionTaskId);
        if (connectionTaskOptional.isPresent()) {
            ConnectionTask connectionTask = connectionTaskOptional.get();
            for (PropertySpec propertySpec : connectionTask.getConnectionType().getPropertySpecs()) {
                if (queueMessage.properties.containsKey(propertySpec.getName())) {
                    String stringValue = queueMessage.properties.get(propertySpec.getName());
                    Object convertedValue = null;
                    try {
                        convertedValue = convertPropertyStringValueToPropertyValue(propertySpec, stringValue);
                        connectionTask.setProperty(propertySpec.getName(), convertedValue);
                        LOGGER.info(String.format("Set property '%s' on connection task %d to value '%s'", propertySpec.getName(), connectionTask.getId(), convertedValue));
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, String.format("Failed to set property '%s' on connection task %d: value '%s' was refused: %s", propertySpec.getName(), connectionTask.getId(), convertedValue, e.getMessage()));
                    }
                }
            }
            connectionTask.save();
        } else {
            LOGGER.log(Level.SEVERE, "No connectionTask with id "+queueMessage.connectionTaskId);
        }
    }

    /**
     * Shamelessly copied from MdcPropertyUtils
     */
    private Object convertPropertyStringValueToPropertyValue(PropertySpec propertySpec, String value) {
        Class valueTypeClazz = propertySpec.getValueFactory().getValueType();
        if (Objects.equals(valueTypeClazz, Password.class)) {
            return new Password(value);
        } else if (Objects.equals(valueTypeClazz, Date.class)) {
            return new Date(Long.valueOf(value));
        } else if (Objects.equals(valueTypeClazz, TimeDuration.class)) {
            try { // String looks like this: '{timeUnit=hours, count=1}'
                TimeDurationJson json = jsonService.deserialize(value, TimeDurationJson.class);
                return new TimeDuration(json.count, TimeDuration.TimeUnit.forDescription(json.timeUnit));
            }
            catch (LocalizedFieldValidationException e) {
                throw new IllegalArgumentException("Invalid timeduration");
            }
        } else if (Objects.equals(valueTypeClazz, String.class)) {
            return jsonService.deserialize(value, String.class);
        } else if (Objects.equals(valueTypeClazz, Boolean.class)) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return value;
            } else {
                throw new IllegalArgumentException("Invalid Boolean value");
            }
        }
        return propertySpec.getValueFactory().fromStringValue(value);
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(ConnectionTaskService connectionTaskService, JsonService jsonService) {
        this.connectionTaskService = connectionTaskService;
        this.jsonService = jsonService;
        return this;
    }
}

class TimeDurationJson {
    public String timeUnit;
    public int count;

    public TimeDurationJson() {
    }
}
