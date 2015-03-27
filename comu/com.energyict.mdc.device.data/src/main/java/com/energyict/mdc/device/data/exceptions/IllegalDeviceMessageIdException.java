package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a DeviceMessageId.dbValue is not known by the
 * DeviceMessageId enumeration
 *
 * Copyrights EnergyICT
 * Date: 3/5/15
 * Time: 3:39 PM
 */
public class IllegalDeviceMessageIdException extends LocalizedException {

    public IllegalDeviceMessageIdException(Thesaurus thesaurus, long deviceMessageId) {
        super(thesaurus, MessageSeeds.DEVICE_MESSAGE_ID_NOT_SUPPORTED, deviceMessageId);
        this.set("deviceMessageId.dbValue", deviceMessageId);
    }
}
