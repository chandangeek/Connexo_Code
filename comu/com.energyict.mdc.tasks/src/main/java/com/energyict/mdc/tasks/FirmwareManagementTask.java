/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

/**
 * Models the protocol task that can upgrade the firmware of a Device
 *
 * @since 1.1
 */
public interface FirmwareManagementTask extends ProtocolTask {

    boolean isValidFirmwareCommand(DeviceMessageSpec deviceMessageId);
}
