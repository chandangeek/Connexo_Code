package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a {@link com.energyict.mdc.device.config.LogBookSpec}
 * with an {@link LogBookType} that is already used
 * by another {@link com.energyict.mdc.device.config.LogBookSpec} in the
 * {@link com.energyict.mdc.device.config.DeviceConfiguration}
 *
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 15:06
 */
public class DuplicateLogBookTypeException extends LocalizedException{

    public DuplicateLogBookTypeException(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, LogBookType logBookType, LogBookSpec logBookSpec) {
        super(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_LOG_BOOK_TYPE_IN_SPEC, deviceConfiguration, logBookType);
        set("deviceConfiguration", deviceConfiguration);
        set("logBookType", logBookType);
        set("logBookSpec", logBookSpec);
    }
}
