package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by bvn on 3/30/15.
 */
public class ItemizeConnectionFilterUpdatePropertiesQueueMessage implements QueueMessage, Serializable {
    public ConnectionTaskFilterSpecificationMessage connectionTaskFilterSpecification;
    public Map<String, String> propertyValues;

    public ItemizeConnectionFilterUpdatePropertiesQueueMessage() {
    }

    public ItemizeConnectionFilterUpdatePropertiesQueueMessage(ConnectionTaskFilterSpecificationMessage connectionTaskFilterSpecification, Map<String, String> properties) {
        this.connectionTaskFilterSpecification = connectionTaskFilterSpecification;
        this.propertyValues = properties;
    }
}

