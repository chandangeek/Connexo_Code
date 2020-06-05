/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.EventType;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ComTaskExecutionCreatorEventHandler extends EventHandler<LocalEvent> {

    private static final String DEVICE_CREATED = EventType.DEVICE_CREATED.topic();
    private static final String DEVICE_UPDATED = EventType.DEVICE_UPDATED.topic();
    private static final String COMTASKENABLEMENT_CREATED = "com/energyict/mdc/device/config/comtaskenablement/CREATED";

    private DeviceService deviceService;

    @Inject
    public ComTaskExecutionCreatorEventHandler(DeviceService deviceService) {
        super(LocalEvent.class);
        this.deviceService = deviceService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        if (topic.equals(DEVICE_CREATED) || topic.equals(DEVICE_UPDATED)) {
            createComTaskExecutionsForDevice((Device) event.getSource());
        } else if (topic.equals(COMTASKENABLEMENT_CREATED)) {
            ComTaskEnablement comTaskEnablement = (ComTaskEnablement) event.getSource();
            deviceService.findDevicesByDeviceConfiguration(comTaskEnablement.getDeviceConfiguration()).stream().forEach(device -> {
                createNewComTaskExecution(comTaskEnablement, device);
            });
        }
    }

    public void createComTaskExecutionsForDevice(Device device) {
        Set<Long> comTaskIdsWithExecution = device.getComTaskExecutions().stream().map(comTaskExecution -> comTaskExecution.getComTask().getId()).collect(Collectors.toSet());
        List<ComTaskEnablement> comTasksWithoutExecutions = device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> !comTaskIdsWithExecution.contains(comTaskEnablement.getComTask().getId()))
                .collect(Collectors.toList());
        comTasksWithoutExecutions.forEach(cte -> createNewComTaskExecution(cte, device));
    }

    private void createNewComTaskExecution(ComTaskEnablement comTaskEnablement, Device device) {
        if (comTaskEnablement.getComTask().getProtocolTasks().stream().anyMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask)) {
            createNewFirmwareComTaskExecution(comTaskEnablement, device);
        } else {
            createNewAdHocComTaskExecution(comTaskEnablement, device);
        }
    }

    private void createNewFirmwareComTaskExecution(ComTaskEnablement comTaskEnablement, Device device) {
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask()
                            .getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(connectionTask -> device.newFirmwareComTaskExecution(comTaskEnablement).connectionTask(connectionTask).add());
        } else {
            device.newFirmwareComTaskExecution(comTaskEnablement).add();
        }
    }

    private void createNewAdHocComTaskExecution(ComTaskEnablement comTaskEnablement, Device device) {
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask()
                            .getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(connectionTask -> device.newAdHocComTaskExecution(comTaskEnablement).connectionTask(connectionTask).add());
        } else {
            device.newAdHocComTaskExecution(comTaskEnablement).add();
        }
    }
}
