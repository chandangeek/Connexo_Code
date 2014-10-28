package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

/**
 * Copyrights EnergyICT
 * Date: 10/27/14
 * Time: 2:59 PM
 */
public class UnknownDeviceMessageId extends LocalizedException {

    public UnknownDeviceMessageId(Thesaurus thesaurus, Device device, DeviceMessageId deviceMessageId) {
        super(thesaurus, MessageSeeds.UNKNOWN_DEVICE_MESSAGE_ID_FOR_DEVICE, device.getName(), deviceMessageId.dbValue());
        this.set("device", device);
        this.set("deviceMessageId", deviceMessageId);
    }
}
