package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import java.util.Optional;

class DeviceMessageServiceImpl implements DeviceMessageService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    DeviceMessageServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
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
                filter(cte-> !cte.isOnHold()).
                flatMap(cte -> cte.getComTasks().stream()).
                flatMap(comTask -> comTask.getProtocolTasks().stream()).
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

}
