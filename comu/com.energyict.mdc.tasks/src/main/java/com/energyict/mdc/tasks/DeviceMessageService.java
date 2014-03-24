package com.energyict.mdc.tasks;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

public interface DeviceMessageService {
    public DeviceMessageSpec findDeviceMessageSpec(String key);
    public DeviceMessageCategory findDeviceMessageCategory(String key);
}
