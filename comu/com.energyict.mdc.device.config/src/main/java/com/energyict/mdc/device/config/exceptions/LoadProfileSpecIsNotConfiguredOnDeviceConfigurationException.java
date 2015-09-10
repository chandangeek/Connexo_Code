package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.LoadProfileSpec;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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

    public LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(LoadProfileSpec loadProfileSpec, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, loadProfileSpec);
        set("loadProfileSpec", loadProfileSpec);
    }

}