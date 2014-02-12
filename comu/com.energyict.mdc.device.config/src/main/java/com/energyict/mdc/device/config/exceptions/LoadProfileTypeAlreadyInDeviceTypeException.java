package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link LoadProfileType} to a {@link DeviceType}
 * but that LoadProfileType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:41)
 */
public class LoadProfileTypeAlreadyInDeviceTypeException extends LocalizedException {

    public LoadProfileTypeAlreadyInDeviceTypeException(Thesaurus thesaurus, DeviceType deviceType, LoadProfileType loadProfileType) {
        super(thesaurus, MessageSeeds.DUPLICATE_LOAD_PROFILE_TYPE_IN_DEVICE_TYPE);
        this.set("deviceType", deviceType);
        this.set("loadProfileType", loadProfileType);
    }

}