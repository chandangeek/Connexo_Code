package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

/**
 * Adds functionality to a DeviceMessage which should not be in the public API
 *
 * Copyrights EnergyICT
 * Date: 11/5/14
 * Time: 10:57 AM
 */
public interface ServerDeviceMessage extends DeviceMessage<Device> {

    /**
     * Moves the DeviceMessage to the new status.
     *
     * @param status the new DeviceMessageStatus
     */
    public void moveTo(DeviceMessageStatus status);

}
