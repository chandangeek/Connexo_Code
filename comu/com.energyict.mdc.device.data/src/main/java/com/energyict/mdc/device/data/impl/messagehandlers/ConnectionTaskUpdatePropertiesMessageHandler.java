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
        UpdateConnectionTaskPropertiesQueueMessage updateConnectionTaskPropertiesQueueMessage = jsonService.deserialize(message.getPayload(), UpdateConnectionTaskPropertiesQueueMessage.class);
        Optional<ConnectionTask> connectionTaskOptional = connectionTaskService.findConnectionTask(updateConnectionTaskPropertiesQueueMessage.connectionTaskId);
        if (connectionTaskOptional.isPresent()) {
            ConnectionTask connectionTask = connectionTaskOptional.get();
            for (PropertySpec propertySpec : connectionTask.getConnectionType().getPropertySpecs()) {
                if (updateConnectionTaskPropertiesQueueMessage.properties.containsKey(propertySpec.getName())) {
                    String stringValue = updateConnectionTaskPropertiesQueueMessage.properties.get(propertySpec.getName());
                    try {
                        connectionTask.setProperty(propertySpec.getName(), convertPropertyStringValueToPropertyValue(propertySpec, stringValue));
                        LOGGER.info(String.format("Set property '%s' on connection task %d to value '%s'", propertySpec.getName(), connectionTask.getId(), stringValue));
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, String.format("Failed to set property '%s' on connection task %d: value '%s' was refused: %s", propertySpec.getName(), connectionTask.getId(), stringValue, e.getMessage()));
                    }
                }
            }
        } else {
            LOGGER.log(Level.SEVERE, "No connectionTask with id "+updateConnectionTaskPropertiesQueueMessage.connectionTaskId);
        }
    }

    /**
     * Shamelessly copied from MdcPropertyUtils
     */
    private Object convertPropertyStringValueToPropertyValue(PropertySpec propertySpec, String value) {
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), Password.class)) {
            return new Password(value);
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), Date.class)) {
            return new Date(Long.valueOf(value));
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), TimeDuration.class)) {
            try {
                return new TimeDuration(value); // expects "" + count + " " + timeUnit
            }
            catch (LocalizedFieldValidationException e) {
                throw new IllegalArgumentException("Invalid timeduration");
            }
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), String.class)) {
            return value;
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), Boolean.class)) {
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
