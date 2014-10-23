package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.tasks.MessagesTask;
import java.util.EnumSet;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageInfoFactory {

    public DeviceMessageInfo asInfo(DeviceMessage<?> deviceMessage) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.id = deviceMessage.getId();
        info.trackingId = deviceMessage.getTrackingId();
        info.name = deviceMessage.getSpecification().getName();
        info.category = deviceMessage.getSpecification().getCategory().getName();
        info.status = deviceMessage.getStatus();
        info.creationDate = deviceMessage.getCreationDate();
        info.releaseDate = deviceMessage.getReleaseDate();
        info.sentDate = deviceMessage.getSentDate().orElse(null);
        info.user = deviceMessage.getUser().getName();
        info.errorMessage = deviceMessage.getProtocolInfo();

        if (EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING).contains(deviceMessage.getStatus())) {
            info.willBePickedUpByScheduledComTask = ((Device) deviceMessage.getDevice()).getComTaskExecutions().stream().
                    filter(cte->!cte.isOnHold()).
                    flatMap(cte -> cte.getComTasks().stream()).
                    flatMap(comTask -> comTask.getProtocolTasks().stream()).
                    filter(task -> task instanceof MessagesTask).
                    flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                    anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
            if (info.willBePickedUpByScheduledComTask) {
                info.willBePickedUpByComTask = true; // shortcut
            } else {
                info.willBePickedUpByComTask = ((Device) deviceMessage.getDevice()).getDeviceConfiguration().
                        getComTaskEnablements().stream().
                        map(ComTaskEnablement::getComTask).
                        flatMap(comTask -> comTask.getProtocolTasks().stream()).
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
            }
        }

        return info;
    }
}
