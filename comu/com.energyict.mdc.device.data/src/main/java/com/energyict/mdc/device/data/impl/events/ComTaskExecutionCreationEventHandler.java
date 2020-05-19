/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.device.data.impl.EventType;

import java.util.List;
import java.util.stream.Collectors;

public class ComTaskExecutionCreationEventHandler extends EventHandler<LocalEvent> {

    private static final String DEVICE_CREATED = EventType.DEVICE_CREATED.topic();
    private static final String DEVICE_UPDATED = EventType.DEVICE_UPDATED.topic();

    public ComTaskExecutionCreationEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        if (topic.equals(DEVICE_CREATED) || topic.equals(DEVICE_UPDATED)) {
            Device device = (Device) event.getSource();
            List<ComTaskEnablement> comTasksWithoutExecutions = device.getDeviceConfiguration().getComTaskEnablements().stream()
                    .filter(cte -> device.getComTaskExecutions().stream().noneMatch(comTaskExecution -> comTaskExecution.getComTask().getId() == cte.getComTask().getId()))
                    .collect(Collectors.toList());
            comTasksWithoutExecutions.stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks()
                    .stream().noneMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask)).forEach(cte->device.newAdHocComTaskExecution(cte).add());
            comTasksWithoutExecutions.stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks()
                    .stream().anyMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask)).forEach(cte->device.newFirmwareComTaskExecution(cte).add());
        }
    }
}
