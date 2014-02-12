package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link LogBookType} to a {@link DeviceType}
 * but that LogBookType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:49)
 */
public class LogBookTypeAlreadyInDeviceTypeException extends LocalizedException {

    public LogBookTypeAlreadyInDeviceTypeException(Thesaurus thesaurus, DeviceType deviceType, LogBookType logBookType) {
        super(thesaurus, MessageSeeds.DUPLICATE_LOG_BOOK_TYPE_IN_DEVICE_TYPE);
        this.set("deviceType", deviceType);
        this.set("logBookType", logBookType);
    }

}