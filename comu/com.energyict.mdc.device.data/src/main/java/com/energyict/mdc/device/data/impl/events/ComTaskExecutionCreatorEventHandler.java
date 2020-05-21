/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.device.data.impl.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComTaskExecutionCreatorEventHandler extends EventHandler<LocalEvent> {

    private static final String DEVICE_CREATED = EventType.DEVICE_CREATED.topic();
    private static final String DEVICE_UPDATED = EventType.DEVICE_UPDATED.topic();

    public ComTaskExecutionCreatorEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        if (topic.equals(DEVICE_CREATED) || topic.equals(DEVICE_UPDATED)) {
            Device device = (Device) event.getSource();
            Map<Long, ComTaskExecution> comTaskExecutionAndComTaskIdMap = new HashMap<>();
            device.getComTaskExecutions().forEach(comTaskExecution -> comTaskExecutionAndComTaskIdMap.put(comTaskExecution.getComTask().getId(), comTaskExecution));
            List<ComTaskEnablement> comTasksWithoutExecutions = device.getDeviceConfiguration().getComTaskEnablements().stream()
                    .filter(comTaskEnablement -> !comTaskExecutionAndComTaskIdMap.containsKey(comTaskEnablement.getId()))
                    .collect(Collectors.toList());
            comTasksWithoutExecutions.stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks()
                    .stream().noneMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask)).forEach(cte ->
            {
                if (cte.hasPartialConnectionTask()) {
                    device.getConnectionTasks().stream()
                            .filter(connectionTask -> connectionTask.getPartialConnectionTask()
                                    .getId() == cte.getPartialConnectionTask().get().getId())
                            .findFirst()
                            .ifPresent(connectionTask -> device.newAdHocComTaskExecution(cte).connectionTask(connectionTask).add());
                } else {
                    device.newAdHocComTaskExecution(cte).add();
                }
            });
            comTasksWithoutExecutions.stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks()
                    .stream().anyMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask)).forEach(cte ->
            {
                if (cte.hasPartialConnectionTask()) {
                    device.getConnectionTasks().stream()
                            .filter(connectionTask -> connectionTask.getPartialConnectionTask()
                                    .getId() == cte.getPartialConnectionTask().get().getId())
                            .findFirst()
                            .ifPresent(connectionTask -> device.newFirmwareComTaskExecution(cte).connectionTask(connectionTask).add());
                } else {
                    device.newFirmwareComTaskExecution(cte).add();
                }
            });
        }
    }
}
