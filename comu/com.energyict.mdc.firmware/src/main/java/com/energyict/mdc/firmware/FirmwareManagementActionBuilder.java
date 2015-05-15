package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

public class FirmwareManagementActionBuilder {
    private Device device;

    private final FirmwareService firmwareService;
    private final TaskService taskService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public FirmwareManagementActionBuilder(FirmwareService firmwareService, TaskService taskService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    public static boolean checkDeviceType(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareOption, FirmwareService firmwareService){
        if (deviceType != null){
            Set<ProtocolSupportedFirmwareOptions> deviceTypeAllowedOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType);
            return !deviceTypeAllowedOptions.isEmpty() && deviceTypeAllowedOptions.contains(firmwareOption);
        }
        return false;
    }

    public static boolean checkDeviceConfiguration(DeviceConfiguration deviceConfiguration, TaskService taskService){
        if (deviceConfiguration != null){
            Optional<ComTask> firmwareComTask = taskService.findFirmwareComTask();
            if (firmwareComTask.isPresent()){
                return deviceConfiguration.getComTaskEnablementFor(firmwareComTask.get()).isPresent();
            }
        }
        return false;
    }

    public static boolean checkDevice(Device device){
        if (device != null){

        }
        return false;
    }
}
