package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;
import java.util.Map;

/**
 * Created by bvn on 3/27/15.
 */
public class UpdateConnectionTaskPropertiesQueueMessage implements QueueMessage {
    public Long connectionTaskId;
    public Map<String, String> properties;

    public UpdateConnectionTaskPropertiesQueueMessage() {
    }

    public UpdateConnectionTaskPropertiesQueueMessage(Long connectionTaskId, Map<String, String> properties) {
        this.connectionTaskId = connectionTaskId;
        this.properties = properties;
    }
}
