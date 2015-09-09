package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LogBookType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link LogBookType} to a {@link DeviceType}
 * but that LogBookType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:49)
 */
public class LogBookTypeAlreadyInDeviceTypeException extends LocalizedException {

    public LogBookTypeAlreadyInDeviceTypeException(DeviceType deviceType, LogBookType logBookType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
        this.set("deviceType", deviceType);
        this.set("logBookType", logBookType);
    }

}