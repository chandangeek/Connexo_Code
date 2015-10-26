package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.util.Optional;

@ProviderType
public interface DeviceMessageService {

    Optional<DeviceMessage> findDeviceMessageById(long id);

    Optional<DeviceMessage> findAndLockDeviceMessageByIdAndVersion(long id, long version);
}
