/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * Created by bvn on 7/16/15.
 */
public enum ConnectionTaskType implements ConnectionTaskCreator, ConnectionTaskUpdater {
    Inbound {
        @Override
        public ConnectionTask<?, ?> updateTask(long connectionTaskId, ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, ConnectionTask<?, ?> connectionTask) {
            return factory.updateInboundConnectionTask(connectionTaskId, info, device, connectionTask);
        }

        @Override
        public ConnectionTask<?, ?> createTask(ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, PartialConnectionTask partialConnectionTask) {
            return factory.createInboundConnectionTask(info, device, partialConnectionTask);
        }
    },
    Outbound {
        @Override
        public ConnectionTask<?, ?> updateTask(long connectionTaskId, ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, ConnectionTask<?, ?> connectionTask) {
            return factory.updateScheduledConnectionTask(connectionTaskId, info, device, connectionTask);
        }

        @Override
        public ConnectionTask<?, ?> createTask(ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, PartialConnectionTask partialConnectionTask) {
            return factory.createScheduledConnectionTask(info, device, partialConnectionTask);
        }
    };

    public static ConnectionTaskType from(ConnectionTask<?,?> connectionTask) {
        return (InboundConnectionTask.class.isAssignableFrom(connectionTask.getClass()))?ConnectionTaskType.Inbound:ConnectionTaskType.Outbound;
    }

    public static ConnectionTaskType from(PartialConnectionTask connectionTask) {
        return (PartialInboundConnectionTask.class.isAssignableFrom(connectionTask.getClass()))?ConnectionTaskType.Inbound:ConnectionTaskType.Outbound;
    }
}

