package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.LoadProfileType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link com.energyict.mdc.device.config.LoadProfileSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * which is modeled by a {@link LoadProfileType} which is not part of the {@link com.energyict.mdc.device.config.DeviceType}
 *
 * Copyrights EnergyICT
 * Date: 04/02/14
 * Time: 15:08
 */
public class LoadProfileTypeIsNotConfiguredOnDeviceTypeException extends LocalizedException{

    public LoadProfileTypeIsNotConfiguredOnDeviceTypeException(Thesaurus thesaurus, LoadProfileType loadProfileType) {
        super(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_NOT_ON_DEVICE_TYPE, loadProfileType);
        set("loadProfileType", loadProfileType);
    }
}
