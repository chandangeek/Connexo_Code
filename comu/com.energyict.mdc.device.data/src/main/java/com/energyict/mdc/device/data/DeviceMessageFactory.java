/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface DeviceMessageFactory {

    List<DeviceMessage> findByDeviceAndState(Device device, DeviceMessageStatus pending);

}