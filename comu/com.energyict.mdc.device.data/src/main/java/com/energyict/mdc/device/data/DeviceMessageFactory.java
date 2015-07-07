package com.energyict.mdc.device.data;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Provides factory methods for DeviceMessages.
 * (The lifetime of this interface depends on when we move DeviceMessage to the new ORM framework ...)
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/03/14
 * Time: 11:09
 */
@ProviderType
public interface DeviceMessageFactory {

    List<DeviceMessage> findByDeviceAndState(Device device, DeviceMessageStatus pending);
}
