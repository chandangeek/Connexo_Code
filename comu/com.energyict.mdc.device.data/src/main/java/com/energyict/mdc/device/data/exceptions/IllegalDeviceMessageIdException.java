package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when a DeviceMessageId.dbValue is not known by the
 * DeviceMessageId enumeration
 *
 * Copyrights EnergyICT
 * Date: 3/5/15
 * Time: 3:39 PM
 */
public class IllegalDeviceMessageIdException extends LocalizedException {

    public IllegalDeviceMessageIdException(long deviceMessageId, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, deviceMessageId);
        this.set("deviceMessageId.dbValue", deviceMessageId);
    }

}