package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceHelper;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;

/**
 * Provides an implementation for the {@link DeviceHelper} interface.Created by tvn on 10/06/2016.
 */
public class DeviceHelperImpl implements DeviceHelper {
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
