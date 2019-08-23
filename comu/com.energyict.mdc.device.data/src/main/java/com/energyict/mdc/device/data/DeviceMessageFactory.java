/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface DeviceMessageFactory {

    List<DeviceMessage> findByDeviceAndState(Device device, DeviceMessageStatus pending);

}