package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.LoadProfileSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link com.energyict.mdc.device.config.ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * which is modeled by a {@link com.energyict.mdc.masterdata.MeasurementType} of a {@link LoadProfileSpec}
 * which is not part of the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 06/02/14
 * Time: 16:33
 */
public class LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException extends LocalizedException {

    public LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(Thesaurus thesaurus, LoadProfileSpec loadProfileSpec) {
        super(thesaurus, MessageSeeds.CHANNEL_SPEC_LOAD_PROFILE_SPEC_IS_NOT_ON_DEVICE_CONFIGURATION, loadProfileSpec);
        set("loadProfileSpec", loadProfileSpec);
    }
}
