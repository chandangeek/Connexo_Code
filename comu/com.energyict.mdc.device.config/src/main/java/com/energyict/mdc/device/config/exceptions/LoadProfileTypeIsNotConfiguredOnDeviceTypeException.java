package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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

    public LoadProfileTypeIsNotConfiguredOnDeviceTypeException(LoadProfileType loadProfileType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, loadProfileType.getName());
        set("loadProfileTypeName", loadProfileType.getName());
    }

}