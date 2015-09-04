package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link LoadProfileType} to a {@link DeviceType}
 * but that LoadProfileType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:41)
 */
public class LoadProfileTypeAlreadyInDeviceTypeException extends LocalizedException {

    public LoadProfileTypeAlreadyInDeviceTypeException(DeviceType deviceType, LoadProfileType loadProfileType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, loadProfileType.getName(), deviceType.getName());
        this.set("deviceType", deviceType);
        this.set("loadProfileType", loadProfileType);
    }

}