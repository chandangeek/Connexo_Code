/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

public interface ServerDeviceMessage extends DeviceMessage<Device> {

    /**
     * Moves the DeviceMessage to the new status.
     *
     * @param status the new DeviceMessageStatus
     */
    public void moveTo(DeviceMessageStatus status);

}
