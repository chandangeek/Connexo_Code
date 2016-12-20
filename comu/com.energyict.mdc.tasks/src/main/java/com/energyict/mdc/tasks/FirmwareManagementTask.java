package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

/**
 * Models the protocol task that can upgrade the firmware of a Device
 *
 * @since 1.1
 */
public interface FirmwareManagementTask extends ProtocolTask {

    boolean isValidFirmwareCommand(DeviceMessageSpec deviceMessageId);
}
