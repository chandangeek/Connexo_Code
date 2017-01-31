/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import static com.elster.jupiter.util.streams.Predicates.not;

class DeviceMessageServiceImpl implements DeviceMessageService {

    private final DeviceDataModelService deviceDataModelService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    DeviceMessageServiceImpl(DeviceDataModelService deviceDataModelService, ThreadPrincipalService threadPrincipalService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public Optional<DeviceMessage> findDeviceMessageById(long id) {
        return this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).getOptional(id);
    }

    @Override
    public Optional<DeviceMessage> findAndLockDeviceMessageByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).lockObjectIfVersion(version, id);
    }

    @Override
    public boolean willDeviceMessageBePickedUpByPlannedComTask(Device device, DeviceMessage deviceMessage) {
        return device.getComTaskExecutions().stream().
                filter(not(ComTaskExecution::isOnHold)).
                map(ComTaskExecution::getComTask).
                map(ComTask::getProtocolTasks).
                flatMap(Collection::stream).
                filter(task -> task instanceof MessagesTask).
                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
    }

    @Override
    public boolean willDeviceMessageBePickedUpByComTask(Device device, DeviceMessage deviceMessage) {
        return device.getDeviceConfiguration().
                getComTaskEnablements().stream().
                map(ComTaskEnablement::getComTask).
                flatMap(comTask -> comTask.getProtocolTasks().stream()).
                filter(task -> task instanceof MessagesTask).
                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
    }

    @Override
    public ComTask getPreferredComTask(Device device, DeviceMessage<?> deviceMessage) {
        return device.getComTaskExecutions().stream().
                filter(cte -> cte.isAdHoc() && cte.isOnHold()).
                map(ComTaskExecution::getComTask).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst(). // An adHoc comTask that has already been executed (nextExecTimestamp==null)
                orElse(device.
                getDeviceConfiguration().
                getComTaskEnablements().stream().
                map(ComTaskEnablement::getComTask).
                filter(ct -> device.getComTaskExecutions().stream().
                        map(ComTaskExecution::getComTask).
                        noneMatch(comTask -> comTask.getId() == ct.getId())).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst(). // A Dangling ComTask -> There is no ComTaskExecution yet but the enabled comTask supports the device message category
                orElse(device.
                getComTaskExecutions().stream().
                sorted(Comparator.comparing(ComTaskExecution::isAdHoc).thenComparing(ComTaskExecution::isScheduledManually).thenComparing(ComTaskExecution::isOnHold).reversed()).
                map(ComTaskExecution::getComTask).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst().
                orElse(null)));
    }

    @Override
    public boolean canUserAdministrateDeviceMessage(DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId) {
        if (threadPrincipalService.getPrincipal() instanceof User) {
            User currentUser = (User) threadPrincipalService.getPrincipal();
            if (currentUser != null) {
                Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = deviceConfiguration.getDeviceMessageEnablements()
                        .stream()
                        .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId))
                        .findFirst();
                if (deviceMessageEnablementOptional.isPresent()) {
                    DeviceMessageEnablement deviceMessageEnablement = deviceMessageEnablementOptional.get();
                    return deviceMessageEnablement
                            .getUserActions()
                            .stream()
                            .anyMatch(deviceMessageUserAction -> currentUser.hasPrivilege("MDC", deviceMessageUserAction.getPrivilege()));
                }
            }
        }
        return true;
    }
}