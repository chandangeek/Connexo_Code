package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;

/**
 * Created by bvn on 7/16/15.
 */
public enum ConnectionTaskType implements ConnectionTaskCreator {
    Inbound {
        @Override
        public ConnectionTask<?, ?> createTask(ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, PartialConnectionTask partialConnectionTask) {
            return factory.createInboundConnectionTask(info, device, partialConnectionTask);
        }
    },
    Outbound {
        @Override
        public ConnectionTask<?, ?> createTask(ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, PartialConnectionTask partialConnectionTask) {
            return factory.createOutboundConnectionTask(info, device, partialConnectionTask);
        }
    };

    public static ConnectionTaskType from(ConnectionTask<?,?> connectionTask) {
        return (InboundConnectionTask.class.isAssignableFrom(connectionTask.getClass()))?ConnectionTaskType.Inbound:ConnectionTaskType.Outbound;
    }

    public static ConnectionTaskType from(PartialConnectionTask connectionTask) {
        return (PartialInboundConnectionTask.class.isAssignableFrom(connectionTask.getClass()))?ConnectionTaskType.Inbound:ConnectionTaskType.Outbound;
    }

}

interface ConnectionTaskCreator {
    public ConnectionTask<?,?> createTask(ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, PartialConnectionTask partialConnectionTask);
}
