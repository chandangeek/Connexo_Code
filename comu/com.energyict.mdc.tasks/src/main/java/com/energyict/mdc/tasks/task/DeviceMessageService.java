package com.energyict.mdc.tasks.task;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

public interface DeviceMessageService {
    public DeviceMessageSpec findDeviceMessageSpec(String key);
    public DeviceMessageCategory findDeviceMessageCategory(String key);
}
