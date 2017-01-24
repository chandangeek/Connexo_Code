package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import java.util.Collection;

import static com.elster.jupiter.util.streams.Predicates.not;

/**
 * Created by bvn on 10/24/14.
 */
public class DeviceMessageSpecInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceMessageSpecInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public DeviceMessageSpecInfo asInfo(DeviceMessageSpec deviceMessageSpec, Device device) {
        DeviceMessageSpecInfo info = new DeviceMessageSpecInfo();
        info.id=deviceMessageSpec.getId().name();
        info.name=deviceMessageSpec.getName();
        info.willBePickedUpByPlannedComTask = device.getComTaskExecutions()
                .stream()
                .filter(not(ComTaskExecution::isOnHold))
                .map(ComTaskExecution::getComTask)
                .map(ComTask::getProtocolTasks)
                .flatMap(Collection::stream)
                .filter(task -> task instanceof MessagesTask).
                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                anyMatch(category -> category.getId() == deviceMessageSpec.getCategory().getId());
        if (info.willBePickedUpByPlannedComTask) {
            info.willBePickedUpByComTask = true; // shortcut
        } else {
            info.willBePickedUpByComTask = device.getDeviceConfiguration().
                    getComTaskEnablements().stream().
                    map(ComTaskEnablement::getComTask).
                    flatMap(comTask -> comTask.getProtocolTasks().stream()).
                    filter(task -> task instanceof MessagesTask).
                    flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                    anyMatch(category -> category.getId() == deviceMessageSpec.getCategory().getId());
        }
        return info;
    }

    public DeviceMessageSpecInfo asInfoWithMessagePropertySpecs(DeviceMessageSpec deviceMessageSpec, Device device) {
        DeviceMessageSpecInfo info = asInfo(deviceMessageSpec, device);
        info.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceMessageSpec.getPropertySpecs(), TypedProperties.empty(), device);
        return info;
    }

}
