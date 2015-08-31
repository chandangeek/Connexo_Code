package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.LoadProfileType;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a {@link com.energyict.mdc.device.config.LoadProfileSpec}
 * with an {@link LoadProfileType} that is already used
 * by another {@link com.energyict.mdc.device.config.LoadProfileSpec} in the
 * {@link com.energyict.mdc.device.config.DeviceConfiguration}
 *
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 14:45
 */
public class DuplicateLoadProfileTypeException extends LocalizedException{

    public DuplicateLoadProfileTypeException(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType, LoadProfileSpec loadProfileSpec) {
        super(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_LOAD_PROFILE_TYPE_IN_SPEC, deviceConfiguration.getName(), loadProfileType.getName());
        set("deviceConfiguration", deviceConfiguration);
        set("loadProfileType", loadProfileType);
        set("loadProfileSpec", loadProfileSpec);
    }
}
